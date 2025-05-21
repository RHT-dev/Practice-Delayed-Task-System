package com.example.demo.dto;

import com.example.demo.entity.RetryType;
import com.example.demo.entity.TaskEntity;

import java.time.LocalDateTime;
public class TaskRequest {

    private String taskClassName;
    private String category;
    private String paramsJSON;
    private LocalDateTime scheduledTime;
    private Integer maxAttempts;
    private RetryType retryType;
    private String retryParamsJSON;

    public TaskEntity toEntity() {
        TaskEntity entity = new TaskEntity();
        entity.setTaskClassName(taskClassName);
        entity.setCategory(category);
        entity.setParamsJSON(paramsJSON);
        entity.setScheduledTime(scheduledTime);
        entity.setMaxAttempts(maxAttempts == null ? 1 : maxAttempts);
        entity.setRetryType(retryType);
        entity.setRetryParamsJSON(retryParamsJSON);
        return entity;
    }

}
