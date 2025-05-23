package com.example.demo.retry;

import com.example.demo.entity.RetryType;
import com.example.demo.entity.TaskEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RetryPolicyResolver {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static RetryPolicy createRetryPolicy(TaskEntity task) {
        if (task.getMaxAttempts() <= 1 || task.getRetryType() == null || task.getRetryType() == RetryType.NONE) {
            return null;
        }

        RetryType retryType = task.getRetryType();
        if (retryType == null || task.getRetryType() == RetryType.NONE) {return null;}

        try {
            JsonNode jsonNode = objectMapper.readTree(task.getRetryParamsJSON());

            switch (retryType) {
                case NONE -> {
                    return null;
                }

                case CONSTANT -> {
                    long delay = jsonNode.get("delay").asLong();
                    return new ConstantRetryPolicy(delay);
                }

                case EXPONENT -> {
                    double exponent = jsonNode.has("exponent") ? jsonNode.get("exponent").asDouble() : Math.E;
                    long maxDelay = jsonNode.has("maxDelay") ? jsonNode.get("maxDelay").asLong() : Long.MAX_VALUE;
                    return new ExponentRetryPolicy(exponent, maxDelay);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("ERROR RETRY POLICY: " + e.getMessage(), e);
        }

        return null;
    }
}
