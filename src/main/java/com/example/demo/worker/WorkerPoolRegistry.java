package com.example.demo.worker;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class WorkerPoolRegistry {

    private final Map<String, WorkerPool> pools = new ConcurrentHashMap<>();

    public WorkerPool getPool(String category) {
        return pools.computeIfAbsent(category.toLowerCase(Locale.ROOT),
                cat -> new WorkerPool(4));
    }

    public void registerPool(String category, int threads) {
        pools.computeIfAbsent(category.toLowerCase(Locale.ROOT), c -> new WorkerPool(threads));
    }

    public void initPoolForCategory(String category) {
        getPool(category.toLowerCase(Locale.ROOT));
    }


    public Set<String> getCategories() {
        return pools.keySet();
    }

    public void shutdown() {
        pools.values().forEach(WorkerPool::shutdown);
    }
}

