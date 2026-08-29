package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.tile.component.TileComponentChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = TileComponentChunkLoader.class, remap = false)
public abstract class TileComponentChunkLoaderMixin {

    @Inject(method = "canOperate", at = @At("RETURN"), cancellable = true)
    private void onCanOperate(CallbackInfoReturnable<Boolean> cir) {
        if (!MekanismOptimizerConfig.ENABLE_OWNER_CHUNKLOAD_GATING.get()) {
            return;
        }
        if (!cir.getReturnValue()) {
            return;
        }
    }
}