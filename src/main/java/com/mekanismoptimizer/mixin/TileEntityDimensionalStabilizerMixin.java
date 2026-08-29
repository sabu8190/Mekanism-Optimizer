package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.OwnerChunkloadGate;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = TileEntityDimensionalStabilizer.class, remap = false)
public abstract class TileEntityDimensionalStabilizerMixin {

    @Inject(method = "onUpdateServer", at = @At("HEAD"), cancellable = true)
    private void onUpdateServer(CallbackInfo ci) {
        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        if (!OwnerChunkloadGate.shouldAllowChunkloading(self)) {
            ci.cancel();
        }
    }
}