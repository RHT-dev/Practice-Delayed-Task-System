package com.example.demo.scheduler;

import com.example.demo.JDBC_TEMPLATE.DynamicTaskDao;
import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;
import com.example.demo.repositoryDataJPA.TaskRepository;
import com.example.demo.worker.WorkerPool;
import com.example.demo.worker.WorkerPoolRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TaskSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerService.class);
    private final WorkerPoolRegistry pools;
    private final DynamicTaskDao dao;

    public void dispatch() {
        pools.getCategori
    }
}




