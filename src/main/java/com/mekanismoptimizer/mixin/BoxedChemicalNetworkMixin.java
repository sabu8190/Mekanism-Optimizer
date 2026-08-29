package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.content.network.BoxedChemicalNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BoxedChemicalNetwork.class, remap = false)
public abstract class BoxedChemicalNetworkMixin {

    @Inject(method = "tickEmit(Lmekanism/api/chemical/ChemicalStack;)J", at = @At("HEAD"), cancellable = true)
    private void onTickEmit(ChemicalStack<?> stack, CallbackInfoReturnable<Long> cir) {
        if (!MekanismOptimizerConfig.ENABLE_NETWORK_EMIT_OPTIMIZATION.get()) {
            return;
        }
        BoxedChemicalNetwork self = (BoxedChemicalNetwork) (Object) this;
        if (self.getAcceptorCount() == 0 || stack == null || stack.isEmpty()) {
            MekanismOptimizerLogger.recordNetworkEmitSkipped();
            cir.setReturnValue(0L);
        }
    }
}