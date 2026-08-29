package com.mekanismoptimizer.mixin;

import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterPathfinder;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = TransporterPathfinder.class, remap = false)
public abstract class TransporterPathfinderMixin {

    @Inject(method = "getNewBasePath(Lmekanism/common/content/network/transmitter/LogisticalTransporterBase;Lmekanism/common/content/transporter/TransporterStack;Lmekanism/common/lib/inventory/TransitRequest;I)Lmekanism/common/content/transporter/TransporterPathfinder$Destination;", at = @At("HEAD"))
    private static void onGetNewBasePath(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min,
                                         CallbackInfoReturnable<Object> cir) {
    }
}