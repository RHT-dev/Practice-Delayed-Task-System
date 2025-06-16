package com.example.demo.jmx;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TaskMonitoringJmx {
    private final AtomicInteger categoryCounter = new AtomicInteger(0);
    private final CopyOnWriteArrayList<String> categories = new CopyOnWriteArrayList<>();

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
