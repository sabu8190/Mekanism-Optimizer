package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import com.mekanismoptimizer.core.MultiblockOptimizer;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = MultiblockData.class, remap = false)
public abstract class MultiblockDataMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onMultiblockTick(Level world, CallbackInfoReturnable<Boolean> cir) {
        MultiblockData self = (MultiblockData) (Object) this;
        if (MultiblockOptimizer.shouldSkipCalculation(self, self.isFormed() ? 1L : 0L, 1L)) {
            cir.setReturnValue(false);
        }
    }
}
