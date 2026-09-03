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

        // Clamp tick delay to configured value (default 0 for instant responsive ejection)
        int configuredDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
        if (tickDelay > configuredDelay) {
            tickDelay = configuredDelay;
        }
    }

    /**
     * Resets tickDelay after item ejection to eliminate hardcoded 10-tick (0.5s) lag.
     */
    @Inject(method = "outputItems", at = @At("TAIL"))
    private void onOutputItemsTail(ConfigInfo info, CallbackInfo ci) {
        this.tickDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
    }
}
