package com.example.demo.retry;

public interface RetryPolicy {
    long getNextDelay(int attemptNumber);
}
