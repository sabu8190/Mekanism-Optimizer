package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.common.content.network.FluidNetwork;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = FluidNetwork.class, remap = false)
public abstract class FluidNetworkMixin {

    @Inject(method = "tickEmit", at = @At("HEAD"), cancellable = true)
    private void onTickEmit(FluidStack fluidToSend, CallbackInfoReturnable<Integer> cir) {
        if (!MekanismOptimizerConfig.ENABLE_NETWORK_EMIT_OPTIMIZATION.get()) {
            return;
        }
        FluidNetwork self = (FluidNetwork) (Object) this;
        if (self.getAcceptorCount() == 0 || fluidToSend.isEmpty()) {
            MekanismOptimizerLogger.recordNetworkEmitSkipped();
            cir.setReturnValue(0);
        }
    }
}