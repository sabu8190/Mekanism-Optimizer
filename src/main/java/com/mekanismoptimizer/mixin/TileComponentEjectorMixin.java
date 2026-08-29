package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = TileComponentEjector.class, remap = false)
public abstract class TileComponentEjectorMixin {

    @Shadow
    @Final
    private TileEntityMekanism tile;

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    private void onTickServer(CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_EJECTOR_OPTIMIZATION.get() || tile == null) {
            return;
        }

        BlockPos pos = tile.getBlockPos();
        long currentTick = tile.getLevel() != null ? tile.getLevel().getGameTime() : 0;

        if (!AdaptiveBackoffManager.shouldRun(pos, currentTick)) {
            ci.cancel();
        }
    }
}