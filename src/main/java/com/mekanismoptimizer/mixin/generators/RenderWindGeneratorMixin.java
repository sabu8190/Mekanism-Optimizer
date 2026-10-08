package com.mekanismoptimizer.mixin.generators;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "mekanism.generators.client.render.RenderWindGenerator", remap = false)
public abstract class RenderWindGeneratorMixin {

    /**
     * Enables frustum culling for Wind Generators by overriding shouldRenderOffScreen to false.
     * Prevents rendering 21 model parts and doing matrix rotations for windmills outside camera view.
     */
    @Inject(method = "shouldRenderOffScreen", at = @At("HEAD"), cancellable = true)
    private void shouldRenderOffScreenOptimized(TileEntityWindGenerator tile, CallbackInfoReturnable<Boolean> cir) {
        if (MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_RENDER_CULLING.get()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Enlarges the render bounding box to fully enclose the 4.5 radius blade sweep and 10 block mast height,
     * ensuring the windmill is not culled prematurely while still enabling efficient frustum culling.
     */
    @Inject(method = "getRenderBoundingBox", at = @At("HEAD"), cancellable = true)
    private void getRenderBoundingBoxOptimized(TileEntityWindGenerator tile, CallbackInfoReturnable<AABB> cir) {
        if (!MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_RENDER_CULLING.get()) {
            return;
        }

        BlockPos pos = tile.getBlockPos();
        cir.setReturnValue(new AABB(pos.offset(-4, 0, -4), pos.offset(5, 11, 5)));
    }
}
