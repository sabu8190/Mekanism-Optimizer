package com.mekanismoptimizer.mixin;

import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Caches getRenderBoundingBox() on TileEntityTransmitter (and subclasses across Mekanism, EvolvedMekanism,
 * MekanismExtras, and EMExtras), eliminating per-frame BlockPos.offset(1, 1, 1) and AABB heap allocations
 * in SodiumWorldRenderer.renderBlockEntities and EntityCulling.
 */
@Pseudo
@Mixin(targets = "mekanism.common.tile.transmitter.TileEntityTransmitter", remap = false)
public abstract class TileEntityTransmitterMixin {

    @Unique
    private AABB mekOpti$cachedRenderBox;

    @Unique
    private BlockPos mekOpti$cachedRenderPos;

    @Inject(method = "getRenderBoundingBox()Lnet/minecraft/world/phys/AABB;", at = @At("HEAD"), cancellable = true, remap = false)
    private void getRenderBoundingBoxCached(CallbackInfoReturnable<AABB> cir) {
        TileEntityTransmitter self = (TileEntityTransmitter) (Object) this;
        BlockPos pos = self.getBlockPos();
        AABB box = this.mekOpti$cachedRenderBox;
        if (box == null || !pos.equals(this.mekOpti$cachedRenderPos)) {
            this.mekOpti$cachedRenderPos = pos;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            box = new AABB(x, y, z, x + 1, y + 1, z + 1);
            this.mekOpti$cachedRenderBox = box;
        }
        cir.setReturnValue(box);
    }
}
