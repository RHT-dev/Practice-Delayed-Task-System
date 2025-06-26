package com.example.demo.jmx;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class TaskMonitoringJmx {
    private final AtomicInteger categoryCounter = new AtomicInteger(0);
    private final CopyOnWriteArrayList<String> categories = new CopyOnWriteArrayList<>();


    // >> Счётчик время выполнения и количество выполненых задач удачно и не удачно по каждой категории
    private final Map<String, AtomicInteger> successTaskCounter = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failureTaskCounter = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> executionTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> executedTaskCounter = new ConcurrentHashMap<>();

    public void recordSuccessTask(String category, long duration) {
        successTaskCounter.computeIfAbsent(category, cat -> new AtomicInteger()).incrementAndGet();
        executionTime.computeIfAbsent(category, cat -> new AtomicLong()).addAndGet(duration);
        executedTaskCounter.computeIfAbsent(category, cat -> new AtomicInteger()).incrementAndGet();
    }

    public void recordFailureTask(String category) {
        failureTaskCounter.computeIfAbsent(category, cat -> new AtomicInteger()).incrementAndGet();
    }

    public Map<String, Integer> getSuccessTaskPerCategory() {
        return successTaskCounter.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    public Map<String, Integer> getFailureTaskPerCategory() {
        return failureTaskCounter.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    // время в миллисекундах
    public Map<String, Long> getExecutionTimePerCategory() {
        Map<String, Long> averageTimes = new HashMap<>();
        for (String category : executionTime.keySet()) {
            long totalTime = executionTime.get(category).get();
            int counter = executedTaskCounter.getOrDefault(category, new AtomicInteger(1)).get();
            averageTimes.put(category, totalTime / Math.max(counter, 1));
        }
        return averageTimes;
    }

    // вывод статов в одну строку
    public String[] getExecutionStatsAsString() {
        return executionTime.keySet().stream()
                .map(category -> String.format(
                        "%s | success=%d | failed=%d | avgTime=%dms",
                        category,
                        successTaskCounter.getOrDefault(category, new AtomicInteger(0)).get(),
                        failureTaskCounter.getOrDefault(category, new AtomicInteger(0)).get(),
                        executionTime.getOrDefault(category, new AtomicLong(0)).get() /
                                Math.max(1, executedTaskCounter.getOrDefault(category,
                                        new AtomicInteger(0)).get())
                ))
                .toArray(String[]::new);
    }

    public void updateCategories(List<String> newCategories) {
        categories.clear();
        categories.addAll(newCategories);
        categoryCounter.set(newCategories.size());
    }

    public int getCategoryCount() {
        return categoryCounter.get();
    }

    public String[] getCategories() {
        return categories.toArray(new String[0]);
    }
}
