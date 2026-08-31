package com.mekanismoptimizer.mixin.generators;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.math.FloatingLong;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntitySolarGenerator", remap = false)
public abstract class TileEntitySolarGeneratorMixin {

    @Unique
    private static long mekanismOptimizer$lastWorldTime = -1L;
    @Unique
    private static boolean mekanismOptimizer$isDayCached = true;

    @Inject(method = "checkCanSeeSun", at = @At("HEAD"), cancellable = true)
    private void onCheckCanSeeSun(CallbackInfoReturnable<Boolean> cir) {
        if (!MekanismOptimizerConfig.ENABLE_SOLAR_LIGHT_CACHE.get()) {
            return;
        }

        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            cir.setReturnValue(false);
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime != mekanismOptimizer$lastWorldTime) {
            mekanismOptimizer$lastWorldTime = gameTime;
            mekanismOptimizer$isDayCached = level.isDay();
        }

        // Nighttime fast bypass: sun is physically not visible at night
        if (!mekanismOptimizer$isDayCached) {
            MekanismOptimizerLogger.recordSolarCached();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getProduction", at = @At("HEAD"), cancellable = true)
    private void onGetProduction(CallbackInfoReturnable<FloatingLong> cir) {
        if (!MekanismOptimizerConfig.ENABLE_SOLAR_LIGHT_CACHE.get()) {
            return;
        }

        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        Level level = self.getLevel();
        if (level != null && !level.isDay()) {
            cir.setReturnValue(FloatingLong.ZERO);
        }
    }
}
