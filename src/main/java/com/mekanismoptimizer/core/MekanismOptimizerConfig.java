package com.mekanismoptimizer.core;

import net.minecraftforge.common.ForgeConfigSpec;

public class MekanismOptimizerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_ACCEPTOR_CACHE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FAST_SIMULATE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PATHFINDER_CACHE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ADAPTIVE_BACKOFF;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FAST_POISSON_SAMPLER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EJECTOR_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SOLAR_CACHE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PUMP_BACKOFF;
    public static final ForgeConfigSpec.BooleanValue ENABLE_NETWORK_EMIT_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PACKET_COALESCING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_OWNER_CHUNKLOAD_GATING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FACTORY_AUTO_SORT_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ADDON_OPTIMIZATIONS;
    public static final ForgeConfigSpec.IntValue MAX_BACKOFF_TICKS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_VERBOSE_LOGGING;

    static {
        BUILDER.push("MekanismOptimizer General Settings");

        ENABLE_ACCEPTOR_CACHE = BUILDER
                .comment("Enable caching and fast lookup for InventoryNetwork calculateAcceptors.")
                .define("enableAcceptorCache", true);

        ENABLE_FAST_SIMULATE = BUILDER
                .comment("Enable O(1) FastSlotIndexer early exit in getPredictedInsert for full/unmatched containers.")
                .define("enableFastSimulate", true);

        ENABLE_PATHFINDER_CACHE = BUILDER
                .comment("Enable pathfinding cache optimizations in TransporterPathfinder.")
                .define("enablePathfinderCache", true);

        ENABLE_ADAPTIVE_BACKOFF = BUILDER
                .comment("Enable adaptive exponential backoff for blocked/unreachable transporters.")
                .define("enableAdaptiveBackoff", true);

        ENABLE_FAST_POISSON_SAMPLER = BUILDER
                .comment("Enable O(1) PoissonSampler table lookup for machine upgrade statistical consumption (from Mekanism-Overclocked).")
                .define("enableFastPoissonSampler", true);

        ENABLE_EJECTOR_OPTIMIZATION = BUILDER
                .comment("Enable auto-ejector throttling and early exits for machine components.")
                .define("enableEjectorOptimization", true);

        ENABLE_SOLAR_CACHE = BUILDER
                .comment("Enable 20-tick caching for Solar / SkyLight checks in Solar Neutron Activator and Solar Generators.")
                .define("enableSolarCache", true);

        ENABLE_PUMP_BACKOFF = BUILDER
                .comment("Enable adaptive exponential backoff when Electric Pump cannot find fluid sources.")
                .define("enablePumpBackoff", true);

        ENABLE_NETWORK_EMIT_OPTIMIZATION = BUILDER
                .comment("Enable early exits and simulation reductions for Universal Cables, Mechanical Pipes, and Pressurized Tubes.")
                .define("enableNetworkEmitOptimization", true);

        ENABLE_PACKET_COALESCING = BUILDER
                .comment("Enable S2C tile update packet coalescing (Latest-Write-Wins) from MekDefender to reduce network lag.")
                .define("enablePacketCoalescing", true);

        ENABLE_OWNER_CHUNKLOAD_GATING = BUILDER
                .comment("Enable owner-aware chunkloading gating (disables Anchor Upgrades & Dimensional Stabilizers when owner is offline).")
                .define("enableOwnerChunkloadGating", true);

        ENABLE_FACTORY_AUTO_SORT_OPTIMIZATION = BUILDER
                .comment("Enable zero-allocation, ultra-fast in-place Auto-Sort for Basic/Advanced/Elite/Ultimate Factories.")
                .define("enableFactoryAutoSortOptimization", true);

        ENABLE_ADDON_OPTIMIZATIONS = BUILDER
                .comment("Enable specific optimizations for Mekanism Addons (Astral Mekanism, Evolved Mekanism).")
                .define("enableAddonOptimizations", true);

        MAX_BACKOFF_TICKS = BUILDER
                .comment("Maximum backoff ticks when a destination remains full/blocked (default: 32 ticks).")
                .defineInRange("maxBackoffTicks", 32, 2, 200);

        ENABLE_VERBOSE_LOGGING = BUILDER
                .comment("Enable verbose logging to logs/mekanism_optimizer/runtime.log")
                .define("enableVerboseLogging", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}