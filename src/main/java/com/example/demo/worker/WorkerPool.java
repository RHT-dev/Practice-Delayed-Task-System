package com.example.demo.worker;

import com.example.demo.entityDB.TaskEntity;
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
                System.out.println("RUNNING THIS TASK: " + task.getId());
                Class<?> clazz = Class.forName(task.getTaskClassName());
                Runnable runnable = (Runnable) clazz.getDeclaredConstructor().newInstance();
                runnable.run();
            }
            catch (Exception e) {
                System.out.println("FAILED TO RUN THIS TASK: " + task.getId() + " " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }

}
