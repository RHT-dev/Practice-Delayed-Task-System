    package com.example.demo.scheduler;

    import com.example.demo.repository.DynamicTaskDao;
    import com.example.demo.entity.TaskEntity;
    import com.example.demo.entity.TaskStatus;
    import com.example.demo.retry.RetryPolicy;
    import com.example.demo.retry.RetryPolicyFactory;
    import com.example.demo.worker.WorkerPoolRegistry;

    import jakarta.annotation.PostConstruct;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Service;

    @Service
    public class TaskSchedulerService {

        private static final Logger log = LoggerFactory.getLogger(TaskSchedulerService.class);
        private final WorkerPoolRegistry pools;
        private final DynamicTaskDao dao;

        public TaskSchedulerService(WorkerPoolRegistry pools, DynamicTaskDao dao) {
            this.pools = pools;
            this.dao = dao;
        }

        @Scheduled(fixedDelay = 20000)
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

            log.info("Registered categories: {}", categories);
        }

        private void processCategory(String category) {
            var tasks = dao.findReadyTasks(category);
            log.info("Found {} ready tasks in category '{}'", tasks.size(), category);
            tasks.forEach(task -> tryStart(task, category));
        }


        private void tryStart(TaskEntity task, String category) {
            if (dao.updateStatus(task.getId(), category,
                    TaskStatus.CONSIDERED, TaskStatus.RUNNING) == 0)
                return;

            task.setStatus(TaskStatus.RUNNING);

            try {
                pools.getPool(category).submit(task,
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
            log.error("Task {} FAILED: {}", task.getId(), e.getMessage(), e);


            int attempt = task.getAttemptCount() + 1;

            if (attempt > task.getMaxAttempts()) {
                dao.finalStatus(task.getId(), category, TaskStatus.FAILED);
                return;
            }

            RetryPolicy retryPolicy = RetryPolicyFactory.createRetryPolicy(task);
            if (retryPolicy == null) {
                dao.finalStatus(task.getId(), category, TaskStatus.FAILED);
                return;
            }

            long delayMillis = retryPolicy.getNextDelay(attempt);
            task.setScheduledTime(task.getScheduledTime().plusNanos(delayMillis * 1_000_000)); // nanos → ms
            task.setAttemptCount(attempt);
            task.setStatus(TaskStatus.CONSIDERED);

            dao.updateForRetry(task, category);
            log.info("Retrying task {} in {}ms (attempt {})", task.getId(), delayMillis, attempt);
            log.info("Updated task {} for retry, next scheduled time: {}, attempt: {}",
                    task.getId(), task.getScheduledTime(), attempt);

        }


    }




