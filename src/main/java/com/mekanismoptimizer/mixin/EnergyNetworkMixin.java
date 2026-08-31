package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.transmitter.UniversalCable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = EnergyNetwork.class, remap = false)
public abstract class EnergyNetworkMixin {

    @Shadow
    public VariableCapacityEnergyContainer energyContainer;

    @Unique
    private FloatingLong mekanismOptimizer$lastSavedEnergy = null;

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

    @Inject(method = "updateSaveShares", at = @At("HEAD"), cancellable = true)
    private void onUpdateSaveShares(UniversalCable triggerTransmitter, CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_NETWORK_EMIT_OPTIMIZATION.get()) {
            return;
        }
        EnergyNetwork self = (EnergyNetwork) (Object) this;
        if (self.isEmpty() || energyContainer == null) {
            ci.cancel();
            return;
        }

        FloatingLong currentEnergy = energyContainer.getEnergy();
        if (mekanismOptimizer$lastSavedEnergy != null && mekanismOptimizer$lastSavedEnergy.equals(currentEnergy)) {
            // Steady state: energy level did not change, skip full network redistribution and tile saving
            MekanismOptimizerLogger.recordNetEmitSkip();
            ci.cancel();
            return;
        }
        mekanismOptimizer$lastSavedEnergy = currentEnergy.copy();
    }
}
