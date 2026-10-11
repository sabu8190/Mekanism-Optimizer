package com.mekanismoptimizer.mixin.generators;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import it.unimi.dsi.fastutil.HashCommon;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntityGenerator", remap = false)
public abstract class TileEntityGeneratorMixin {

    @Inject(method = "onUpdateServer()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUpdateServerGenerator(CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_CABLE_BACKOFF.get()) {
            return;
        }

        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        List<IEnergyContainer> containers = self.getEnergyContainers(null);
        if (containers != null && !containers.isEmpty()) {
            IEnergyContainer container = containers.get(0);
            if (container != null && container.getNeeded().isZero() && self.getLevel() != null) {
                long gameTime = self.getLevel().getGameTime();
                long posHash = HashCommon.mix(self.getBlockPos().asLong());
                if (((gameTime + posHash) & 7L) != 0L || AdaptiveBackoffManager.shouldBackoff(self)) {
                    MekanismOptimizerLogger.recordCableBackoffSkipped();
                    ci.cancel();
                }
            }
        }
    }
}
