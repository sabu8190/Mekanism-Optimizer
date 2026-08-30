package com.mekanismoptimizer.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public final class MekanismOptimizerLogger {
    private static final Logger LOGGER = LogManager.getLogger("MekanismOptimizer");

    private static final AtomicLong PIPE_SIM_AVOIDED = new AtomicLong(0);
    private static final AtomicLong BACKOFF_TICKS_SKIPPED = new AtomicLong(0);
    private static final AtomicLong POISSON_O1_SAMPLES = new AtomicLong(0);
    private static final AtomicLong SOLAR_LOOKUPS_CACHED = new AtomicLong(0);
    private static final AtomicLong PUMP_SKIPS = new AtomicLong(0);
    private static final AtomicLong NET_EMIT_SKIPS = new AtomicLong(0);
    private static final AtomicLong PACKETS_COALESCED = new AtomicLong(0);
    private static final AtomicLong CHUNKLOAD_SKIPS = new AtomicLong(0);
    private static final AtomicLong MULTITHREAD_TASKS = new AtomicLong(0);
    private static final AtomicLong FACTORY_SORT_OPTIMIZED = new AtomicLong(0);
    private static final AtomicLong MULTIBLOCK_TICKS_OPTIMIZED = new AtomicLong(0);

    private static long lastLogTime = System.currentTimeMillis();

    private MekanismOptimizerLogger() {
    }

    public static void info(String message, Object... params) {
        LOGGER.info(message, params);
    }

    public static void warn(String message, Object... params) {
        LOGGER.warn(message, params);
    }

    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
    }

    public static void recordPipeSimAvoided() { PIPE_SIM_AVOIDED.incrementAndGet(); }
    public static void recordMekanismSimulationSaved() { PIPE_SIM_AVOIDED.incrementAndGet(); }
    public static void recordPathCalculated() { PIPE_SIM_AVOIDED.incrementAndGet(); }
    public static void recordBackoffTick() { BACKOFF_TICKS_SKIPPED.incrementAndGet(); }
    public static void recordBackoffSkipped() { BACKOFF_TICKS_SKIPPED.incrementAndGet(); }
    public static void recordPoissonSample() { POISSON_O1_SAMPLES.incrementAndGet(); }
    public static void recordSolarCached() { SOLAR_LOOKUPS_CACHED.incrementAndGet(); }
    public static void recordSolarCheckCached() { SOLAR_LOOKUPS_CACHED.incrementAndGet(); }
    public static void recordPumpSkip() { PUMP_SKIPS.incrementAndGet(); }
    public static void recordPumpOptimized() { PUMP_SKIPS.incrementAndGet(); }
    public static void recordPumpThrottled() { PUMP_SKIPS.incrementAndGet(); }
    public static void recordNetEmitSkip() { NET_EMIT_SKIPS.incrementAndGet(); }
    public static void recordNetworkEmitSkipped() { NET_EMIT_SKIPS.incrementAndGet(); }
    public static void recordPacketCoalesced() { PACKETS_COALESCED.incrementAndGet(); }
    public static void recordChunkloadSkip() { CHUNKLOAD_SKIPS.incrementAndGet(); }
    public static void recordChunkloadGated() { CHUNKLOAD_SKIPS.incrementAndGet(); }
    public static void recordParallelTask() { MULTITHREAD_TASKS.incrementAndGet(); }
    public static void recordMultithreadTask() { MULTITHREAD_TASKS.incrementAndGet(); }
    public static void recordFactorySortOptimized() { FACTORY_SORT_OPTIMIZED.incrementAndGet(); }
    public static void recordMultiblockOptimized() { MULTIBLOCK_TICKS_OPTIMIZED.incrementAndGet(); }

    public static String getMetricsSummary() {
        return String.format("[MekanismOptimizer Stats] Pipe Sim: %d | Backoff: %d | Poisson O(1): %d | Solar: %d | Pump: %d | Net Emits: %d | Packets: %d | Chunkloads: %d | Multithread: %d | Factory Sort: %d | Multiblock: %d",
                PIPE_SIM_AVOIDED.get(),
                BACKOFF_TICKS_SKIPPED.get(),
                POISSON_O1_SAMPLES.get(),
                SOLAR_LOOKUPS_CACHED.get(),
                PUMP_SKIPS.get(),
                NET_EMIT_SKIPS.get(),
                PACKETS_COALESCED.get(),
                CHUNKLOAD_SKIPS.get(),
                MULTITHREAD_TASKS.get(),
                FACTORY_SORT_OPTIMIZED.get(),
                MULTIBLOCK_TICKS_OPTIMIZED.get());
    }

    public static void checkAndLogStats() {
        int interval = MekanismOptimizerConfig.LOG_INTERVAL_SECONDS.get();
        if (interval <= 0) return;

        long now = System.currentTimeMillis();
        if (now - lastLogTime >= interval * 1000L) {
            lastLogTime = now;
            info(getMetricsSummary());
        }
    }
}
