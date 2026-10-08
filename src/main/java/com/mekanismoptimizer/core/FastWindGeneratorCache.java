package com.mekanismoptimizer.core;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.api.math.FloatingLong;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * High-performance O(1) multiplier and sky-check cache for Wind Generators.
 * Eliminates per-second FloatingLong allocations (subtract, divide, multiply, add, divide)
 * and reduces repeated BlockPos / chunk skylight lookups.
 * Safe for runtime decoupling from MekanismGenerators compile-time classes.
 */
public final class FastWindGeneratorCache {

    // Cache computed height multiplier per (Dimension Key -> (Y coord -> FloatingLong))
    private static final Map<String, Int2ObjectMap<FloatingLong>> MULTIPLIER_CACHE = new ConcurrentHashMap<>();

    // Adaptive Skylight Scanner cache: packed BlockPos -> SkyCheckEntry
    private static final it.unimi.dsi.fastutil.longs.Long2ObjectMap<SkyCheckEntry> SKY_CACHE = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>();

    private record SkyCheckEntry(boolean canGenerate, long lastCheckedTick) {}

    private static boolean configInitialized = false;
    private static Supplier<Integer> windGenMinYSupplier;
    private static Supplier<Integer> windGenMaxYSupplier;
    private static Supplier<FloatingLong> windGenMinSupplier;
    private static Supplier<FloatingLong> windGenMaxSupplier;

    private FastWindGeneratorCache() {
    }

    /**
     * Clears cached multipliers on config reload or dimension reload.
     */
    public static void clear() {
        MULTIPLIER_CACHE.clear();
        synchronized (SKY_CACHE) {
            SKY_CACHE.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private static void initConfigAccess() {
        if (configInitialized) {
            return;
        }
        synchronized (FastWindGeneratorCache.class) {
            if (configInitialized) {
                return;
            }
            try {
                Class<?> configClass = Class.forName("mekanism.generators.common.config.MekanismGeneratorsConfig");
                Field genField = configClass.getField("generators");
                Object genConfig = genField.get(null);
                Class<?> genConfigClass = genConfig.getClass();

                Field minYField = genConfigClass.getField("windGenerationMinY");
                Object minYVal = minYField.get(genConfig);
                windGenMinYSupplier = () -> {
                    try {
                        return ((Supplier<Integer>) minYVal).get();
                    } catch (Exception e) {
                        return 24;
                    }
                };

                Field maxYField = genConfigClass.getField("windGenerationMaxY");
                Object maxYVal = maxYField.get(genConfig);
                windGenMaxYSupplier = () -> {
                    try {
                        return ((Supplier<Integer>) maxYVal).get();
                    } catch (Exception e) {
                        return 255;
                    }
                };

                Field minGField = genConfigClass.getField("windGenerationMin");
                Object minGVal = minGField.get(genConfig);
                windGenMinSupplier = () -> {
                    try {
                        return ((Supplier<FloatingLong>) minGVal).get();
                    } catch (Exception e) {
                        return FloatingLong.ZERO;
                    }
                };

                Field maxGField = genConfigClass.getField("windGenerationMax");
                Object maxGVal = maxGField.get(genConfig);
                windGenMaxSupplier = () -> {
                    try {
                        return ((Supplier<FloatingLong>) maxGVal).get();
                    } catch (Exception e) {
                        return FloatingLong.ZERO;
                    }
                };
            } catch (Throwable t) {
                windGenMinYSupplier = () -> 24;
                windGenMaxYSupplier = () -> 255;
                windGenMinSupplier = () -> FloatingLong.ZERO;
                windGenMaxSupplier = () -> FloatingLong.ZERO;
            }
            configInitialized = true;
        }
    }

    /**
     * Computes or returns the cached height multiplier for a wind generator at the given position.
     */
    @NotNull
    public static FloatingLong getOptimizedMultiplier(Level level, BlockPos worldPosition) {
        if (level == null || worldPosition == null) {
            return FloatingLong.ZERO;
        }

        // Adaptive Skylight Scanner: check cached status to avoid heavy canSeeSky raycast every second
        long packedPos = worldPosition.asLong();
        long currentTick = level.getGameTime();
        int interval = 100;
        try {
            if (MekanismOptimizerConfig.WIND_GENERATOR_SKY_CHECK_INTERVAL_TICKS != null) {
                interval = MekanismOptimizerConfig.WIND_GENERATOR_SKY_CHECK_INTERVAL_TICKS.get();
            }
        } catch (Throwable ignored) {
        }

        SkyCheckEntry skyEntry;
        synchronized (SKY_CACHE) {
            skyEntry = SKY_CACHE.get(packedPos);
        }

        boolean canGenerate;
        if (skyEntry != null && (currentTick - skyEntry.lastCheckedTick >= 0 && currentTick - skyEntry.lastCheckedTick < interval)) {
            canGenerate = skyEntry.canGenerate;
        } else {
            int topY = worldPosition.getY() + 4;
            BlockPos topPos = new BlockPos(worldPosition.getX(), topY, worldPosition.getZ());
            canGenerate = level.getFluidState(topPos).isEmpty() && level.canSeeSky(topPos);
            synchronized (SKY_CACHE) {
                SKY_CACHE.put(packedPos, new SkyCheckEntry(canGenerate, currentTick));
            }
        }

        if (!canGenerate) {
            return FloatingLong.ZERO;
        }

        int topY = worldPosition.getY() + 4;

        String dimKey = level.dimension().location().toString();
        Int2ObjectMap<FloatingLong> dimCache = MULTIPLIER_CACHE.computeIfAbsent(dimKey, k -> new Int2ObjectOpenHashMap<>());

        FloatingLong cached = dimCache.get(topY);
        if (cached != null) {
            return cached;
        }

        initConfigAccess();

        int confMinY = windGenMinYSupplier != null ? windGenMinYSupplier.get() : 24;
        int confMaxY = windGenMaxYSupplier != null ? windGenMaxYSupplier.get() : 255;
        FloatingLong minG = windGenMinSupplier != null ? windGenMinSupplier.get() : FloatingLong.ZERO;
        FloatingLong maxG = windGenMaxSupplier != null ? windGenMaxSupplier.get() : FloatingLong.ZERO;

        int minY = Math.max(confMinY, level.getMinBuildHeight());
        int maxY = Math.min(confMaxY, level.dimensionType().logicalHeight());
        float clampedY = Math.min(maxY, Math.max(minY, topY));

        if (minG.isZero() || maxY <= minY) {
            dimCache.put(topY, FloatingLong.ZERO);
            return FloatingLong.ZERO;
        }

        FloatingLong slope = maxG.subtract(minG).divide(maxY - minY);
        FloatingLong toGen = minG.add(slope.multiply(clampedY - minY));
        FloatingLong computed = toGen.divide(minG);

        dimCache.put(topY, computed);
        return computed;
    }
}
