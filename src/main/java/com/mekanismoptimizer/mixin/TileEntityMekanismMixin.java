package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.CobblestoneSupplyEngine;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityMekanism.class, priority = 100, remap = false)
public abstract class TileEntityMekanismMixin {

    @Inject(method = "onUpdateServer", at = @At("HEAD"), remap = false)
    private void mo$onUpdateServer(CallbackInfo ci) {
        CobblestoneSupplyEngine.tick((TileEntityMekanism) (Object) this);
    }
}
