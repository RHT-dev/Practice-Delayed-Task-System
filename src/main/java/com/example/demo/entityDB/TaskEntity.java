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
    private long version;

    // to do: setters + getters

}
