package com.example.demo.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class SomeTestingTask implements AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(SomeTestingTask.class);

    @Override
    public void execute(Map<String, Object> params) {
        log.info("SomeTestingTask выполняется с параметрами: {}", params);
    }
}
