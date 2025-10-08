package com.vahabvahabov.SilentSignals.security;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Primary
public class InMemoryRateLimiterUtil {

    private final Map<Long, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS = 5;
    private final long WINDOW_MINUTES = 5;

    public boolean isAllowed(Long userId) {
        long currentTime = System.currentTimeMillis();
        RateLimitInfo userLimit = rateLimitMap.get(userId);

        if (userLimit == null) {
            rateLimitMap.put(userId, new RateLimitInfo(1, currentTime));
            return true;
        }

        long timeElapsed = currentTime - userLimit.getStartTime();
        long windowInMillis = TimeUnit.MINUTES.toMillis(WINDOW_MINUTES);

        if (timeElapsed > windowInMillis) {
            rateLimitMap.put(userId, new RateLimitInfo(1, currentTime));
            return true;
        } else {
            if (userLimit.getCount() < MAX_REQUESTS) {
                userLimit.incrementCount();
                return true;
            } else {
                return false;
            }
        }
    }

    public Long getTimeUntilReset(Long userId) {
        RateLimitInfo userLimit = rateLimitMap.get(userId);
        if (userLimit == null) {
            return 0L;
        }

        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - userLimit.getStartTime();
        long windowInMillis = TimeUnit.MINUTES.toMillis(WINDOW_MINUTES);

        return Math.max(0, (windowInMillis - timeElapsed) / 1000);
    }

    private static class RateLimitInfo {
        private int count;
        private long startTime;

        public RateLimitInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }

        public int getCount() { return count; }
        public long getStartTime() { return startTime; }
        public void incrementCount() { this.count++; }
    }
}