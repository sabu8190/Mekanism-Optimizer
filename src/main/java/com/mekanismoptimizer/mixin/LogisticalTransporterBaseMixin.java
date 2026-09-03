package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tier.TransporterTier;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Pseudo
@Mixin(value = LogisticalTransporterBase.class, remap = false)
public abstract class LogisticalTransporterBaseMixin {

    @Shadow
    public int delay;

    @Shadow
    public int delayCount;

    @Shadow
    public TransporterTier tier;

    /**
     * Overclocked burst pulling for Logistical Transporters.
     * Completely eliminates the hardcoded 10-tick (0.5s) pull delay and exponential backoff stall.
     */
    @Inject(method = "onUpdateServer", at = @At("HEAD"))
    private void onUpdateServerHead(CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_ITEM_OVERCLOCK.get()) {
            return;
        }

        // Reset hardcoded delays to allow instant/overclocked throughput
        if (delay > 1) {
            delay = 0;
        }
        if (delayCount > 2) {
            delayCount = 0;
        }

        LogisticalTransporterBase self = (LogisticalTransporterBase) (Object) this;

        // Perform burst extraction if in PULL mode and connected
        Set<Direction> pullSides = self.getConnections(ConnectionType.PULL);
        if (pullSides != null && !pullSides.isEmpty()) {
            int maxBurst = MekanismOptimizerConfig.ITEM_BURST_PER_TICK.get();
            if (maxBurst < 1) maxBurst = 16;

            for (Direction side : pullSides) {
                BlockEntity tile = WorldUtils.getTileEntity(self.getTileWorld(), self.getTilePos().relative(side));
                if (tile != null) {
                    for (int i = 0; i < maxBurst; i++) {
                        TransitRequest request = TransitRequest.anyItem(tile, side.getOpposite(), tier.getPullAmount());
                        if (request.isEmpty()) {
                            break;
                        }
                        TransitRequest.TransitResponse response = self.insert(tile, request, self.getColor(), true, 0);
                        if (response.isEmpty()) {
                            break;
                        }
                        response.useAll();
                    }
                }
            }
        }
    }
}
