package com.example.demo.worker;

import com.example.demo.entity.TaskEntity;
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

    public void submit(TaskEntity task, Runnable handleSuccess, java.util.function.Consumer<Exception> handleFail) {
        executor.submit(() -> {
            try {
                AbstractTask impl = (AbstractTask) Class.forName(task.getTaskClassName())
                        .getDeclaredConstructor().newInstance();

                Map<String, Object> params = new ObjectMapper()
                        .readValue(task.getParamsJSON(), new TypeReference<>() {});

                impl.execute(params);

                handleSuccess.run();
            } catch (Exception e) {
                handleFail.accept(e);
            }
        });
    }


    public void shutdown() {
        executor.shutdown();
    }
}

