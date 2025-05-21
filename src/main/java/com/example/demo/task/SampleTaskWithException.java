package com.example.demo.task;

import java.util.Map;

public class SampleTaskWithException implements AbstractTask {
    @Override
    public void execute(Map<String, Object> params) {
        // для проверки работы политики повторов
        throw new RuntimeException("SampleTaskWithException: симулируем неудачу");
    }
}
