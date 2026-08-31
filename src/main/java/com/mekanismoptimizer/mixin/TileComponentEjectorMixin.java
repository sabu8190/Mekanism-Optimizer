package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private static final ThreadLocal<Boolean> mekanismOptimizer$IS_BURSTING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    private void onTickServer(CallbackInfo ci) {
        if (tile == null) {
            return;
        }

        BlockPos pos = tile.getBlockPos();
        long currentTick = tile.getLevel() != null ? tile.getLevel().getGameTime() : 0;

        if (MekanismOptimizerConfig.ENABLE_EJECTOR_OPTIMIZATION.get() && !AdaptiveBackoffManager.shouldRun(pos, currentTick)) {
            ci.cancel();
            return;
        }

        if (tickDelay > MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get()) {
            tickDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
        }

        // Multi-burst ejection engine
        if (MekanismOptimizerConfig.ENABLE_UNLIMITED_AUTO_EJECT.get() && !mekanismOptimizer$IS_BURSTING.get()) {
            int burstMultiplier = MekanismOptimizerConfig.AUTO_EJECT_BURST_MULTIPLIER.get();
            int itemMaxStacks = MekanismOptimizerConfig.ITEM_EJECT_MAX_STACKS_PER_TICK.get();

            if (burstMultiplier > 1 || itemMaxStacks > 1) {
                mekanismOptimizer$IS_BURSTING.set(true);
                try {
                    for (Map.Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
                        TransmissionType type = entry.getKey();
                        ConfigInfo info = entry.getValue();
                        if (isEjecting(info, type)) {
                            if (type == TransmissionType.ITEM) {
                                for (int i = 0; i < itemMaxStacks - 1; i++) {
                                    outputItems(info);
                                }
                            } else if (type != TransmissionType.HEAT) {
                                for (int i = 0; i < burstMultiplier - 1; i++) {
                                    eject(type, info);
                                }
                            }
                        }
                    }
                } finally {
                    mekanismOptimizer$IS_BURSTING.set(false);
                }
            }
        }
    }

    @Inject(method = "outputItems", at = @At("RETURN"))
    private void onOutputItemsReturn(ConfigInfo info, CallbackInfo ci) {
        this.tickDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
    }
}
