package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import com.mekanismoptimizer.core.PoissonSampler;
import mekanism.common.util.StatUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = StatUtils.class, remap = false)
public abstract class StatUtilsMixin {

    @Inject(method = "inversePoisson", at = @At("HEAD"), cancellable = true)
    private static void onInversePoisson(double mean, CallbackInfoReturnable<Integer> cir) {
        if (MekanismOptimizerConfig.ENABLE_FAST_POISSON_SAMPLER.get()) {
            cir.setReturnValue(PoissonSampler.sampleFast(mean));
        }
    }
}