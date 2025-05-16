package com.example.demo.retryPolicy;

public interface RetryPolicy {
    long getNextDelay(int attemptNumber);
}
