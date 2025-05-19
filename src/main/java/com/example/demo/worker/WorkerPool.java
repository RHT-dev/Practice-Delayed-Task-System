package com.example.demo.worker;

import com.example.demo.entityDB.TaskEntity;
import com.example.demo.task.AbstractTask;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerPool {
    private final ExecutorService executor;

    public WorkerPool(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public void submit(TaskEntity task) {
        executor.submit(() -> {
            try {
                Class<?> clazz = Class.forName(task.getTaskClassName());
                AbstractTask runnable = (AbstractTask) clazz.getDeclaredConstructor().newInstance();

                // Параметры
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> params = mapper.readValue(task.getParamsJSON(), new TypeReference<>() {});
                runnable.execute(params);

                // Обновим статус задачи в БД (лучше делать через сервис)
                // Временно лог
                System.out.println("Task executed successfully: " + task.getId());

            } catch (Exception e) {
                System.err.println("Task execution failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}

