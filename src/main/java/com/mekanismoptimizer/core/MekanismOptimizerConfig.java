package com.mekanismoptimizer.core;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MekanismOptimizerConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_INVENTORY_ACCEPTOR_CACHE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ACCEPTOR_CACHE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TRANSPORTER_CACHE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FAST_POISSON_SAMPLER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ADAPTIVE_BACKOFF;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EJECTOR_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SOLAR_LIGHT_CACHE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SOLAR_CACHE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ELECTRIC_PUMP_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PUMP_BACKOFF;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DYNAMIC_NETWORK_COOLDOWN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_NETWORK_EMIT_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FAST_SIMULATE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PACKET_COALESCING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_OWNER_CHUNKLOAD_GATING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FACTORY_AUTO_SORT_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MULTIBLOCK_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ADDON_OPTIMIZATIONS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_RADIATION_FAST_PATH;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CABLE_BACKOFF;

    public static final ForgeConfigSpec.IntValue MAX_BACKOFF_TICKS;
    public static final ForgeConfigSpec.IntValue ADAPTIVE_BACKOFF_MAX_TICKS;
    public static final ForgeConfigSpec.IntValue SOLAR_LIGHT_CACHE_TTL_TICKS;
    public static final ForgeConfigSpec.IntValue DYNAMIC_NETWORK_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue LOG_INTERVAL_SECONDS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Mekanism Optimizer General Settings").push("General Settings");

        ENABLE_INVENTORY_ACCEPTOR_CACHE = builder
                .comment("Enable caching of inventory acceptors to eliminate repeated chunk/capability lookups during item routing.")
                .define("enableInventoryAcceptorCache", true);
        ENABLE_ACCEPTOR_CACHE = ENABLE_INVENTORY_ACCEPTOR_CACHE;

        ENABLE_TRANSPORTER_CACHE = builder
                .comment("Enable caching of transporter paths and destination lookups.")
                .define("enableTransporterCache", true);

        ENABLE_FAST_POISSON_SAMPLER = builder
                .comment("Enable O(1) fast Poisson distribution sampling to replace iterative loops.")
                .define("enableFastPoissonSampler", true);

        ENABLE_ADAPTIVE_BACKOFF = builder
                .comment("Enable adaptive tick backoff when ejectors or pipes encounter blocked targets.")
                .define("enableAdaptiveBackoff", true);
        ENABLE_EJECTOR_OPTIMIZATION = ENABLE_ADAPTIVE_BACKOFF;

        ENABLE_SOLAR_LIGHT_CACHE = builder
                .comment("Enable caching of skylight checks for Solar Neutron Activator and solar machines.")
                .define("enableSolarLightCache", true);
        ENABLE_SOLAR_CACHE = ENABLE_SOLAR_LIGHT_CACHE;

        ENABLE_ELECTRIC_PUMP_OPTIMIZATION = builder
                .comment("Enable fast-path liquid fluid checks for Electric Pumps.")
                .define("enableElectricPumpOptimization", true);
        ENABLE_PUMP_BACKOFF = ENABLE_ELECTRIC_PUMP_OPTIMIZATION;

        ENABLE_DYNAMIC_NETWORK_COOLDOWN = builder
                .comment("Enable dynamic emit cooldown for empty or stalled energy/chemical/fluid networks.")
                .define("enableDynamicNetworkCooldown", true);
        ENABLE_NETWORK_EMIT_OPTIMIZATION = ENABLE_DYNAMIC_NETWORK_COOLDOWN;

        ENABLE_FAST_SIMULATE = builder
                .comment("Enable fast simulated item insertion without full capability recreation.")
                .define("enableFastSimulate", true);

        ENABLE_PACKET_COALESCING = builder
                .comment("Enable coalescing of tile entity update packets to reduce network bandwidth and client lag (MekDefender).")
                .define("enablePacketCoalescing", true);

        ENABLE_OWNER_CHUNKLOAD_GATING = builder
                .comment("Enable sleeping anchor upgrades and dimensional stabilizers when the owner is offline (MekDefender).")
                .define("enableOwnerChunkloadGating", true);

        ENABLE_FACTORY_AUTO_SORT_OPTIMIZATION = builder
                .comment("Enable ultra-fast, zero-allocation auto-sort item balancing for Mekanism and Addon (AME/Extras) factories.")
                .define("enableFactoryAutoSortOptimization", true);

        ENABLE_MULTIBLOCK_OPTIMIZATION = builder
                .comment("Enable adaptive tick caching for massive multiblocks (Turbine, Fusion Reactor, etc.) during steady states.")
                .define("enableMultiblockOptimization", true);

        ENABLE_ADDON_OPTIMIZATIONS = builder
                .comment("Enable special performance optimizations for Mekanism Addons (Astral Mekanism, Evolved Mekanism, Mekanism Extras).")
                .define("enableAddonOptimizations", true);

        ENABLE_RADIATION_FAST_PATH = builder
                .comment("Enable O(1) instant bypass for living entity radiation calculations when world has no active radiation.")
                .define("enableRadiationFastPath", true);

        ENABLE_CABLE_BACKOFF = builder
                .comment("Enable adaptive backoff for universal cables and energy cubes when targets are fully charged.")
                .define("enableCableBackoff", true);

        MAX_BACKOFF_TICKS = builder
                .comment("Maximum ticks to back off when an ejection target is continuously blocked.")
                .defineInRange("maxBackoffTicks", 20, 1, 100);
        ADAPTIVE_BACKOFF_MAX_TICKS = MAX_BACKOFF_TICKS;

        SOLAR_LIGHT_CACHE_TTL_TICKS = builder
                .comment("Time-to-live in ticks for cached skylight visibility.")
                .defineInRange("solarLightCacheTtlTicks", 40, 5, 200);

        DYNAMIC_NETWORK_COOLDOWN_TICKS = builder
                .comment("Cooldown ticks before re-checking stalled networks.")
                .defineInRange("dynamicNetworkCooldownTicks", 10, 1, 60);

        LOG_INTERVAL_SECONDS = builder
                .comment("Interval in seconds for performance statistics logging (0 to disable).")
                .defineInRange("logIntervalSeconds", 60, 0, 3600);

        builder.pop();
        SPEC = builder.build();
    }

    private MekanismOptimizerConfig() {
    }
}
