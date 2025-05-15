package com.example.demo.repositoryDataJPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long>, TaskRepositoryCustomInterface {
}