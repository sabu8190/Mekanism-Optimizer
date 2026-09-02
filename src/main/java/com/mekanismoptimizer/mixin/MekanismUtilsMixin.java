package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(value = MekanismUtils.class, remap = false)
public abstract class MekanismUtilsMixin {

    /**
     * O(1) Fast path for redstoneLevelFromContents.
     * Prevents thousands of getLimit() / BiggerStacks rule checks per tick when inventory is empty or static.
     */
    @Inject(method = "redstoneLevelFromContents(Ljava/util/List;)I", at = @At("HEAD"), cancellable = true)
    private static void onRedstoneLevelFromContents(List<IInventorySlot> slots, CallbackInfoReturnable<Integer> cir) {
        if (!MekanismOptimizerConfig.ENABLE_LOOKING_AT_CACHE.get()) {
            return;
        }

        if (slots == null || slots.isEmpty()) {
            cir.setReturnValue(0);
            return;
        }

        // Quick check: if all slots are empty, return 0 immediately without calling getLimit on all slots
        boolean allEmpty = true;
        for (int i = 0; i < slots.size(); i++) {
            IInventorySlot slot = slots.get(i);
            if (slot != null && !slot.isEmpty()) {
                allEmpty = false;
                break;
            }
        }

        if (allEmpty) {
            cir.setReturnValue(0);
        }
    }
}
