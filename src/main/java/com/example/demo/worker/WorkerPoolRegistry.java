package com.example.demo.worker;

import org.springframework.stereotype.Component;
import com.example.demo.entityDB.TaskEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WorkerPoolRegistry {

    private static final Map<String, ExecutorService> pools = new ConcurrentHashMap<>();

    public WorkerPoolRegistry() {
        pools.put("default", Executors.newFixedThreadPool(5));
    }

    public static class WorkerPool {
        private final ExecutorService executor;

        public WorkerPool(ExecutorService executor) {
            this.executor = executor;
        }

        public void submit(TaskEntity task) {
            executor.submit(() -> {
                try {
                    System.out.println("TEST: Running task " + task.getId() + " Class: " + task.getTaskClassName());

                    // Пример загрузки и запуска класса по имени (будущая реализация)
                    Class<?> clazz = Class.forName(task.getTaskClassName());
                    Runnable runnable = (Runnable) clazz.getDeclaredConstructor().newInstance();
                    runnable.run();

                } catch (Exception e) {
                    System.err.println("Failed to execute task ID: " + task.getId() + ", error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

    }

    public WorkerPool get(String category) {
        return new WorkerPool(pools.getOrDefault(category, pools.get("default")));
    }


}
