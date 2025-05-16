package com.example.demo.scheduler;

import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;
import com.example.demo.repositoryDataJPA.TaskRepository;
import com.example.demo.worker.WorkerPool;
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
                    log.info("Task {} submitted to pool '{}'", task.getId(), task.getCategory());
                } else {
                    log.warn("No worker pool found for category '{}'", task.getCategory());
                    handleFailure(task);
                }

            } catch (Exception e) {
                log.error("Ошибка выполнения задачи id = {}. Ошибка: {}", task.getId(), e.getMessage(), e);
                handleFailure(task);
            }
        }
    }

    public void handleFailure(TaskEntity task) {
        int currentAttempts = task.getAttemptCount();
        int maxAttempts = task.getMaxAttempts();

        if (currentAttempts >= maxAttempts) {
            log.warn("Task id:{} превысил maxAttempts. Устанавливаем статус FAILED.", task.getId());
            task.setStatus(TaskStatus.FAILED);
        } else {
            task.setAttemptCount(currentAttempts + 1);
            task.setStatus(TaskStatus.CONSIDERED);
            task.setScheduledTime(calculateNextRuntime(task));
            log.info("Task id:{} будет повторно запланирован на {}", task.getId(), task.getScheduledTime());
        }

        taskRepository.save(task);
    }

    public LocalDateTime calculateNextRuntime(TaskEntity task) {
        Map<String, Object> retryParams;
        try {
            retryParams = objectMapper.readValue(task.getRetryParamsJSON(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Не удалось распарсить retryParamsJSON задачи id: {}. Используем задержку по умолчанию (30 сек). Ошибка: {}", task.getId(), e.getMessage());
            return LocalDateTime.now().plusSeconds(30);
        }

        long baseDelay = ((Number) retryParams.getOrDefault("delay", 30)).longValue();

        return switch (task.getRetryType()) {
            case CONSTANT -> LocalDateTime.now().plusSeconds(baseDelay);
            case EXPONENT -> LocalDateTime.now().plusSeconds((long) Math.pow(baseDelay, task.getAttemptCount()));
        };
    }
}
