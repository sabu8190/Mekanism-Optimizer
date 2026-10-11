package com.mekanismoptimizer.mixin.addons;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.FastWindGeneratorRenderer;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import it.unimi.dsi.fastutil.HashCommon;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Server tick backoff and cached AABB frustum culling for Mekanism More Machine's TileEntityLargeWindGenerator.
 */
@Pseudo
@Mixin(targets = "com.jerry.meklg.common.tile.TileEntityLargeWindGenerator", remap = false)
public abstract class TileEntityLargeWindGeneratorMixin {

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
        List<IEnergyContainer> containers = self.getEnergyContainers(null);
        if (containers != null && !containers.isEmpty()) {
            IEnergyContainer container = containers.get(0);
            if (container != null && container.getNeeded().isZero() && self.getLevel() != null) {
                long gameTime = self.getLevel().getGameTime();
                long posHash = HashCommon.mix(self.getBlockPos().asLong());
                if (((gameTime + posHash) & 7L) != 0L || AdaptiveBackoffManager.shouldBackoff(self)) {
                    MekanismOptimizerLogger.recordBackoffSkipped();
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "onUpdateClient()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUpdateClientHead(CallbackInfo ci) {
        if (FastWindGeneratorRenderer.isClientTickSkipEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getRenderBoundingBox()Lnet/minecraft/world/phys/AABB;", at = @At("HEAD"), cancellable = true, remap = false)
    private void getRenderBoundingBoxOptimized(CallbackInfoReturnable<AABB> cir) {
        if (!FastWindGeneratorRenderer.isRenderCullingEnabled()) {
            return;
        }

        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        BlockPos pos = self.getBlockPos();
        AABB box = this.mekOpti$cachedRenderBox;
        if (box == null || !pos.equals(this.mekOpti$cachedRenderPos)) {
            this.mekOpti$cachedRenderPos = pos;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            box = new AABB(x - 20, y, z - 20, x + 21, y + 55, z + 21);
            this.mekOpti$cachedRenderBox = box;
        }
        cir.setReturnValue(box);
    }
}
