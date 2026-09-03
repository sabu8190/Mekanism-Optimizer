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
    public static final ForgeConfigSpec.BooleanValue ENABLE_LOOKING_AT_CACHE;

    // Dedicated File Logger
    public static final ForgeConfigSpec.BooleanValue ENABLE_DEDICATED_LOG_FILE;

    public static final ForgeConfigSpec.IntValue ITEM_EJECT_TICK_DELAY;
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
                .translation("mekanism_optimizer.config.enableInventoryAcceptorCache")
                .define("enableInventoryAcceptorCache", true);
        ENABLE_ACCEPTOR_CACHE = ENABLE_INVENTORY_ACCEPTOR_CACHE;

        ENABLE_TRANSPORTER_CACHE = builder
                .comment("Enable caching of transporter paths and destination lookups.")
                .translation("mekanism_optimizer.config.enableTransporterCache")
                .define("enableTransporterCache", true);

        ENABLE_FAST_POISSON_SAMPLER = builder
                .comment("Enable O(1) fast Poisson distribution sampling to replace iterative loops.")
                .translation("mekanism_optimizer.config.enableFastPoissonSampler")
                .define("enableFastPoissonSampler", true);

        ENABLE_ADAPTIVE_BACKOFF = builder
                .comment("Enable adaptive tick backoff when ejectors or pipes encounter blocked targets.")
                .translation("mekanism_optimizer.config.enableAdaptiveBackoff")
                .define("enableAdaptiveBackoff", true);
        ENABLE_EJECTOR_OPTIMIZATION = ENABLE_ADAPTIVE_BACKOFF;

        ENABLE_SOLAR_LIGHT_CACHE = builder
                .comment("Enable caching of skylight checks for Solar Neutron Activator and solar machines.")
                .translation("mekanism_optimizer.config.enableSolarLightCache")
                .define("enableSolarLightCache", true);
        ENABLE_SOLAR_CACHE = ENABLE_SOLAR_LIGHT_CACHE;

        ENABLE_ELECTRIC_PUMP_OPTIMIZATION = builder
                .comment("Enable fast-path liquid fluid checks for Electric Pumps.")
                .translation("mekanism_optimizer.config.enableElectricPumpOptimization")
                .define("enableElectricPumpOptimization", true);
        ENABLE_PUMP_BACKOFF = ENABLE_ELECTRIC_PUMP_OPTIMIZATION;

        ENABLE_DYNAMIC_NETWORK_COOLDOWN = builder
                .comment("Enable dynamic emit cooldown for empty or stalled energy/chemical/fluid networks.")
                .translation("mekanism_optimizer.config.enableDynamicNetworkCooldown")
                .define("enableDynamicNetworkCooldown", true);
        ENABLE_NETWORK_EMIT_OPTIMIZATION = ENABLE_DYNAMIC_NETWORK_COOLDOWN;

        ENABLE_FAST_SIMULATE = builder
                .comment("Enable fast simulated item insertion without full capability recreation.")
                .translation("mekanism_optimizer.config.enableFastSimulate")
                .define("enableFastSimulate", true);

        ENABLE_PACKET_COALESCING = builder
                .comment("Enable coalescing of tile entity update packets to reduce network bandwidth and client lag (MekDefender).")
                .translation("mekanism_optimizer.config.enablePacketCoalescing")
                .define("enablePacketCoalescing", true);

        ENABLE_OWNER_CHUNKLOAD_GATING = builder
                .comment("Enable sleeping anchor upgrades and dimensional stabilizers when the owner is offline (MekDefender).")
                .translation("mekanism_optimizer.config.enableOwnerChunkloadGating")
                .define("enableOwnerChunkloadGating", true);

        ENABLE_FACTORY_AUTO_SORT_OPTIMIZATION = builder
                .comment("Enable ultra-fast, zero-allocation auto-sort item balancing for Mekanism and Addon (AME/Extras) factories.")
                .translation("mekanism_optimizer.config.enableFactoryAutoSortOptimization")
                .define("enableFactoryAutoSortOptimization", true);

        ENABLE_MULTIBLOCK_OPTIMIZATION = builder
                .comment("Enable adaptive tick caching for massive multiblocks (Turbine, Fusion Reactor, etc.) during steady states.")
                .translation("mekanism_optimizer.config.enableMultiblockOptimization")
                .define("enableMultiblockOptimization", true);

        ENABLE_ADDON_OPTIMIZATIONS = builder
                .comment("Enable special performance optimizations for Mekanism Addons (Astral Mekanism, Evolved Mekanism, Mekanism Extras).")
                .translation("mekanism_optimizer.config.enableAddonOptimizations")
                .define("enableAddonOptimizations", true);

        ENABLE_RADIATION_FAST_PATH = builder
                .comment("Enable O(1) instant bypass for living entity radiation calculations when world has no active radiation.")
                .translation("mekanism_optimizer.config.enableRadiationFastPath")
                .define("enableRadiationFastPath", true);

        ENABLE_CABLE_BACKOFF = builder
                .comment("Enable adaptive backoff for universal cables and energy cubes when targets are fully charged.")
                .translation("mekanism_optimizer.config.enableCableBackoff")
                .define("enableCableBackoff", true);

        ENABLE_LOOKING_AT_CACHE = builder
                .comment("Enable unit display and HUD tooltip formatting cache to eliminate repeated string allocations.")
                .translation("mekanism_optimizer.config.enableLookingAtCache")
                .define("enableLookingAtCache", true);

        ENABLE_DEDICATED_LOG_FILE = builder
                .comment("Enable writing dedicated optimization and performance logs to logs/mekanism_optimizer.log.")
                .translation("mekanism_optimizer.config.enableDedicatedLogFile")
                .define("enableDedicatedLogFile", true);

        ITEM_EJECT_TICK_DELAY = builder
                .comment("Tick delay between item auto-ejection cycles (0 = eject every tick, vanilla Mekanism default is 10).")
                .translation("mekanism_optimizer.config.itemEjectTickDelay")
                .defineInRange("itemEjectTickDelay", 0, 0, 20);

        builder.pop();

        builder.comment("Advanced Tuning").push("Advanced Tuning");

        MAX_BACKOFF_TICKS = builder
                .comment("Maximum ticks to back off when an ejection target is continuously blocked.")
                .translation("mekanism_optimizer.config.maxBackoffTicks")
                .defineInRange("maxBackoffTicks", 20, 1, 100);
        ADAPTIVE_BACKOFF_MAX_TICKS = MAX_BACKOFF_TICKS;

        SOLAR_LIGHT_CACHE_TTL_TICKS = builder
                .comment("Time-to-live in ticks for cached skylight visibility.")
                .translation("mekanism_optimizer.config.solarLightCacheTtlTicks")
                .defineInRange("solarLightCacheTtlTicks", 40, 5, 200);

        DYNAMIC_NETWORK_COOLDOWN_TICKS = builder
                .comment("Cooldown ticks before re-checking stalled networks.")
                .translation("mekanism_optimizer.config.dynamicNetworkCooldownTicks")
                .defineInRange("dynamicNetworkCooldownTicks", 10, 1, 60);

        LOG_INTERVAL_SECONDS = builder
                .comment("Interval in seconds for performance statistics logging (0 to disable).")
                .translation("mekanism_optimizer.config.logIntervalSeconds")
                .defineInRange("logIntervalSeconds", 60, 0, 3600);

        builder.pop();
        SPEC = builder.build();
    }

    private MekanismOptimizerConfig() {
    }
}
