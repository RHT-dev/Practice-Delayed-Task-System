package com.example.demo.jmx;

import com.example.demo.scheduler.TaskDispatcher;
import com.example.demo.repository.DynamicTaskTableDao;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = "com.example.demo:type=TaskDispatcher", description = "Task Dispatcher Management Bean")
public class TaskDispatcherMBeanImpl implements TaskDispatcherMBean {

    private final TaskDispatcher dispatcher;
    private final TaskMonitoringJmx monitoring;

    public TaskDispatcherMBeanImpl(TaskDispatcher dispatcher, TaskMonitoringJmx monitoring) {
        this.dispatcher = dispatcher;
        this.monitoring = monitoring;
    }

    @Override
    @ManagedAttribute(description = "Number of task categories")
    public int getCategoryCount() {
        return monitoring.getCategoryCount();  // получение без БД
    }

    @Override
    @ManagedAttribute(description = "List of categories")
    public String[] getCategories() {
        return monitoring.getCategories();  // кэшированный список
    }

    @Override
    @ManagedOperation(description = "Shutdown all dispatchers")
    public void shutdown() {
        dispatcher.shutdown();
    }
}