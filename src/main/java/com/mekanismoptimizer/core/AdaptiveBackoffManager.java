package com.mekanismoptimizer.core;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Adaptive Backoff Manager for suppressing high-frequency tick polling.
 */
public class AdaptiveBackoffManager {
    private static final Object2LongOpenHashMap<Object> NEXT_ALLOWED_TICK = new Object2LongOpenHashMap<>();
    private static final Object2LongOpenHashMap<Object> CURRENT_BACKOFF = new Object2LongOpenHashMap<>();
    private static long currentGlobalTick = 0L;

    static {
        NEXT_ALLOWED_TICK.defaultReturnValue(0L);
        CURRENT_BACKOFF.defaultReturnValue(1L);
    }

    public static synchronized void onServerTick() {
        currentGlobalTick++;
    }

    public static synchronized boolean shouldBackoff(Object key) {
        long nextTick = NEXT_ALLOWED_TICK.getLong(key);
        return currentGlobalTick < nextTick;
    }

    public static synchronized void recordFailure(Object key) {
        long backoff = CURRENT_BACKOFF.getLong(key);
        int maxBackoff = 32;
        long nextBackoff = Math.min(maxBackoff, Math.max(2L, backoff * 2));
        CURRENT_BACKOFF.put(key, nextBackoff);
        NEXT_ALLOWED_TICK.put(key, currentGlobalTick + nextBackoff);
    }

    public static synchronized void recordSuccess(Object key) {
        CURRENT_BACKOFF.remove(key);
        NEXT_ALLOWED_TICK.remove(key);
    }

    public static synchronized void clear() {
        CURRENT_BACKOFF.clear();
        NEXT_ALLOWED_TICK.clear();
    }
}


