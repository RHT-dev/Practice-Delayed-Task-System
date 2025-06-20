package com.example.demo.service;

import com.example.demo.jmx.TaskMonitoringJmx;
import com.example.demo.repository.DynamicTaskTableDao;
import com.example.demo.entity.TaskEntity;
import com.example.demo.entity.TaskStatus;
import com.example.demo.worker.WorkerPoolRegistry;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskLifecycleService {

    private final DynamicTaskTableDao dao;
    private final WorkerPoolRegistry pools;

    private final TaskMonitoringJmx taskMonitoringJmx;

    public TaskLifecycleService(DynamicTaskTableDao dao, WorkerPoolRegistry pools, TaskMonitoringJmx taskMonitoringJmx) {
        this.dao = dao;
        this.pools = pools;
        this.taskMonitoringJmx = taskMonitoringJmx;
    }

    public void initCategoryPool(String category) {
        pools.initPoolForCategory(category);
    }

    public long create(TaskEntity task) {
        task.setStatus(TaskStatus.CONSIDERED);
        task.setAttemptCount(0);
        if (task.getMaxAttempts() <= 0) {
            task.setMaxAttempts(1);
        }
        if (task.getScheduledTime() == null) {
            task.setScheduledTime(LocalDateTime.now());
        }
        long id = dao.save(task);
        task.setId(id);

        taskMonitoringJmx.updateCategories(dao.getAllCategories());
        return id;
    }

    public boolean cancel(long id, String category) {
        return dao.updateStatus(id, category,
                TaskStatus.CONSIDERED, TaskStatus.CANCELED) == 1;
    }

    public TaskStatus getStatus(long id, String category) {
        return dao.getStatus(id, category);
    }
}
