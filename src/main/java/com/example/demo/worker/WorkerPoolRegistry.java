package com.example.demo.worker;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class WorkerPoolRegistry {

    private final Map<String, WorkerPool> poolMap = new ConcurrentHashMap<>();

    public WorkerPool getPool(String category) {
        return poolMap.computeIfAbsent(category, cat -> new WorkerPool(4)); // дефолт 4 потока
    }

    public void registerPool(String category, int threads) {
        poolMap.put(category, new WorkerPool(threads));
    }

    public boolean hasPool(String category) {
        return poolMap.containsKey(category);
    }

    public void shutdownAll() {
        poolMap.values().forEach(WorkerPool::shutdown);
    }
}

