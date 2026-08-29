package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.FastFactorySorter;
import mekanism.common.tile.factory.TileEntityFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = TileEntityFactory.class, remap = false)
public abstract class TileEntityFactoryMixin {

    @Inject(method = "sortInventory", at = @At("HEAD"), cancellable = true)
    private void onSortInventory(CallbackInfo ci) {
        TileEntityFactory<?> self = (TileEntityFactory<?>) (Object) this;
        if (FastFactorySorter.sortFast(self)) {
            ci.cancel();
        }
    }
}