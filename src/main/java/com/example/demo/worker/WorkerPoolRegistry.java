package com.example.demo.worker;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class WorkerPoolRegistry {

    private final Map<String, WorkerPool> poolMap = new ConcurrentHashMap<>();

    public WorkerPoolRegistry() {
        registerPool("default", 4);
    }

    public void registerPool(String category, int threads) {
        poolMap.put(category, new WorkerPool(threads));
    }

    public WorkerPool get(String category) {
        return poolMap.getOrDefault(category, poolMap.get("default"));
    }

    public boolean hasPool(String category) {
        return poolMap.containsKey(category);
    }
}
