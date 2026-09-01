package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.FastFactorySorter;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(value = TileEntityFactory.class, remap = false)
public abstract class TileEntityFactoryMixin {

    @Shadow
    protected List<IInventorySlot> inputSlots;

    @Shadow
    protected List<IInventorySlot> outputSlots;

    @Inject(method = "sortInventory", at = @At("HEAD"), cancellable = true)
    private void onSortInventory(CallbackInfo ci) {
        TileEntityFactory<?> self = (TileEntityFactory<?>) (Object) this;
        if (FastFactorySorter.sortFast(self)) {
            ci.cancel();
        }
    }

    /**
     * O(1) Fast early return for completely empty or stalled factories under Torcherino / Tick acceleration.
     * Prevents thousands of redundant monitor/recipe/energy iterations per tick.
     */
    @Inject(method = "onUpdateServer", at = @At("HEAD"), cancellable = true)
    private void onUpdateServerAccelerated(CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_ADDON_OPTIMIZATIONS.get()) {
            return;
        }

        TileEntityFactory<?> self = (TileEntityFactory<?>) (Object) this;

        // 1. Check if all input slots are completely empty
        if (inputSlots != null && !inputSlots.isEmpty()) {
            boolean allEmpty = true;
            for (int i = 0; i < inputSlots.size(); i++) {
                IInventorySlot slot = inputSlots.get(i);
                if (slot != null && !slot.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }

            if (allEmpty) {
                // If completely empty and inactive, skip heavy monitor and energy checking
                if (!self.getActive()) {
                    ci.cancel();
                    return;
                }
            }
        }

        // 2. Check if all output slots are completely full and unable to receive
        if (outputSlots != null && !outputSlots.isEmpty()) {
            boolean allFull = true;
            for (int i = 0; i < outputSlots.size(); i++) {
                IInventorySlot slot = outputSlots.get(i);
                if (slot == null || slot.isEmpty()) {
                    allFull = false;
                    break;
                }
                ItemStack stack = slot.getStack();
                if (stack.getCount() < stack.getMaxStackSize()) {
                    allFull = false;
                    break;
                }
            }

            if (allFull && !self.getActive()) {
                ci.cancel();
            }
        }
    }
}