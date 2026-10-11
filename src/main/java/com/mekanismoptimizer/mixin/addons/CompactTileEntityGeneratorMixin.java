package com.mekanismoptimizer.mixin.addons;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents per-tick EnumSet.noneOf(Direction.class) and RelativeSide[] allocations
 * in Compact Mekanism Machines' TileEntityGenerator when cable emission is backed off.
 */
@Pseudo
@Mixin(targets = "com.CompactMekanismMachines.common.tile.TileEntityGenerator", remap = false)
public abstract class CompactTileEntityGeneratorMixin {

    @Inject(
            method = "onUpdateServer()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/util/MekanismUtils;canFunction(Lmekanism/common/tile/base/TileEntityMekanism;)Z"
            ),
            cancellable = true,
            remap = false
    )
    private void onBeforeCableEject(CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_CABLE_BACKOFF.get()) {
            return;
        }
        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        if (AdaptiveBackoffManager.shouldBackoff(self)) {
            MekanismOptimizerLogger.recordCableBackoffSkipped();
            ci.cancel();
        }
    }
}
