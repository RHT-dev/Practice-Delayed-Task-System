package com.example.demo.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static TaskEntity fromRs(ResultSet rs) throws SQLException {
        TaskEntity task = new TaskEntity();
        task.setId(rs.getLong("id"));
        task.setTaskClassName(rs.getString("task_class_name"));
        task.setCategory(extractCategory(rs.getMetaData().getTableName(1)));   // task_category_email → email
        task.setParamsJSON(rs.getString("params_json"));
        task.setRetryParamsJSON(rs.getString("retry_params_json"));
        task.setRetryType(RetryType.valueOf(rs.getString("retry_type")));
        task.setScheduledTime(rs.getTimestamp("scheduled_time").toLocalDateTime());
        task.setAttemptCount(rs.getInt("attempt_count"));
        task.setMaxAttempts(rs.getInt("max_attempts"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setVersion(rs.getLong("version"));
        return task;
    }

    private static String extractCategory(String table) {
        return table.replaceFirst("^task_category2_", "");
    }


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
