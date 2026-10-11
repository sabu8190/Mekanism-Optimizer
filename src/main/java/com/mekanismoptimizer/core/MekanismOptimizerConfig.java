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

    // Wind Generator & Machine Rendering Optimizations
    public static final ForgeConfigSpec.BooleanValue ENABLE_WIND_GENERATOR_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WIND_GENERATOR_RENDER_CULLING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WIND_GENERATOR_PREBAKED_MESHES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WIND_GENERATOR_DIRECT_RENDERING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WIND_GENERATOR_DISTANCE_LOD;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WIND_GENERATOR_ANGLE_THROTTLING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TRANSMITTER_RENDER_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FLUID_TANK_RENDER_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ENERGY_CUBE_RENDER_OPTIMIZATION;

    // Dedicated File Logger
    public static final ForgeConfigSpec.BooleanValue ENABLE_DEDICATED_LOG_FILE;

    public static final ForgeConfigSpec.IntValue ITEM_EJECT_TICK_DELAY;
    public static final ForgeConfigSpec.IntValue MAX_BACKOFF_TICKS;
    public static final ForgeConfigSpec.IntValue ADAPTIVE_BACKOFF_MAX_TICKS;
    public static final ForgeConfigSpec.IntValue SOLAR_LIGHT_CACHE_TTL_TICKS;
    public static final ForgeConfigSpec.IntValue DYNAMIC_NETWORK_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue LOG_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.IntValue WIND_GENERATOR_SKY_CHECK_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue ENERGY_CUBE_POOL_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue WIND_GENERATOR_LOD1_DISTANCE_BLOCKS;
    public static final ForgeConfigSpec.DoubleValue WIND_GENERATOR_LOD2_DISTANCE_BLOCKS;
    public static final ForgeConfigSpec.DoubleValue WIND_GENERATOR_CULL_DISTANCE_BLOCKS;
    public static final ForgeConfigSpec.DoubleValue WIND_GENERATOR_MID_QUANTIZE_DEGREES;
    public static final ForgeConfigSpec.DoubleValue WIND_GENERATOR_FAR_QUANTIZE_DEGREES;

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

        ENABLE_WIND_GENERATOR_OPTIMIZATION = builder
                .comment("Enable O(1) height multiplier caching and adaptive skylight checks for Wind Generators to eliminate server tick allocations.")
                .translation("mekanism_optimizer.config.enableWindGeneratorOptimization")
                .define("enableWindGeneratorOptimization", true);

        ENABLE_WIND_GENERATOR_RENDER_CULLING = builder
                .comment("Enable frustum culling for Wind Generators to skip rendering and matrix math for off-screen/backside models.")
                .translation("mekanism_optimizer.config.enableWindGeneratorRenderCulling")
                .define("enableWindGeneratorRenderCulling", true);

        ENABLE_WIND_GENERATOR_PREBAKED_MESHES = builder
                .comment("Enable pre-baked static mast/body and rigid rotor meshes for Wind Generators (reduces 21 matrix transforms per frame to 2).")
                .translation("mekanism_optimizer.config.enableWindGeneratorPrebakedMeshes")
                .define("enableWindGeneratorPrebakedMeshes", true);

        ENABLE_WIND_GENERATOR_DIRECT_RENDERING = builder
                .comment("Enable zero-allocation direct rendering for Wind Generators to eliminate per-frame capturing lambda allocations.")
                .translation("mekanism_optimizer.config.enableWindGeneratorDirectRendering")
                .define("enableWindGeneratorDirectRendering", true);

        ENABLE_WIND_GENERATOR_DISTANCE_LOD = builder
                .comment("Enable 3-tier distance-adaptive Level of Detail (LOD) and far-distance culling for Wind Generators.")
                .translation("mekanism_optimizer.config.enableWindGeneratorDistanceLod")
                .define("enableWindGeneratorDistanceLod", true);

        ENABLE_WIND_GENERATOR_ANGLE_THROTTLING = builder
                .comment("Enable rotation angle quantization/throttling for medium and far distance Wind Generators.")
                .translation("mekanism_optimizer.config.enableWindGeneratorAngleThrottling")
                .define("enableWindGeneratorAngleThrottling", true);

        ENABLE_TRANSMITTER_RENDER_OPTIMIZATION = builder
                .comment("Enable zero-allocation model caching for pipes, cables, and tubes to eliminate stream/string allocations per frame.")
                .translation("mekanism_optimizer.config.enableTransmitterRenderOptimization")
                .define("enableTransmitterRenderOptimization", true);

        ENABLE_FLUID_TANK_RENDER_OPTIMIZATION = builder
                .comment("Enable fluid tank rendering cache and lookup optimizations.")
                .translation("mekanism_optimizer.config.enableFluidTankRenderOptimization")
                .define("enableFluidTankRenderOptimization", true);

        ENABLE_ENERGY_CUBE_RENDER_OPTIMIZATION = builder
                .comment("Enable energy cube rendering optimizations to reduce per-frame object allocations.")
                .translation("mekanism_optimizer.config.enableEnergyCubeRenderOptimization")
                .define("enableEnergyCubeRenderOptimization", true);

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

        WIND_GENERATOR_SKY_CHECK_INTERVAL_TICKS = builder
                .comment("Interval in ticks between skylight occlusion checks for Wind Generators (default 100 ticks = 5s).")
                .translation("mekanism_optimizer.config.windGeneratorSkyCheckIntervalTicks")
                .defineInRange("windGeneratorSkyCheckIntervalTicks", 100, 10, 1200);

        ENERGY_CUBE_POOL_CAPACITY = builder
                .comment("Maximum capacity for pooled Energy Cube core LazyRender instances.")
                .translation("mekanism_optimizer.config.energyCubePoolCapacity")
                .defineInRange("energyCubePoolCapacity", 256, 16, 1024);

        WIND_GENERATOR_LOD1_DISTANCE_BLOCKS = builder
                .comment("Distance threshold in blocks for switching Wind Generators to LOD 1 (medium detail, omitting sub-pixel wires/plates/secondary blades).")
                .translation("mekanism_optimizer.config.windGeneratorLod1DistanceBlocks")
                .defineInRange("windGeneratorLod1DistanceBlocks", 24.0D, 8.0D, 128.0D);

        WIND_GENERATOR_LOD2_DISTANCE_BLOCKS = builder
                .comment("Distance threshold in blocks for switching Wind Generators to LOD 2 (far detail, core mast and primary 3 blades only).")
                .translation("mekanism_optimizer.config.windGeneratorLod2DistanceBlocks")
                .defineInRange("windGeneratorLod2DistanceBlocks", 48.0D, 16.0D, 192.0D);

        WIND_GENERATOR_CULL_DISTANCE_BLOCKS = builder
                .comment("Maximum distance in blocks beyond which Wind Generator rendering is completely culled.")
                .translation("mekanism_optimizer.config.windGeneratorCullDistanceBlocks")
                .defineInRange("windGeneratorCullDistanceBlocks", 80.0D, 16.0D, 256.0D);

        WIND_GENERATOR_MID_QUANTIZE_DEGREES = builder
                .comment("Rotation angle quantization step in degrees for medium-distance (LOD 1) Wind Generators.")
                .translation("mekanism_optimizer.config.windGeneratorMidQuantizeDegrees")
                .defineInRange("windGeneratorMidQuantizeDegrees", 8.0D, 0.25D, 45.0D);

        WIND_GENERATOR_FAR_QUANTIZE_DEGREES = builder
                .comment("Rotation angle quantization step in degrees for far-distance (LOD 2) Wind Generators.")
                .translation("mekanism_optimizer.config.windGeneratorFarQuantizeDegrees")
                .defineInRange("windGeneratorFarQuantizeDegrees", 12.0D, 0.25D, 90.0D);

        builder.pop();
        SPEC = builder.build();
    }

    private MekanismOptimizerConfig() {
    }
}
