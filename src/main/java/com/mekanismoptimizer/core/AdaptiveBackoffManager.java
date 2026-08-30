package com.mekanismoptimizer.core;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;

/**
 * Adaptive Exponential Backoff Manager for Mekanism logistical pipes, transporters, and power cables.
 * Suppresses high-frequency tick polling for obstructed, full, or failing inventory/energy interactions.
 * Wakes up automatically when maximum backoff expires or upon block update notification.
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

    public static synchronized boolean shouldRun(Object key, long currentTick) {
        long nextTick = NEXT_ALLOWED_TICK.getLong(key);
        if (currentTick < nextTick) {
            MekanismOptimizerLogger.recordBackoffTick();
            return false;
        }
        return true;
    }

    public static synchronized boolean shouldBackoff(Object key) {
        long nextTick = NEXT_ALLOWED_TICK.getLong(key);
        if (currentGlobalTick < nextTick) {
            return true;
        }
        return false;
    }

    public static synchronized void recordFailure(Object key) {
        recordFailure(key, currentGlobalTick);
    }

    public static synchronized void reset(Object key) {
        recordSuccess(key);
    }

    public static synchronized void recordFailure(Object key, long currentTick) {
        long backoff = CURRENT_BACKOFF.getLong(key);
        int maxBackoff = 32;
        try {
            if (MekanismOptimizerConfig.MAX_BACKOFF_TICKS != null && MekanismOptimizerConfig.MAX_BACKOFF_TICKS.get() != null) {
                maxBackoff = MekanismOptimizerConfig.MAX_BACKOFF_TICKS.get();
            }
        } catch (Exception ignored) {
            // Safe fallback for testing
        }

        long nextBackoff = Math.min(maxBackoff, backoff * 2);
        CURRENT_BACKOFF.put(key, nextBackoff);
        NEXT_ALLOWED_TICK.put(key, currentTick + nextBackoff);
    }

    public static synchronized void recordSuccess(Object key) {
        CURRENT_BACKOFF.remove(key);
        NEXT_ALLOWED_TICK.remove(key);
    }

    public static synchronized void wakeUp(Object key) {
        recordSuccess(key);
    }

    public static synchronized void notifyBlockUpdate(BlockPos pos) {
        CURRENT_BACKOFF.remove(pos);
        NEXT_ALLOWED_TICK.remove(pos);
    }

    public static synchronized void clear() {
        CURRENT_BACKOFF.clear();
        NEXT_ALLOWED_TICK.clear();
    }
}
