package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@Mixin(value = TileComponentEjector.class, remap = false)
public abstract class TileComponentEjectorMixin {

    @Shadow
    @Final
    private TileEntityMekanism tile;

    @Shadow
    private int tickDelay;

    @Shadow
    @Final
    private Map<TransmissionType, ConfigInfo> configInfo;

    @Shadow
    public abstract boolean isEjecting(ConfigInfo info, TransmissionType type);

    @Shadow
    protected abstract void eject(TransmissionType type, ConfigInfo info);

    @Shadow
    protected abstract void outputItems(ConfigInfo info);

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServerHead(CallbackInfo ci) {
        if (tile == null) {
            return;
        }

        // Clamp tick delay to configured value (0 = instant ejection every tick)
        int configuredDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
        if (tickDelay > configuredDelay) {
            tickDelay = configuredDelay;
        }

        // Multi-burst ejection for fluid, chemical and energy
        for (Map.Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();

            if (type == TransmissionType.ITEM || type == TransmissionType.HEAT || !isEjecting(info, type)) {
                continue;
            }

            int burst = 1;
            if (type == TransmissionType.FLUID && MekanismOptimizerConfig.ENABLE_FLUID_OVERCLOCK.get()) {
                burst = MekanismOptimizerConfig.FLUID_BURST_PER_TICK.get();
            } else if (type.isChemical() && MekanismOptimizerConfig.ENABLE_CHEMICAL_OVERCLOCK.get()) {
                burst = MekanismOptimizerConfig.CHEMICAL_BURST_PER_TICK.get();
            } else if (type == TransmissionType.ENERGY && MekanismOptimizerConfig.ENABLE_ENERGY_OVERCLOCK.get()) {
                burst = MekanismOptimizerConfig.ENERGY_BURST_PER_TICK.get();
            }

            if (burst > 1) {
                for (int i = 0; i < burst - 1; i++) {
                    eject(type, info);
                }
            }
        }
    }

    /**
     * Multi-burst auto-ejection for ITEMS when overclocking is enabled.
     * Native outputItems handles the first ejection. This loop performs subsequent burst ejections safely.
     */
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void onTickServerTail(CallbackInfo ci) {
        if (tile == null || !MekanismOptimizerConfig.ENABLE_ITEM_OVERCLOCK.get()) {
            return;
        }

        int maxBurst = MekanismOptimizerConfig.ITEM_BURST_PER_TICK.get();
        if (maxBurst <= 1) {
            return;
        }

        ConfigInfo itemConfig = configInfo.get(TransmissionType.ITEM);
        if (itemConfig != null && isEjecting(itemConfig, TransmissionType.ITEM)) {
            for (int i = 1; i < maxBurst; i++) {
                outputItems(itemConfig);
            }
        }
    }

    /**
     * After outputItems completes, immediately reset tickDelay to configured value (0)
     * so that the machine never waits 10 ticks (0.5s) between ejections.
     */
    @Inject(method = "outputItems", at = @At("TAIL"))
    private void onOutputItemsTail(ConfigInfo info, CallbackInfo ci) {
        this.tickDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
    }
}
