package com.example.demo.repositoryDataJPA;

import com.example.demo.entityDB.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class TaskRepositoryCustom implements TaskRepositoryCustomInterface {

    private final EntityManager entityManager;

    public TaskRepositoryCustom(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean updateStatus(Long id, TaskStatus from, TaskStatus to) {
        Query query = entityManager.createQuery("""
            UPDATE TaskEntity t SET t.status = :to WHERE t.id = :id AND t.status = :from
        """);
        query.setParameter("id", id);
        query.setParameter("from", from);
        query.setParameter("to", to);

        return query.executeUpdate() == 1;
    }
}
