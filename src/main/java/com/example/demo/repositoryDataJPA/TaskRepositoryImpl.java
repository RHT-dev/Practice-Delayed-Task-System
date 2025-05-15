package com.example.demo.repositoryDataJPA;

import com.example.demo.entityDB.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepositoryImpl  implements TaskRepositoryCustomInterface {

    private final EntityManager entityManager;

    public TaskRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean updateStatus(Long id, TaskStatus from, TaskStatus to) {
        Query query = entityManager.createQuery("""
            UPDATE TaskEntity task SET task.status = :to WHERE task.id = :id AND task.status = :from
        """);
        query.setParameter("id", id);
        query.setParameter("from", from);
        query.setParameter("to", to);
        return query.executeUpdate() == 1;
    }
}

