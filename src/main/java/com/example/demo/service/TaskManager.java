package com.example.demo.service;

import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;
import com.example.demo.repositoryDataJPA.TaskRepository;
import com.example.demo.worker.WorkerPoolRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskManager {

    private final TaskRepository taskRepository;
    private final WorkerPoolRegistry workerPoolRegistry;

    public TaskManager(TaskRepository taskRepository, WorkerPoolRegistry workerPoolRegistry) {
        this.taskRepository = taskRepository;
        this.workerPoolRegistry = workerPoolRegistry;
    }

    @Transactional
    public Long createAndScheduleTask(TaskEntity task) {
        task.setStatus(TaskStatus.CONSIDERED);
        task.setAttemptCount(0);
        if (task.getScheduledTime() == null) {
            task.setScheduledTime(LocalDateTime.now());
        }
        taskRepository.save(task);
        scheduleTask(task);
        return task.getId();
    }

    @Transactional
    public void cancelTask(Long taskID) {
        Optional<TaskEntity> optionalTask = taskRepository.findById(taskID);
        if (optionalTask.isPresent()) {
            TaskEntity task = optionalTask.get();
            if (task.getStatus() == TaskStatus.CONSIDERED || task.getStatus() == TaskStatus.FAILED) {
                taskRepository.updateStatus(taskID, task.getStatus(), TaskStatus.CANCELED);
            }
        }
    }

    @Transactional
    public TaskStatus getTaskStatus(Long taskID) {
        return taskRepository.findById(taskID)
                .map(TaskEntity::getStatus)
                .orElse(null);    }

    @Transactional
    public void scheduleTask(TaskEntity task) {
        var pool = workerPoolRegistry.get(task.getCategory());
        if (pool != null) {
            pool.submit(task);
        }
        else {
            throw new IllegalStateException("Pool does not exist");
        }
    }
}
