package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.FastSlotIndexer;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.Coord4D;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Pseudo
@Mixin(value = TransporterManager.class, remap = false)
public abstract class TransporterManagerMixin {

    @Inject(method = "getPredictedInsert(Lmekanism/api/Coord4D;Lnet/minecraft/core/Direction;Lnet/minecraftforge/items/IItemHandler;Lmekanism/common/lib/inventory/TransitRequest;Ljava/util/Map;)Lmekanism/common/lib/inventory/TransitRequest$TransitResponse;",
            at = @At("HEAD"), cancellable = true)
    private static void onGetPredictedInsert(Coord4D position, Direction side, IItemHandler handler, TransitRequest request,
                                             Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks,
                                             CallbackInfoReturnable<TransitRequest.TransitResponse> cir) {
        if (!MekanismOptimizerConfig.ENABLE_FAST_SIMULATE.get() || handler == null || request == null || request.isEmpty()) {
            return;
        }

        FastSlotIndexer indexer = FastSlotIndexer.get(handler);
        if (indexer != null && !indexer.hasEmptySlot()) {
            // Container is full: check if it contains any of the requested items
            boolean hasAnyMatch = false;
            for (TransitRequest.ItemData data : request.getItemData()) {
                if (data != null && data.getStack() != null && indexer.containsItem(data.getStack().getItem())) {
                    hasAnyMatch = true;
                    break;
                }
            }

            if (!hasAnyMatch) {
                // Container cannot accept any of the requested items; fast exit
                MekanismOptimizerLogger.recordMekanismSimulationSaved();
                cir.setReturnValue(request.getEmptyResponse());
            }
        }
    }
}