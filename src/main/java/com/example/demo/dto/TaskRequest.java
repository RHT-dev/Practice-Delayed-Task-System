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

    public String getTaskClassName() {
        return taskClassName;
    }

    public void setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getParamsJSON() {
        return paramsJSON;
    }

    public void setParamsJSON(String paramsJSON) {
        this.paramsJSON = paramsJSON;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public RetryType getRetryType() {
        return retryType;
    }

    public void setRetryType(RetryType retryType) {
        this.retryType = retryType;
    }

    public String getRetryParamsJSON() {
        return retryParamsJSON;
    }

    public void setRetryParamsJSON(String retryParamsJSON) {
        this.retryParamsJSON = retryParamsJSON;
    }
    
    public TaskEntity toEntity() {
        TaskEntity entity = new TaskEntity();
        entity.setTaskClassName(taskClassName);
        entity.setCategory(category);
        entity.setParamsJSON(paramsJSON);
        entity.setScheduledTime(scheduledTime);
        entity.setMaxAttempts(maxAttempts == null ? 1 : maxAttempts);
        entity.setRetryType(retryType == null ? RetryType.NONE : retryType);
        entity.setRetryParamsJSON(retryParamsJSON);
        return entity;
    }

}
