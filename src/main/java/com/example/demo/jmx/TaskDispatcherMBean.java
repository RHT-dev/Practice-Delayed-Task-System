package com.example.demo.jmx;

import java.util.Map;

public interface TaskDispatcherMBean {
    int getCategoryCount();
    String[] getCategories();
    void shutdown();

    Map<String, Integer> getSuccessCountPerCategory();
    Map<String, Integer> getFailureCountPerCategory();
    Map<String, Long> getExecutionTimePerCategory();

    String[] getExecutionStatsAsString();
}
