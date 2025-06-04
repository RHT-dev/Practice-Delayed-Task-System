package com.example.demo.scheduler;

import com.example.demo.entity.TaskEntity;

import java.time.ZoneId;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedTaskWrapper implements Delayed {

    private final TaskEntity task;
    private final long triggerTimeMillis;

    public DelayedTaskWrapper(TaskEntity task) {
        this.task = task;
        this.triggerTimeMillis = task.getScheduledTime()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delayMillis = triggerTimeMillis - System.currentTimeMillis();
        return unit.convert(delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof DelayedTaskWrapper other) {
            return Long.compare(this.triggerTimeMillis, other.triggerTimeMillis);
        }
        return 0;
    }

    public TaskEntity getTask() {
        return task;
    }
}
