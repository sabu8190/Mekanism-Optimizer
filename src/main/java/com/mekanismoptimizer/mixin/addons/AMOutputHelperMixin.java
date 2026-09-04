package com.mekanismoptimizer.mixin.addons;

import astral_mekanism.recipes.output.AMOutputHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = AMOutputHelper.class, remap = false)
public abstract class AMOutputHelperMixin {

    /**
     * Optimized O(1) zero-allocation operation calculation for Inventory Slots
     */
    @Inject(method = "calculateOperationsCanSupport(Lmekanism/api/recipes/cache/CachedRecipe$OperationTracker;Lmekanism/api/recipes/cache/CachedRecipe$OperationTracker$RecipeError;Lmekanism/api/inventory/IInventorySlot;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onCalculateOperationsItem(OperationTracker tracker, RecipeError notEnoughSpace, IInventorySlot slot, ItemStack toOutput, CallbackInfo ci) {
        if (toOutput.isEmpty()) {
            ci.cancel();
            return;
        }

        ItemStack current = slot.getStack();
        int outputCount = toOutput.getCount();
        if (outputCount <= 0) {
            ci.cancel();
            return;
        }

        int limit = slot.getLimit(toOutput);
        if (limit <= 0) {
            tracker.updateOperations(0);
            tracker.addError(notEnoughSpace);
            ci.cancel();
            return;
        }

        if (current.isEmpty()) {
            int operations = limit / outputCount;
            tracker.updateOperations(operations);
            if (operations == 0) {
                tracker.addError(notEnoughSpace);
            }
            ci.cancel();
            return;
        }

        if (!ItemHandlerHelper.canItemStacksStack(current, toOutput)) {
            tracker.updateOperations(0);
            tracker.addError(notEnoughSpace);
            ci.cancel();
            return;
        }

        int currentCount = current.getCount();
        int space = limit - currentCount;
        if (space <= 0) {
            tracker.updateOperations(0);
            tracker.addError(notEnoughSpace);
            ci.cancel();
            return;
        }

        int operations = space / outputCount;
        tracker.updateOperations(operations);
        if (operations == 0) {
            tracker.addError(notEnoughSpace);
        }
        ci.cancel();
    }

    /**
     * Optimized O(1) zero-allocation operation calculation for Fluid Tanks
     */
    @Inject(method = "calculateOperationsCanSupport(Lmekanism/api/recipes/cache/CachedRecipe$OperationTracker;Lmekanism/api/recipes/cache/CachedRecipe$OperationTracker$RecipeError;Lmekanism/api/fluid/IExtendedFluidTank;Lnet/minecraftforge/fluids/FluidStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onCalculateOperationsFluid(OperationTracker tracker, RecipeError notEnoughSpace, IExtendedFluidTank tank, FluidStack toOutput, CallbackInfo ci) {
        if (toOutput.isEmpty()) {
            ci.cancel();
            return;
        }

        int outputAmount = toOutput.getAmount();
        if (outputAmount <= 0) {
            ci.cancel();
            return;
        }

        int needed = tank.getNeeded();
        if (needed <= 0 || !tank.isFluidValid(toOutput)) {
            tracker.updateOperations(0);
            tracker.addError(notEnoughSpace);
            ci.cancel();
            return;
        }

        FluidStack current = tank.getFluid();
        if (!current.isEmpty() && !current.isFluidEqual(toOutput)) {
            tracker.updateOperations(0);
            tracker.addError(notEnoughSpace);
            ci.cancel();
            return;
        }

        int operations = needed / outputAmount;
        tracker.updateOperations(operations);
        if (operations == 0) {
            tracker.addError(notEnoughSpace);
        }
        ci.cancel();
    }

    /**
     * Optimized O(1) zero-allocation operation calculation for Chemical Tanks
     */
    @Inject(method = "calculateOperationsCanSupport(Lmekanism/api/recipes/cache/CachedRecipe$OperationTracker;Lmekanism/api/recipes/cache/CachedRecipe$OperationTracker$RecipeError;Lmekanism/api/chemical/IChemicalTank;Lmekanism/api/chemical/ChemicalStack;)V", at = @At("HEAD"), cancellable = true)
    private static <STACK extends ChemicalStack<?>> void onCalculateOperationsChemical(OperationTracker tracker, RecipeError notEnoughSpace, IChemicalTank<?, STACK> tank, STACK toOutput, CallbackInfo ci) {
        if (toOutput.isEmpty()) {
            ci.cancel();
            return;
        }

        long outputAmount = toOutput.getAmount();
        if (outputAmount <= 0) {
            ci.cancel();
            return;
        }

        long needed = tank.getNeeded();
        if (needed <= 0 || !tank.isValid(toOutput)) {
            tracker.updateOperations(0);
            tracker.addError(notEnoughSpace);
            ci.cancel();
            return;
        }

        STACK current = tank.getStack();
        if (!current.isEmpty() && current.getType() != toOutput.getType()) {
            tracker.updateOperations(0);
            tracker.addError(notEnoughSpace);
            ci.cancel();
            return;
        }

        int operations = MathUtils.clampToInt(needed / outputAmount);
        tracker.updateOperations(operations);
        if (operations == 0) {
            tracker.addError(notEnoughSpace);
        }
        ci.cancel();
    }
}
