package com.example.demo.entityDB;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class TaskEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskClassName;
    private String category;

    @Column(columnDefinition = "TEXT")
    private String paramsJSON;

    private LocalDateTime scheduledTime;

    private int attemptCount;
    private int maxAttempts;

    @Enumerated(EnumType.STRING)
    private RetryType retryType;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(columnDefinition = "TEXT")
    private String retryParamsJSON;

    @Version
    @Column(name = "version")
    private long version;



    // setters and getters
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getRetryParamsJSON() {
        return retryParamsJSON;
    }

    public void setRetryParamsJSON(String retryParamsJSON) {
        this.retryParamsJSON = retryParamsJSON;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public RetryType getRetryType() {
        return retryType;
    }

    public void setRetryType(RetryType retryType) {
        this.retryType = retryType;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getParamsJSON() {
        return paramsJSON;
    }

    public void setParamsJSON(String paramsJSON) {
        this.paramsJSON = paramsJSON;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTaskClassName() {
        return taskClassName;
    }

    public void setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
