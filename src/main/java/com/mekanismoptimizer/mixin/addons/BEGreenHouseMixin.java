package com.mekanismoptimizer.mixin.addons;

import com.mekanismoptimizer.core.FastRecipeLookupCache;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "astral_mekanism.generalrecipe.recipe.CropSoilRecipe", remap = false)
public abstract class BEGreenHouseMixin {

    /**
     * O(1) Fast path for Astral Mekanism CropSoilRecipe emptyableTest.
     * Prevents thousands of full recipe and ingredient list traversals per tick for GreenHouse machines.
     */
    @Inject(method = "emptyableTest", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onEmptyableTest(ItemStack crop, ItemStack soil, Object recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!MekanismOptimizerConfig.ENABLE_ADDON_OPTIMIZATIONS.get()) {
            return;
        }

        if (crop != null && soil != null && !crop.isEmpty() && !soil.isEmpty()) {
            Object cached = FastRecipeLookupCache.getCropSoilRecipe(crop, soil);
            if (cached != null) {
                cir.setReturnValue(cached == recipe);
            }
        }
    }
}
