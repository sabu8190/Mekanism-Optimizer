package com.mekanismoptimizer.mixin;

import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TileEntityBoundingBlock has no BlockEntityRenderer (RenderShape.INVISIBLE), so neither Vanilla nor Embeddium
 * ever renders it. However, EntityCulling blindly iterates all chunk BlockEntities and calls
 * IForgeBlockEntity.getRenderBoundingBox(), which invokes BlockBounding.getCollisionShape -> VoxelShape.move()
 * and performs 3D voxel raycasting on 20,898+ invisible bounding blocks in dense Wind Generator farms.
 * Returning INFINITE_EXTENT_AABB causes EntityCulling's hitboxLimit check to skip raycasting immediately with zero allocations.
 */
@Pseudo
@Mixin(targets = "mekanism.common.tile.TileEntityBoundingBlock", remap = false)
public abstract class TileEntityBoundingBlockMixin {

    public AABB getRenderBoundingBox() {
        return IForgeBlockEntity.INFINITE_EXTENT_AABB;
    }
}
