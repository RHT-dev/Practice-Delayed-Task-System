package com.example.demo.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SampleTask implements AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(SampleTask.class);

    @Override
    public void execute(Map<String, Object> params) {
        log.info("SampleTask выполняется с параметрами: {}", params);
    }
}
