package com.example.demo.repositoryDataJPA;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<TaskEntity, Long>, TaskRepositoryCustomInterface {

    @Modifying
    @Query("UPDATE TaskEntity t SET t.status = :newStatus WHERE t.id = :taskId AND t.status = :expectedStatus")
    int updateStatusIfMatches(@Param("taskId") Long taskId,
                              @Param("expectedStatus") TaskStatus expectedStatus,
                              @Param("newStatus") TaskStatus newStatus);

}