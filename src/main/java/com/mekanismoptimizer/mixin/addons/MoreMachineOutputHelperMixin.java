package com.mekanismoptimizer.mixin.addons;

import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.jerry.mekmm.api.recipes.outputs.MoreMachineOutputHelper", remap = false)
public abstract class MoreMachineOutputHelperMixin {

    /**
     * Optimized O(1) zero-allocation operation calculation for Mekanism More Machine Inventory Slots.
     * Replaces expensive copyWithCount, simulated insertion, and avoids slot limit exceptions.
     */
    @Inject(method = "calculateOperationsCanSupport(Lmekanism/api/recipes/cache/CachedRecipe$OperationTracker;Lmekanism/api/recipes/cache/CachedRecipe$OperationTracker$RecipeError;Lmekanism/api/inventory/IInventorySlot;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
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
}
