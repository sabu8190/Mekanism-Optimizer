package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = MechanicalPipe.class, remap = false)
public abstract class MechanicalPipeMixin {

    @Shadow
    public abstract void pullFromAcceptors();

    @Unique
    private boolean mekanismOptimizer$isPulling = false;

    @Inject(method = "pullFromAcceptors", at = @At("HEAD"))
    private void onPullFromAcceptorsHead(CallbackInfo ci) {
        if (mekanismOptimizer$isPulling || !MekanismOptimizerConfig.ENABLE_FLUID_OVERCLOCK.get()) {
            return;
        }

        int burst = MekanismOptimizerConfig.FLUID_BURST_PER_TICK.get();
        if (burst > 1) {
            mekanismOptimizer$isPulling = true;
            try {
                for (int i = 1; i < burst; i++) {
                    pullFromAcceptors();
                }
            } finally {
                mekanismOptimizer$isPulling = false;
            }
        }
    }
}
