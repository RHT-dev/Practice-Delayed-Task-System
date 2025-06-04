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
    private final DynamicTaskTableDao dao;

    public TaskDispatcherMBeanImpl(TaskDispatcher dispatcher, DynamicTaskTableDao dao) {
        this.dispatcher = dispatcher;
        this.dao = dao;
    }

    @Override
    @ManagedAttribute(description = "Number of task categories")
    public int getCategoryCount() {
        return dao.getAllCategories().size();
    }

    @Override
    @ManagedAttribute(description = "List of categories")
    public String[] getCategories() {
        return dao.getAllCategories().toArray(new String[0]);
    }

    @Override
    @ManagedAttribute(description = "Queue size for category")
    public int getQueueSize(String category) {
        return dispatcher.getQueueSize(category);
    }

    @Override
    @ManagedOperation(description = "Shutdown all dispatchers")
    public void shutdown() {
        dispatcher.shutdown();
    }
}
