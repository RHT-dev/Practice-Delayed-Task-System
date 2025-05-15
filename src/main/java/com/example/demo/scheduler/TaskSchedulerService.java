package com.example.demo.scheduler;

import com.example.demo.entityDB.TaskStatus;
import com.example.demo.entityDB.TaskEntity;
import com.example.demo.repositoryDataJPA.TaskRepository;
import com.example.demo.worker.WorkerPoolRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerService.class);
    private final TaskRepository taskRepository;
    private final WorkerPoolRegistry workerPoolRegistry;

    public TaskSchedulerService(TaskRepository taskRepository, WorkerPoolRegistry workerPoolRegistry) {
        this.taskRepository = taskRepository;
        this.workerPoolRegistry = workerPoolRegistry;
    }


    @Scheduled
    public void checkAndDispatchTasks() {
        List<TaskEntity> readyTasks = taskRepository.findAll().stream()
                .filter(task -> task.getStatus() == TaskStatus.CONSIDERED
                        && task.getScheduledTime().isBefore(LocalDateTime.now()))
                .toList();

    }
}
