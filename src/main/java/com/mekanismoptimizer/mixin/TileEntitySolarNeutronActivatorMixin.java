package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = TileEntitySolarNeutronActivator.class, remap = false)
public abstract class TileEntitySolarNeutronActivatorMixin {

    @Unique
    private long mekanism_optimizer$lastCheckTick = -1;
    @Unique
    private boolean mekanism_optimizer$cachedCanSeeSun = false;

    @Inject(method = "canSeeSun", at = @At("HEAD"), cancellable = true)
    private void onCanSeeSun(CallbackInfoReturnable<Boolean> cir) {
        if (!MekanismOptimizerConfig.ENABLE_SOLAR_CACHE.get()) {
            return;
        }

        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        long currentTick = level != null ? level.getGameTime() : 0;

        if (currentTick - mekanism_optimizer$lastCheckTick < 20 && mekanism_optimizer$lastCheckTick >= 0) {
            MekanismOptimizerLogger.recordSolarCheckCached();
            cir.setReturnValue(mekanism_optimizer$cachedCanSeeSun);
            return;
        }

        BlockPos pos = self.getBlockPos();
        boolean canSee = WorldUtils.canSeeSun(level, pos.above());
        mekanism_optimizer$lastCheckTick = currentTick;
        mekanism_optimizer$cachedCanSeeSun = canSee;
        cir.setReturnValue(canSee);
    }
}