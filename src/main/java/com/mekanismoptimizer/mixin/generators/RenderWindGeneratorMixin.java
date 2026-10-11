package com.mekanismoptimizer.mixin.generators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mekanismoptimizer.core.FastWindGeneratorRenderer;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "mekanism.generators.client.render.RenderWindGenerator", remap = false)
public abstract class RenderWindGeneratorMixin {

    /**
     * Early distance culling and dense-farm spatial grid decimation in BlockEntityRenderer.shouldRender (m_142163_).
     * Skips tryRender lambda allocation, LevelRenderer.getLightColor, and profiler push/pop for culled windmills.
     */
    public boolean m_142163_(BlockEntity tile, Vec3 cameraPos) {
        return FastWindGeneratorRenderer.shouldRenderWindGenerator(tile, cameraPos);
    }

    /**
     * Enables frustum culling for Wind Generators by overriding shouldRenderOffScreen to false.
     */
    @Inject(method = "shouldRenderOffScreen(Lmekanism/generators/common/tile/TileEntityWindGenerator;)Z", at = @At("HEAD"), cancellable = true)
    private void shouldRenderOffScreenOptimized(TileEntityWindGenerator tile, CallbackInfoReturnable<Boolean> cir) {
        if (FastWindGeneratorRenderer.isRenderCullingEnabled()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Replaces per-frame 21-part ModelPart hierarchy traversal and capturing lambda allocation
     * with pre-baked static & rigid rotor meshes, distance/cluster LOD, and angle quantization.
     */
    @Inject(
            method = "render(Lmekanism/generators/common/tile/TileEntityWindGenerator;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderOptimized(
            TileEntityWindGenerator tile,
            float partialTick,
            PoseStack matrix,
            MultiBufferSource renderer,
            int light,
            int overlayLight,
            ProfilerFiller profiler,
            CallbackInfo ci
    ) {
        if (FastWindGeneratorRenderer.renderFast(this, tile, partialTick, matrix, renderer, light, overlayLight)) {
            ci.cancel();
        }
    }
}
