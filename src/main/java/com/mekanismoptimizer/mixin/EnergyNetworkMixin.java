package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.network.EnergyNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = EnergyNetwork.class, remap = false)
public abstract class EnergyNetworkMixin {

    @Inject(method = "tickEmit", at = @At("HEAD"), cancellable = true)
    private void onTickEmit(FloatingLong energyToSend, CallbackInfoReturnable<FloatingLong> cir) {
        if (!MekanismOptimizerConfig.ENABLE_NETWORK_EMIT_OPTIMIZATION.get()) {
            return;
        }
        EnergyNetwork self = (EnergyNetwork) (Object) this;
        if (self.getAcceptorCount() == 0 || energyToSend.isZero()) {
            MekanismOptimizerLogger.recordNetworkEmitSkipped();
            cir.setReturnValue(FloatingLong.ZERO);
        }
    }
}