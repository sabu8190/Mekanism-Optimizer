package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.UniversalFactoryAutoSorter;
import mekanism.common.tile.factory.TileEntityFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityFactory.class, remap = false)
public abstract class TileEntityFactoryMixin {

    @Inject(method = "sortInventory", at = @At("HEAD"), cancellable = true, remap = false)
    private void onSortInventory(CallbackInfo ci) {
        TileEntityFactory<?> self = (TileEntityFactory<?>) (Object) this;
        if (UniversalFactoryAutoSorter.sortMekanismFactory(self)) {
            ci.cancel();
        }
    }
}
