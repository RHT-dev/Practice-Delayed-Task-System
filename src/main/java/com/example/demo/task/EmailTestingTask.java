package com.example.demo.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EmailTestingTask implements AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(EmailTestingTask.class);

    @Override
    public void execute(Map<String, Object> params) {
        log.info("EmailTestingTask выполняется с параметрами: {}", params);
    }
}
