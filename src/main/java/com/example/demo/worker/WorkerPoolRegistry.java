package com.example.demo.worker;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class WorkerPoolRegistry {

    private final Map<String, WorkerPool> pools = new ConcurrentHashMap<>();

    public WorkerPool getPool(String category) {
        return pools.computeIfAbsent(category, cat -> new WorkerPool(4)); // дефолт 4 потока
    }

    public void registerPool(String category, int threads) {
        pools.put(category, new WorkerPool(threads));
    }

    public Set<String> getCategories() {
        return pools.keySet();
    }

    public void shutdown() {
        pools.values().forEach(WorkerPool::shutdown);
    }
}

