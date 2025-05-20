package com.example.demo.retry;

public class ExponentRetryPolicy implements RetryPolicy {
    private final double exponent;
    private final long maxDelay;


    public ExponentRetryPolicy(double exponent, long maxDelay) {
        this.exponent = exponent;
        this.maxDelay = maxDelay;
    }

    @Override
    public long getNextDelay(int attemptNumber) {
        double delay = Math.pow(exponent, attemptNumber);
        return Math.min((long) delay, maxDelay);
    }
}
