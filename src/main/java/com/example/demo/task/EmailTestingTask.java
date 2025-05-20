package com.example.demo.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EmailTestingTask implements AbstractTask {
    @Override
    public void execute(Map<String, Object> params) {
        System.out.println("Executing email task with params: " + params);
        throw new RuntimeException("Simulated failure"); // 💥 обязательно!
    }
}
