package com.mekanismoptimizer.mixin.addons;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
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
@Mixin(targets = "fr.iglee42.evolvedmekanism.tiles.TileEntityLunarNeutronActivator", remap = false)
public abstract class EvolvedLunarActivatorMixin {

    @Unique
    private long mekanism_optimizer$lastCheckTick = -1;
    @Unique
    private boolean mekanism_optimizer$cachedCanSeeSun = false;

    @Inject(method = "canSeeSun", at = @At("HEAD"), cancellable = true)
    private void onCanSeeSun(CallbackInfoReturnable<Boolean> cir) {
        if (!MekanismOptimizerConfig.ENABLE_ADDON_OPTIMIZATIONS.get()) {
            return;
        }

        long currentTick = ((BlockEntity) (Object) this).getLevel() != null ? 
                           ((BlockEntity) (Object) this).getLevel().getGameTime() : 0;

        // Cache result for 20 ticks (1 second) to avoid per-tick sky light / dimension / night calculation
        if (currentTick - mekanism_optimizer$lastCheckTick < 20 && mekanism_optimizer$lastCheckTick >= 0) {
            cir.setReturnValue(mekanism_optimizer$cachedCanSeeSun);
            return;
        }

        Level level = ((BlockEntity) (Object) this).getLevel();
        BlockPos pos = ((BlockEntity) (Object) this).getBlockPos();
        boolean canSee = level != null && level.dimensionType().hasSkyLight() && 
                         level.canSeeSky(pos.above()) && level.isNight();

        mekanism_optimizer$lastCheckTick = currentTick;
        mekanism_optimizer$cachedCanSeeSun = canSee;
        cir.setReturnValue(canSee);
    }
}