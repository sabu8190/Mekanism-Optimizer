package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.inventory.TileTransitRequest;
import mekanism.common.util.InventoryUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Pseudo
@Mixin(value = InventoryUtils.class, remap = false)
public abstract class InventoryUtilsMixin {

    /**
     * O(1) Fast path for getEjectItemMap.
     * If all slots are empty or cannot be extracted, skip ArrayList allocation, Collections.shuffle, and simulation entirely.
     */
    @Inject(method = "getEjectItemMap(Lmekanism/common/lib/inventory/TileTransitRequest;Ljava/util/List;)Lmekanism/common/lib/inventory/TileTransitRequest;",
            at = @At("HEAD"), cancellable = true)
    private static <REQUEST extends TileTransitRequest> void onGetEjectItemMapHead(REQUEST request, List<IInventorySlot> slots,
                                                                                  CallbackInfoReturnable<REQUEST> cir) {
        if (!MekanismOptimizerConfig.ENABLE_ADDON_OPTIMIZATIONS.get()) {
            return;
        }

        if (slots == null || slots.isEmpty()) {
            cir.setReturnValue(request);
            return;
        }

        // Quick check: if all slots are empty, return empty request immediately without allocations
        boolean hasAnyItem = false;
        for (int i = 0; i < slots.size(); i++) {
            IInventorySlot slot = slots.get(i);
            if (slot != null && !slot.isEmpty()) {
                hasAnyItem = true;
                break;
            }
        }

        if (!hasAnyItem) {
            cir.setReturnValue(request);
            return;
        }

        // If only 1 slot has item, no need to shuffle
        if (slots.size() == 1) {
            IInventorySlot slot = slots.get(0);
            if (slot != null && !slot.isEmpty()) {
                ItemStack sim = slot.extractItem(slot.getCount(), Action.SIMULATE, AutomationType.EXTERNAL);
                if (!sim.isEmpty()) {
                    request.addItem(sim, 0);
                }
            }
            cir.setReturnValue(request);
        }
    }
}
