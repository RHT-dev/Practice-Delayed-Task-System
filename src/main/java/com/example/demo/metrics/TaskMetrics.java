package com.example.demo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TaskMetrics {

    private final MeterRegistry registry;

    public TaskMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSuccess(String category) {
        registry.counter("task_success_total", "category", category).increment();
    }

    public void recordFailure(String category) {
        registry.counter("task_failure_total", "category", category).increment();
    }

    public void recordConsidered(String category) {
        registry.counter("task_considered_total", "category", category).increment();
    }
}
