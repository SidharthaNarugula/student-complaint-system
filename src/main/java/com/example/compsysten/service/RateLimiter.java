package com.example.compsysten.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simple in-memory global rate limiter.
 * Allows up to MAX_REQUESTS requests per TIME_WINDOW_MS (sliding window).
 * No external dependencies required.
 */
@Component
public class RateLimiter {

    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW_MS = 60_000; // 1 minute

    private final ConcurrentLinkedQueue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();

    /**
     * Attempts to acquire a request slot.
     *
     * @return true if the request is allowed, false if rate limit exceeded
     */
    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long windowStart = now - TIME_WINDOW_MS;

        // Evict timestamps outside the sliding window
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek() < windowStart) {
            requestTimestamps.poll();
        }

        if (requestTimestamps.size() < MAX_REQUESTS) {
            requestTimestamps.add(now);
            return true;
        }

        return false;
    }
}
