package com.mekanismoptimizer.mixin.generators;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.FastWindGeneratorCache;
import com.mekanismoptimizer.core.FastWindGeneratorRenderer;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import it.unimi.dsi.fastutil.HashCommon;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntityWindGenerator", remap = false)
public abstract class TileEntityWindGeneratorMixin {

    @Shadow(remap = false)
    private FloatingLong currentMultiplier;

    @Shadow(remap = false)
    private boolean isBlacklistDimension;

    @Unique
    private AABB mekOpti$cachedRenderBox;

    @Unique
    private BlockPos mekOpti$cachedRenderPos;

    @Inject(method = "onUpdateServer()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUpdateServerHead(CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_ADAPTIVE_BACKOFF.get()) {
            return;
        }

        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }

        long gameTime = level.getGameTime();
        long posHash = HashCommon.mix(self.getBlockPos().asLong());
        if (((gameTime + posHash) & 7L) != 0L) {
            MekanismOptimizerLogger.recordBackoffSkipped();
            ci.cancel();
            return;
        }

        List<IEnergyContainer> containers = self.getEnergyContainers(null);
        IEnergyContainer container = (containers != null && !containers.isEmpty()) ? containers.get(0) : null;
        if (container != null && container.getNeeded().isZero()) {
            if (AdaptiveBackoffManager.shouldBackoff(self)) {
                MekanismOptimizerLogger.recordBackoffSkipped();
                ci.cancel();
                return;
            }
        }

        if (this.currentMultiplier == null || this.currentMultiplier.isZero() || ((gameTime + posHash) & 16L) == 0L) {
            self.ticker = 0;
        } else if (self.ticker % 20 == 0) {
            self.ticker = 1;
        }

        if (!this.isBlacklistDimension && this.currentMultiplier != null && !this.currentMultiplier.isZero()
                && container != null && !container.getNeeded().isZero() && MekanismUtils.canFunction(self)) {
            FloatingLong sevenTicksGen = FastWindGeneratorCache.getSevenTicksGeneration(this.currentMultiplier);
            if (!sevenTicksGen.isZero()) {
                container.insert(sevenTicksGen, Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
    }

    @Inject(method = "onUpdateClient()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUpdateClientHead(CallbackInfo ci) {
        if (FastWindGeneratorRenderer.isClientTickSkipEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getMultiplier()Lmekanism/api/math/FloatingLong;", at = @At("HEAD"), cancellable = true, remap = false)
    private void getMultiplierOptimized(CallbackInfoReturnable<FloatingLong> cir) {
        if (!MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_OPTIMIZATION.get()) {
            return;
        }

        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        cir.setReturnValue(FastWindGeneratorCache.getOptimizedMultiplier(self.getLevel(), self.getBlockPos()));
    }

    @Inject(method = "getRenderBoundingBox()Lnet/minecraft/world/phys/AABB;", at = @At("HEAD"), cancellable = true, remap = false)
    private void getRenderBoundingBoxOptimized(CallbackInfoReturnable<AABB> cir) {
        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        BlockPos pos = self.getBlockPos();
        AABB box = this.mekOpti$cachedRenderBox;
        if (box == null || !pos.equals(this.mekOpti$cachedRenderPos)) {
            this.mekOpti$cachedRenderPos = pos;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            box = new AABB(x - 2, y, z - 2, x + 3, y + 7, z + 3);
            this.mekOpti$cachedRenderBox = box;
        }
        cir.setReturnValue(box);
    }
}

