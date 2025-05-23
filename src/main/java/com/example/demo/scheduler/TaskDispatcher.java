    package com.example.demo.scheduler;

    import com.example.demo.repository.DynamicTaskTableDao;
    import com.example.demo.entity.TaskEntity;
    import com.example.demo.entity.TaskStatus;
    import com.example.demo.retry.RetryPolicy;
    import com.example.demo.retry.RetryPolicyResolver;
    import com.example.demo.worker.WorkerPoolRegistry;

    import jakarta.annotation.PostConstruct;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Service;

    import java.util.Locale;
    import java.util.Optional;

    @Service
    public class TaskDispatcher {

        private static final Logger log = LoggerFactory.getLogger(TaskDispatcher.class);
        private final WorkerPoolRegistry pools;
        private final DynamicTaskTableDao dao;

        public TaskDispatcher(WorkerPoolRegistry pools, DynamicTaskTableDao dao) {
            this.pools = pools;
            this.dao = dao;
        }

        @Scheduled(fixedDelay = 5000)
        public void dispatch() {
            log.info(">>> DISPATCH TRIGGERED");
            var categories = pools.getCategories();
            log.info("Registered categories: {}", categories);
            categories.forEach(this::processCategory);
        }

        @PostConstruct
        public void init() {
            var categories = dao.getAllCategories();
            categories.forEach(cat -> pools.registerPool(cat, 4));
        }

        private void processCategory(String category) {
            while (true) {
                Optional<TaskEntity> optionalTask = dao.fetchReadyTask(category);
                if (optionalTask.isEmpty()) break;

                TaskEntity task = optionalTask.get();

                task.setStatus(TaskStatus.RUNNING);
                log.info(">>> TASK STATUS: {}", task.getStatus());

                tryStart(task, category);

                try {
                    pools.getPool(category.toLowerCase(Locale.ROOT)).submit(task,
                            () -> handleSuccess(task, category),
                            ex -> handleFail(task, category, ex)
                    );
                    log.info("Task {} handed to pool '{}'", task.getId(), category);
                } catch (Exception ex) {
                    handleFail(task, category, ex);
                }
            }
        }

        private void tryStart(TaskEntity task, String category) {
            if (dao.updateStatus(task.getId(), category,
                    TaskStatus.CONSIDERED, TaskStatus.RUNNING) == 0)
                return;

            task.setStatus(TaskStatus.RUNNING);
            log.info(">>> TASK STATUS: {}", task.getStatus());

            try {
                pools.getPool(category.toLowerCase(Locale.ROOT)).submit(task,
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
            task.setScheduledTime(task.getScheduledTime().plusNanos(delayMillis * 1_000_000)); // nanos → ms
            task.setAttemptCount(attempt);
            task.setStatus(TaskStatus.CONSIDERED);

            dao.updateForRetry(task, category);
            log.info("Retrying task {} for retry, next scheduled time: {}, attempt: {}",
                    task.getId(), task.getScheduledTime(), attempt);
        }


    }




