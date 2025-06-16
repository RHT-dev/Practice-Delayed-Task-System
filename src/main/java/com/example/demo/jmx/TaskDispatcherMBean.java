package com.example.demo.jmx;

public interface TaskDispatcherMBean {
    int getCategoryCount();
    String[] getCategories();
    void shutdown();
}
