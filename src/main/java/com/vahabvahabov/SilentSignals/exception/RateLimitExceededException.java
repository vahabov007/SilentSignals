package com.vahabvahabov.SilentSignals.exception;

public class RateLimitExceededException extends RuntimeException {

    private final Long userId;
    private final long remainingTime;

    public RateLimitExceededException(Long userId, long remainingTime) {
        super(String.format("Rate limit exceeded for user %d. Try again in %d seconds", userId, remainingTime));
        this.userId = userId;
        this.remainingTime = remainingTime;
    }

    public Long getUserId() {
        return userId;
    }

    public long getRemainingTime() {
        return remainingTime;
    }
}