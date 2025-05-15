package com.example.demo.repositoryDataJPA;


import com.example.demo.entityDB.TaskStatus;

public interface TaskRepositoryCustomInterface {
    boolean updateStatus(Long id, TaskStatus from, TaskStatus to);
}