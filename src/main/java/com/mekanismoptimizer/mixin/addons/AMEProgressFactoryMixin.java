package com.mekanismoptimizer.mixin.addons;

import com.mekanismoptimizer.core.UniversalFactoryAutoSorter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for Astral Mekanism BEEnergizedSmeltingFactory.
 * Bypasses Astral Mekanism's buggy sort() implementation that skips sorting when no empty slots exist,
 * and provides perfect, balanced item distribution.
 */
@Mixin(targets = "astral_mekanism.block.blockentity.normalfactory.BEEnergizedSmeltingFactory", remap = false)
public abstract class AMEProgressFactoryMixin {

    @Inject(method = "sort", at = @At("HEAD"), cancellable = true, remap = false)
    private void mo$onSort(CallbackInfo ci) {
        if (UniversalFactoryAutoSorter.sortAMEFactory(this)) {
            ci.cancel();
        }
    }
}
