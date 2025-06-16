package com.example.demo.scheduler;

import com.example.demo.entity.TaskEntity;
import com.example.demo.entity.TaskStatus;
import com.example.demo.jmx.TaskMonitoringJmx;
import com.example.demo.repository.DynamicTaskTableDao;
import com.example.demo.retry.RetryPolicy;
import com.example.demo.retry.RetryPolicyResolver;
import com.example.demo.worker.WorkerPoolRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class TaskDispatcher {

    private static final Logger log = LoggerFactory.getLogger(TaskDispatcher.class);

    private final WorkerPoolRegistry pools;
    private final DynamicTaskTableDao dao;

    private final Map<String, DelayQueue<DelayedTaskWrapper>> categoryQueues = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> categoryDispatchers = new ConcurrentHashMap<>();

    private final TaskMonitoringJmx taskMonitoringJmx;

    public TaskDispatcher(WorkerPoolRegistry pools, DynamicTaskTableDao dao, TaskMonitoringJmx taskMonitoringJmx) {
        this.pools = pools;
        this.dao = dao;
        this.taskMonitoringJmx = taskMonitoringJmx;
    }

    @PostConstruct
    public void init() {
        List<String> categories = dao.getAllCategories();
        taskMonitoringJmx.updateCategories(categories);
        log.info("Initializing dispatcher queues for categories: {}", categories);
        categories.forEach(this::initCategoryDispatcher);
    }

    public void initCategoryDispatcher(String category) {
        categoryQueues.computeIfAbsent(category, cat -> new DelayQueue<>());

        categoryDispatchers.computeIfAbsent(category, cat -> {
            log.info("Starting dispatcher thread for category: {}", category);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> runDispatcherLoop(category));
            return executor;
        });

        pools.registerPool(category.toLowerCase(), 4);
    }

    public void enqueueTask(TaskEntity task) {
        String category = task.getCategory().toLowerCase();

        initCategoryDispatcher(category);

        categoryQueues.get(category).offer(new DelayedTaskWrapper(task));

        log.info("Task {} enqueued in category '{}' with scheduledTime {}",
                task.getId(), category, task.getScheduledTime());
    }

    private void runDispatcherLoop(String category) {
        DelayQueue<DelayedTaskWrapper> queue = categoryQueues.get(category);

        while (true) {
            try {
                // не отдаст задачу до scheduledTime
                DelayedTaskWrapper wrapper = queue.take();
                TaskEntity task = wrapper.getTask();

                log.info("Dispatcher picked task {} for category '{}' (scheduledTime: {})",
                        task.getId(), category, task.getScheduledTime());

                tryStart(task, category);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Dispatcher thread for category '{}' interrupted — stopping", category);
                break;
            } catch (Exception ex) {
                log.error("Error in dispatcher loop for category '{}': {}", category, ex.getMessage(), ex);
            }
        }
    }

    private void tryStart(TaskEntity task, String category) {
        if (dao.updateStatus(task.getId(), category,
                TaskStatus.CONSIDERED, TaskStatus.RUNNING) == 0) {
            log.info("Skipping task {} — already not in CONSIDERED (current status changed)", task.getId());
            return;
        }

        task.setStatus(TaskStatus.RUNNING);
        log.info(">>> TASK {} STATUS: {}", task.getId(), task.getStatus());

        try {
            pools.getPool(category.toLowerCase()).submit(task,
                    () -> handleSuccess(task, category),
                    ex -> handleFail(task, category, ex)
            );
            log.info("Task {} handed to pool '{}'", task.getId(), category);
        } catch (Exception ex) {
            handleFail(task, category, ex);
        }
    }

    private void handleSuccess(TaskEntity task, String category) {
        dao.finalStatus(task.getId(), category, TaskStatus.SUCCESS);
        log.info("Task {} status: SUCCESS", task.getId());
    }

    private void handleFail(TaskEntity task, String category, Exception e) {

        int attempt = task.getAttemptCount() + 1;

        if (attempt > task.getMaxAttempts()) {
            dao.finalStatus(task.getId(), category, TaskStatus.FAILED);
            log.info("Task {} status: FAILED", task.getId());
            return;
        }

        RetryPolicy retryPolicy = RetryPolicyResolver.createRetryPolicy(task);
        if (retryPolicy == null) {
            dao.finalStatus(task.getId(), category, TaskStatus.FAILED);
            log.info("Task {} status: FAILED", task.getId());
            return;
        }

        long delayMillis = retryPolicy.getNextDelay(attempt);

        task.setScheduledTime(task.getScheduledTime().plusNanos(delayMillis * 1_000_000));
        task.setAttemptCount(attempt);
        task.setStatus(TaskStatus.CONSIDERED);

        dao.updateForRetry(task, category);

        log.info("Retrying task {} — next scheduled time: {}, attempt: {}",
                task.getId(), task.getScheduledTime(), attempt);

        enqueueTask(task);
    }

    public void shutdown() {
        log.info("Shutting down dispatchers...");
        categoryDispatchers.forEach((category, executor) -> {
            log.info("Shutting down dispatcher for category '{}'", category);
            executor.shutdownNow();
        });
        log.info("All dispatchers shutdown complete.");
    }


    // для JMX
    public int getQueueSize(String category) {
        DelayQueue<DelayedTaskWrapper> queue = categoryQueues.get(category.toLowerCase());
        return queue == null ? 0 : queue.size();
    }

}
