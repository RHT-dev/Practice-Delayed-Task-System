package com.example.demo.scheduler;

import com.example.demo.entityDB.TaskStatus;
import com.example.demo.entityDB.TaskEntity;
import com.example.demo.repositoryDataJPA.TaskRepository;
import com.example.demo.worker.WorkerPool;
import com.example.demo.worker.WorkerPoolRegistry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerService.class);
    private final TaskRepository taskRepository;
    private final WorkerPoolRegistry workerPoolRegistry;
    private final ObjectMapper objectMapper;

    public TaskSchedulerService(TaskRepository taskRepository,
                                WorkerPoolRegistry workerPoolRegistry,
                                ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.workerPoolRegistry = workerPoolRegistry;
        this.objectMapper = objectMapper;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 2000)
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
                WorkerPool pool = workerPoolRegistry.getPool(task.getCategory());
                if (pool != null) {
                    pool.submit(task);
                }
                else {
                    log.warn("NO WORKER FOR THIS CATEGORY", task.getCategory());
                    task.setStatus(TaskStatus.FAILED);
                    taskRepository.save(task);
                }

            } catch (Exception e) {
                log.error("Ошибка выполнения задачи id = " + task.getId() + "\nError: " +  e.getMessage());
                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);
            }
        }
    }

    public void handleFailure(TaskEntity task) {
        int currentAttempts = task.getAttemptCount();
        int maxAttempts = task.getMaxAttempts();

        if(currentAttempts >= maxAttempts) {
            log.warn("Task id:{}, попытки превысили максимальное количество. Статус задачи теперь FAILED", task.getId());
            task.setStatus(TaskStatus.FAILED);
        }
        else {
            task.setAttemptCount(currentAttempts + 1);
            task.setStatus(TaskStatus.CONSIDERED);
            task.setScheduledTime(calculateNextRuntime(task));
            taskRepository.save(task);
        }
    }

    public LocalDateTime calculateNextRuntime(TaskEntity task) {
        Map<String, Object> retryParams;
        try {
            retryParams = objectMapper.readValue(task.getRetryParamsJSON(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("FAILED TO PARSE JSON PARAMS. USING 30 SEC DELAY INSTEAD. " + e.getMessage());
            return LocalDateTime.now().plusSeconds(15); // 15
        }

        long baseDelay = ((Number) retryParams.getOrDefault("delay", 30)).longValue(); // в секундах

        return switch (task.getRetryType()) {
            case CONSTANT -> LocalDateTime.now().plusSeconds(baseDelay);
            case EXPONENT -> LocalDateTime.now().plusSeconds((long) Math.pow(baseDelay, task.getAttemptCount()));
        };
    }
}
