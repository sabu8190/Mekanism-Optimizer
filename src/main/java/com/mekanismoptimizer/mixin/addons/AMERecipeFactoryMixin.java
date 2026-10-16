package com.mekanismoptimizer.mixin.addons;

import com.mekanismoptimizer.core.UniversalFactoryAutoSorter;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for Astral Mekanism BlockEntityRecipeFactory.
 * Enables auto-sorting for high-tier Astral Mekanism factories (like BEAstralEnergizedSmeltingFactory)
 * that lack sorting implementations in Astral Mekanism.
 */
@Mixin(targets = "astral_mekanism.block.blockentity.base.BlockEntityRecipeFactory", remap = false)
public abstract class AMERecipeFactoryMixin {

    @Inject(method = "onUpdateServer", at = @At("HEAD"), remap = false)
    private void mo$onUpdateServer(CallbackInfo ci) {
        if ((Object) this instanceof TileEntityMekanism tile && tile.getLevel() != null) {
            if (tile.getLevel().getGameTime() % 10 == 0) {
                UniversalFactoryAutoSorter.sortAMEFactory(this);
            }
        }
    }
}
