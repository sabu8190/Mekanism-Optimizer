package com.mekanismoptimizer;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import com.mekanismoptimizer.core.PacketCoalescer;
import com.mekanismoptimizer.core.ParallelWorkerPool;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismOptimizer.MODID)
public class MekanismOptimizer {
    public static final String MODID = "mekanism_optimizer";
    private static long currentServerTick = 0;

    public MekanismOptimizer() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MekanismOptimizerConfig.SPEC, "mekanism_optimizer-common.toml");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        MekanismOptimizerLogger.info("Mekanism Optimizer initialized with " + ParallelWorkerPool.getThreadCount() + " worker threads.");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            currentServerTick++;

            // Flush coalesced packets to clients safely at tick boundary
            PacketCoalescer.flushPendingUpdates();

            if (currentServerTick % 1200 == 0) { // Every 1 minute
                MekanismOptimizerLogger.info(MekanismOptimizerLogger.getMetricsSummary());
            }
        }
    }

    public static long getCurrentServerTick() {
        return currentServerTick;
    }
}