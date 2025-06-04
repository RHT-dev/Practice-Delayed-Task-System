package com.example.demo.jmx;

public interface TaskDispatcherMBean {

    int getCategoryCount();
    String[] getCategories();
    int getQueueSize(String category);

    void shutdown();
}
