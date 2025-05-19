package com.example.demo.scheduler;

import com.example.demo.JDBC_TEMPLATE.DynamicTaskDao;
import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;
import com.example.demo.worker.WorkerPoolRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public void dispatch() {
        pools.getCategories().forEach(this::processCategory);
    }

    private void processCategory(String category) {
        dao.findReadyTasks(category).forEach(task -> tryStart(task, category));
    }

    private void tryStart(TaskEntity task, String category) {
        if (dao.updateStatus(task.getId(), category,
                TaskStatus.CONSIDERED, TaskStatus.RUNNING) == 0)
            return;

        try {
            pools.getPool(category).submit(task, () -> handleSuccess(task, category));
            log.info("Task {} handed to pool '{}'", task.getId(), category);
        } catch (Exception ex) {
            handleFail(task, category, ex);
        }
    }

    private void handleSuccess(TaskEntity task, String category) {
        dao.finalStatus(task.getId(), category, TaskStatus.SUCCESS);
    }

    private void handleFail(TaskEntity task, String category, Exception e) {
        log.error("Task {} FAILED: {} ", task.getId(), e.getMessage(), e);
        // to do: политику повторов реализовать тут
        dao.finalStatus(task.getId(), category, TaskStatus.FAILED);
    }

}




