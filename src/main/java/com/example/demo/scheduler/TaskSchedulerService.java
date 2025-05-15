package com.example.demo.scheduler;

import com.example.demo.entityDB.TaskStatus;
import com.example.demo.entityDB.TaskEntity;
import com.example.demo.repositoryDataJPA.TaskRepository;
import com.example.demo.task.AbstractTask;
import com.example.demo.worker.WorkerPoolRegistry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TaskSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerService.class);
    private final TaskRepository taskRepository;
    private final WorkerPoolRegistry workerPoolRegistry;
    private final ObjectMapper objectMapper;

    public TaskSchedulerService(TaskRepository taskRepository, WorkerPoolRegistry workerPoolRegistry, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.workerPoolRegistry = workerPoolRegistry;
        this.objectMapper = objectMapper;
    }


    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void checkAndDispatchTasks() {
        List<TaskEntity> readyTasks = taskRepository.findAll().stream()
                .filter(task -> task.getStatus() == TaskStatus.CONSIDERED
                        && task.getScheduledTime().isBefore(LocalDateTime.now()))
                .toList();


        for (TaskEntity task : readyTasks) {
            if (task.getStatus() == TaskStatus.CANCELED) {
                log.info("Task {} is canceled, skipping.", task.getId());
                continue;
            }
            try {
                Class<?> classTask = Class.forName(task.getTaskClassName());

                AbstractTask abstractTask = (AbstractTask) classTask.getDeclaredConstructor().newInstance();

                Map<String, Object> params = objectMapper.readValue(task.getParamsJSON(), new TypeReference<>() {});
                abstractTask.execute(params);

                task.setStatus(TaskStatus.SUCCESS);
                taskRepository.save(task);

            } catch (Exception e) {
                log.error("Ошибка выполнения задачи id = " + task.getId() + "\nError: " +  e.getMessage());
                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);
            }
        }
    }
}
