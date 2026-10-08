package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.content.transporter.TransporterStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Pseudo
@Mixin(targets = "mekanism.client.render.transmitter.RenderLogisticalTransporter", remap = false)
public abstract class RenderLogisticalTransporterMixin {

    /**
     * Fast-path for single-item transit to bypass HashSet and TransportInformation allocations completely.
     */
    @Inject(method = "getReducedTransit", at = @At("HEAD"), cancellable = true)
    private void getReducedTransitFastPath(Collection<TransporterStack> inTransit, CallbackInfoReturnable<Collection<TransporterStack>> cir) {
        if (!MekanismOptimizerConfig.ENABLE_TRANSMITTER_RENDER_OPTIMIZATION.get()) {
            return;
        }

        if (inTransit == null || inTransit.isEmpty()) {
            cir.setReturnValue(inTransit);
            return;
        }

        // Fast-path: When there is only 1 item in transit (very common case), no deduplication is needed
        if (inTransit.size() == 1) {
            TransporterStack first = inTransit.iterator().next();
            if (first != null && !first.itemStack.isEmpty()) {
                cir.setReturnValue(inTransit);
                return;
            }
        }

        // Fast-path: When there are 2 items in transit, check if they are obviously distinct without allocating Set
        if (inTransit.size() == 2) {
            java.util.Iterator<TransporterStack> it = inTransit.iterator();
            TransporterStack s1 = it.next();
            TransporterStack s2 = it.next();
            if (s1 != null && s2 != null && !s1.itemStack.isEmpty() && !s2.itemStack.isEmpty()) {
                // If progress differs, they cannot be in the same position
                if (s1.progress != s2.progress || s1.color != s2.color) {
                    cir.setReturnValue(inTransit);
                }
            }
        }
    }
}
