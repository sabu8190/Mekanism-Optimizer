package com.mekanismoptimizer.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class MekanismOptimizerLogger {
    private static final Logger LOGGER = LogManager.getLogger("MekanismOptimizer");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static PrintWriter fileWriter = null;

    // Real-time Optimization Metrics
    private static final AtomicLong mekanismSimulationsSaved = new AtomicLong(0);
    private static final AtomicLong backoffTicksThrottled = new AtomicLong(0);
    private static final AtomicLong poissonSamplesComputed = new AtomicLong(0);
    private static final AtomicLong solarChecksCached = new AtomicLong(0);
    private static final AtomicLong pumpQueriesThrottled = new AtomicLong(0);
    private static final AtomicLong networkEmitsSkipped = new AtomicLong(0);
    private static final AtomicLong packetsCoalesced = new AtomicLong(0);
    private static final AtomicLong chunkloadsGated = new AtomicLong(0);
    private static final AtomicLong parallelTasksExecuted = new AtomicLong(0);
    private static final AtomicLong factorySortsOptimized = new AtomicLong(0);

    static {
        try {
            File logDir = new File("logs/mekanism_optimizer");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            File logFile = new File(logDir, "runtime.log");
            fileWriter = new PrintWriter(new FileWriter(logFile, true), true);
            logToFile("INFO", "MekanismOptimizer Dedicated Logging System initialized.");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize file logger for MekanismOptimizer", e);
        }
    }

    private static synchronized void logToFile(String level, String message) {
        if (fileWriter != null) {
            String time = DATE_FORMAT.format(new Date());
            fileWriter.println(String.format("[%s] [%s] %s", time, level, message));
        }
    }

    public static boolean isVerbose() {
        try {
            return MekanismOptimizerConfig.ENABLE_VERBOSE_LOGGING != null &&
                   MekanismOptimizerConfig.ENABLE_VERBOSE_LOGGING.get() != null &&
                   MekanismOptimizerConfig.ENABLE_VERBOSE_LOGGING.get();
        } catch (Exception e) {
            return false;
        }
    }

    public static void info(String message, Object... params) {
        LOGGER.info(message, params);
        logToFile("INFO", String.format(message.replace("{}", "%s"), params));
    }

    public static void debug(String message, Object... params) {
        if (isVerbose()) {
            LOGGER.debug(message, params);
            logToFile("DEBUG", String.format(message.replace("{}", "%s"), params));
        }
    }

    public static void warn(String message, Object... params) {
        LOGGER.warn(message, params);
        logToFile("WARN", String.format(message.replace("{}", "%s"), params));
    }

    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
        if (fileWriter != null) {
            String time = DATE_FORMAT.format(new Date());
            fileWriter.println(String.format("[%s] [ERROR] %s", time, message));
            t.printStackTrace(fileWriter);
        }
    }

    public static void recordMekanismSimulationSaved() { mekanismSimulationsSaved.incrementAndGet(); }
    public static void recordBackoffTick() { backoffTicksThrottled.incrementAndGet(); }
    public static void recordPoissonSample() { poissonSamplesComputed.incrementAndGet(); }
    public static void recordSolarCheckCached() { solarChecksCached.incrementAndGet(); }
    public static void recordPumpThrottled() { pumpQueriesThrottled.incrementAndGet(); }
    public static void recordNetworkEmitSkipped() { networkEmitsSkipped.incrementAndGet(); }
    public static void recordPacketCoalesced() { packetsCoalesced.incrementAndGet(); }
    public static void recordChunkloadGated() { chunkloadsGated.incrementAndGet(); }
    public static void recordParallelTask() { parallelTasksExecuted.incrementAndGet(); }
    public static void recordFactorySortOptimized() { factorySortsOptimized.incrementAndGet(); }

    public static String getMetricsSummary() {
        return String.format(
            "[MekanismOptimizer Stats] Pipe Sim: %d | Backoff: %d | Poisson O(1): %d | Solar: %d | Pump: %d | Net Emits: %d | Packets: %d | Chunkloads: %d | Multithread: %d | Factory Sort: %d",
            mekanismSimulationsSaved.get(),
            backoffTicksThrottled.get(),
            poissonSamplesComputed.get(),
            solarChecksCached.get(),
            pumpQueriesThrottled.get(),
            networkEmitsSkipped.get(),
            packetsCoalesced.get(),
            chunkloadsGated.get(),
            parallelTasksExecuted.get(),
            factorySortsOptimized.get()
        );
    }
}