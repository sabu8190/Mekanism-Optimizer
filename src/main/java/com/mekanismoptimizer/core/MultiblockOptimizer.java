package com.mekanismoptimizer.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MultiblockOptimizer {
    private static final Map<Object, SteadyStateTracker> TRACKER_MAP = new ConcurrentHashMap<>();

    private MultiblockOptimizer() {
    }

    public static boolean shouldSkipCalculation(Object multiblock, long currentEnergyOrFluid, long capacity) {
        if (!MekanismOptimizerConfig.ENABLE_MULTIBLOCK_OPTIMIZATION.get()) {
            return false;
        }

        SteadyStateTracker tracker = TRACKER_MAP.computeIfAbsent(multiblock, k -> new SteadyStateTracker());
        if (tracker.isSteady(currentEnergyOrFluid, capacity)) {
            MekanismOptimizerLogger.recordMultiblockOptimized();
            return true;
        }
        return false;
    }

    public static void clear(Object multiblock) {
        TRACKER_MAP.remove(multiblock);
    }

    private static class SteadyStateTracker {
        private long lastValue = -1;
        private int steadyTicks = 0;

        public boolean isSteady(long currentValue, long capacity) {
            if (currentValue == lastValue && (currentValue == 0 || currentValue == capacity)) {
                steadyTicks++;
                // Skip every 3 out of 4 ticks when completely full or completely empty
                return (steadyTicks % 4 != 0);
            }
            lastValue = currentValue;
            steadyTicks = 0;
            return false;
        }
    }
}
