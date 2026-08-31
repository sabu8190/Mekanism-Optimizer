package com.mekanismoptimizer.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class MekanismOptimizerLogger {
    private static final Logger LOGGER = LogManager.getLogger("MekanismOptimizer");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    private static final AtomicLong RADIATION_CHECKS_SAVED = new AtomicLong(0);
    private static final AtomicLong CABLE_BACKOFF_SKIPPED = new AtomicLong(0);
    private static final AtomicLong UNIT_DISPLAY_CACHED = new AtomicLong(0);

    private static long lastLogTime = System.currentTimeMillis();

    // Async dedicated file logging
    private static final BlockingQueue<String> LOG_QUEUE = new LinkedBlockingQueue<>(5000);
    private static final ExecutorService FILE_WRITER_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MekanismOptimizer-LogWriter");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    static {
        initFileLogger();
    }

    private MekanismOptimizerLogger() {
    }

    private static void initFileLogger() {
        if (INITIALIZED.compareAndSet(false, true)) {
            FILE_WRITER_EXECUTOR.submit(() -> {
                Path logDir = Paths.get("logs");
                Path logFile = logDir.resolve("mekanism_optimizer.log");
                try {
                    if (!Files.exists(logDir)) {
                        Files.createDirectories(logDir);
                    }
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile.toFile(), true))) {
                        writer.write(String.format("=== Mekanism Optimizer Log Started at %s ===%n", DATE_FORMAT.format(new Date())));
                        writer.flush();

                        while (!Thread.currentThread().isInterrupted()) {
                            String entry = LOG_QUEUE.take();
                            writer.write(entry);
                            writer.newLine();
                            if (LOG_QUEUE.isEmpty()) {
                                writer.flush();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LOGGER.error("Failed in MekanismOptimizer log writer loop", e);
                }
            });
        }
    }

    private static void writeToFile(String level, String message) {
        try {
            if (MekanismOptimizerConfig.ENABLE_DEDICATED_LOG_FILE == null || MekanismOptimizerConfig.ENABLE_DEDICATED_LOG_FILE.get()) {
                String timestamp = DATE_FORMAT.format(new Date());
                String formatted = String.format("[%s] [%s] %s", timestamp, level, message);
                LOG_QUEUE.offer(formatted);
            }
        } catch (Exception ignored) {
        }
    }

    public static void info(String message, Object... params) {
        LOGGER.info(message, params);
        writeToFile("INFO", formatMessage(message, params));
    }

    public static void warn(String message, Object... params) {
        LOGGER.warn(message, params);
        writeToFile("WARN", formatMessage(message, params));
    }

    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        writeToFile("ERROR", message + "\n" + sw.toString());
    }

    private static String formatMessage(String message, Object... params) {
        if (params == null || params.length == 0) {
            return message;
        }
        try {
            return String.format(message.replace("{}", "%s"), params);
        } catch (Exception e) {
            return message;
        }
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
    public static void recordRadiationCheckSaved() { RADIATION_CHECKS_SAVED.incrementAndGet(); }
    public static void recordCableBackoffSkipped() { CABLE_BACKOFF_SKIPPED.incrementAndGet(); }
    public static void recordUnitDisplayCached() { UNIT_DISPLAY_CACHED.incrementAndGet(); }

    public static String getMetricsSummary() {
        return String.format("[MekanismOptimizer Stats] Pipe: %d | Backoff: %d | Poisson: %d | Solar: %d | Pump: %d | Net: %d | Packets: %d | Chunk: %d | Multi: %d | Factory: %d | MultiBlock: %d | Radiation: %d | Cable: %d | UnitDisplay: %d",
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
                MULTIBLOCK_TICKS_OPTIMIZED.get(),
                RADIATION_CHECKS_SAVED.get(),
                CABLE_BACKOFF_SKIPPED.get(),
                UNIT_DISPLAY_CACHED.get());
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
