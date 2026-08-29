package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.common.tile.machine.TileEntityElectricPump;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = TileEntityElectricPump.class, remap = false)
public abstract class TileEntityElectricPumpMixin {

    @Inject(method = "suck()Z", at = @At("HEAD"), cancellable = true)
    private void onSuckHead(CallbackInfoReturnable<Boolean> cir) {
        if (!MekanismOptimizerConfig.ENABLE_PUMP_BACKOFF.get()) {
            return;
        }

        BlockEntity self = (BlockEntity) (Object) this;
        BlockPos pos = self.getBlockPos();
        long currentTick = self.getLevel() != null ? self.getLevel().getGameTime() : 0;

        if (!AdaptiveBackoffManager.shouldRun(pos, currentTick)) {
            MekanismOptimizerLogger.recordPumpThrottled();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "suck()Z", at = @At("RETURN"))
    private void onSuckReturn(CallbackInfoReturnable<Boolean> cir) {
        if (!MekanismOptimizerConfig.ENABLE_PUMP_BACKOFF.get()) {
            return;
        }

        BlockEntity self = (BlockEntity) (Object) this;
        BlockPos pos = self.getBlockPos();
        long currentTick = self.getLevel() != null ? self.getLevel().getGameTime() : 0;

        if (cir.getReturnValue()) {
            AdaptiveBackoffManager.recordSuccess(pos);
        } else {
            AdaptiveBackoffManager.recordFailure(pos, currentTick);
        }
    }
}