package com.mekanismoptimizer.mixin.generators;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "mekanism.generators.client.render.RenderWindGenerator", remap = false)
public abstract class RenderWindGeneratorMixin {

    /**
     * Enables frustum culling for Wind Generators by overriding shouldRenderOffScreen to false.
     * Prevents rendering 21 model parts and doing matrix rotations for windmills outside camera view.
     */
    @Inject(method = "shouldRenderOffScreen", at = @At("HEAD"), cancellable = true)
    private void shouldRenderOffScreenOptimized(Object tile, CallbackInfoReturnable<Boolean> cir) {
        if (MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_RENDER_CULLING.get()) {
            cir.setReturnValue(false);
        }
    }
}
