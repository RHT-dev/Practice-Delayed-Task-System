package com.example.demo.retryPolicy;

public class ConstantRetryPolicy implements RetryPolicy {
    private final long delay;

    public ConstantRetryPolicy(long delay) {
        this.delay = delay;
    }

    @Override
    public long getNextDelay(int attemptNumber) {
        return delay;
    }
}
