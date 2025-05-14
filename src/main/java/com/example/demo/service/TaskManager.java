package com.example.demo.service;

import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;
import com.example.demo.repositoryDataJPA.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskManager {

    private final TaskRepository taskRepository;

    public TaskManager(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskEntity createTask(TaskEntity task) {
        task.setStatus(TaskStatus.CONSIDERED);
        task.setAttemptCount(0);
        return taskRepository.save(task);
    }

    @Transactional
    public boolean cancelTask(Long taskID) {
        return taskRepository.findById(taskID).map(task -> {
            if (task.getStatus() == TaskStatus.CONSIDERED) {
                task.setStatus(TaskStatus.CANCELED);
                taskRepository.save(task);
                return true;
            }
            return false;
        }).orElse(false);
    }
}
