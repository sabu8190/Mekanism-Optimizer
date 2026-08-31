package com.mekanismoptimizer.mixin.generators;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntityWindGenerator", remap = false)
public abstract class TileEntityWindGeneratorMixin {

    @Inject(method = "onUpdateServer", at = @At("HEAD"), cancellable = true)
    private void onUpdateServerHead(CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_ADAPTIVE_BACKOFF.get()) {
            return;
        }

        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        List<IEnergyContainer> containers = self.getEnergyContainers(null);
        if (containers != null && !containers.isEmpty()) {
            IEnergyContainer container = containers.get(0);
            if (container != null && container.getNeeded().isZero()) {
                if (self.getLevel() != null && (self.getLevel().getGameTime() % 4L != 0L)) {
                    MekanismOptimizerLogger.recordBackoffSkipped();
                    ci.cancel();
                }
            }
        }
    }
}
