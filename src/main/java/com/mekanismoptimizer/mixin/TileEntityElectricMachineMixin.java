package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.FastRecipeLookupCache;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = TileEntityElectricMachine.class, remap = false)
public abstract class TileEntityElectricMachineMixin {

    @Shadow
    InputInventorySlot inputSlot;

    @Shadow
    OutputInventorySlot outputSlot;

    /**
     * O(1) Fast recipe lookup cache check for all single-item electric machines (Smelter, Crusher, Enrichment, etc.)
     */
    @Inject(method = "getRecipe", at = @At("HEAD"), cancellable = true)
    private void onGetRecipeHead(int cacheIndex, CallbackInfoReturnable<ItemStackToItemStackRecipe> cir) {
        if (!MekanismOptimizerConfig.ENABLE_ADDON_OPTIMIZATIONS.get()) {
            return;
        }

        if (inputSlot != null && !inputSlot.isEmpty()) {
            ItemStack stack = inputSlot.getStack();
            ItemStackToItemStackRecipe cached = FastRecipeLookupCache.getSingleItemRecipe(stack);
            if (cached != null) {
                cir.setReturnValue(cached);
            }
        }
    }

    /**
     * Cache the resolved recipe on return.
     */
    @Inject(method = "getRecipe", at = @At("RETURN"))
    private void onGetRecipeReturn(int cacheIndex, CallbackInfoReturnable<ItemStackToItemStackRecipe> cir) {
        if (!MekanismOptimizerConfig.ENABLE_ADDON_OPTIMIZATIONS.get()) {
            return;
        }

        if (inputSlot != null && !inputSlot.isEmpty()) {
            ItemStack stack = inputSlot.getStack();
            FastRecipeLookupCache.putSingleItemRecipe(stack, cir.getReturnValue());
        }
    }

    /**
     * O(1) Fast early return for completely empty or stalled electric machines (Energized Smelter, Crusher, etc.)
     * under Torcherino / Tick acceleration.
     */
    @Inject(method = "onUpdateServer", at = @At("HEAD"), cancellable = true)
    private void onUpdateServerElectricAccelerated(CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_ADDON_OPTIMIZATIONS.get()) {
            return;
        }

        TileEntityElectricMachine self = (TileEntityElectricMachine) (Object) this;

        // If input is empty and machine is not active, skip recipe and energy checking
        if (inputSlot != null && inputSlot.isEmpty() && !self.getActive()) {
            ci.cancel();
            return;
        }

        // If output slot is completely full (max stack reached) and machine is not active, skip
        if (outputSlot != null && !outputSlot.isEmpty() && !self.getActive()) {
            ItemStack outStack = outputSlot.getStack();
            if (outStack.getCount() >= outStack.getMaxStackSize()) {
                ci.cancel();
            }
        }
    }
}
