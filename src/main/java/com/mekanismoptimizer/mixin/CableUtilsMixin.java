package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.CableUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Pseudo
@Mixin(value = CableUtils.class, remap = false)
public abstract class CableUtilsMixin {

    @Inject(method = "emit(Ljava/util/Set;Lmekanism/api/math/FloatingLong;Lnet/minecraft/world/level/block/entity/BlockEntity;)Lmekanism/api/math/FloatingLong;", at = @At("HEAD"), cancellable = true)
    private static void onEmit(Set<Direction> sides, FloatingLong energyToSend, BlockEntity from, CallbackInfoReturnable<FloatingLong> cir) {
        if (!MekanismOptimizerConfig.ENABLE_CABLE_BACKOFF.get() || from == null || energyToSend == null || energyToSend.isZero() || sides == null || sides.isEmpty()) {
            return;
        }
        if (AdaptiveBackoffManager.shouldBackoff(from)) {
            MekanismOptimizerLogger.recordCableBackoffSkipped();
            cir.setReturnValue(FloatingLong.ZERO);
        }
    }

    @Inject(method = "emit(Ljava/util/Set;Lmekanism/api/math/FloatingLong;Lnet/minecraft/world/level/block/entity/BlockEntity;)Lmekanism/api/math/FloatingLong;", at = @At("RETURN"))
    private static void onEmitReturn(Set<Direction> sides, FloatingLong energyToSend, BlockEntity from, CallbackInfoReturnable<FloatingLong> cir) {
        if (!MekanismOptimizerConfig.ENABLE_CABLE_BACKOFF.get() || from == null) {
            return;
        }
        FloatingLong result = cir.getReturnValue();
        if (result == null || result.isZero()) {
            AdaptiveBackoffManager.recordFailure(from);
        } else {
            AdaptiveBackoffManager.reset(from);
        }
    }
}
