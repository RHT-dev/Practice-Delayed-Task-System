package com.example.demo.worker;

import org.springframework.stereotype.Component;
import com.example.demo.entityDB.TaskEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WorkerPoolRegistry {

    private final Map<String, ExecutorService> pools = new ConcurrentHashMap<>();

    public WorkerPoolRegistry() {
        pools.put("default", Executors.newFixedThreadPool(4));
    }

    public static class WorkerPool {
        private final ExecutorService executor;

        public WorkerPool(ExecutorService executor) {
            this.executor = executor;
        }

        public void submit(TaskEntity task) {
            /*executor.submit(() ->  {
                System.out.println("TEST: Running this " + task.getId() + " Class: " + task.getTaskClassName());
                // to do: будущая логика воркера

            })*/
        }
    }

    public WorkerPool get(String category) {
        return new WorkerPool(pools.getOrDefault(category, pools.get("default")));
    }

}
