package com.mekanismoptimizer.mixin.addons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mekanismoptimizer.core.FastWindGeneratorRenderer;
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

/**
 * Optimizes rendering for Compact Mekanism Machines' Compressed Wind Generators (x2 .. x532480).
 * Hooks shouldRender (m_142163_) and bridge methods on RenderCompressedWindGenerator.
 */
@Pseudo
@Mixin(targets = "com.CompactMekanismMachines.client.render.RenderCompressedWindGenerator", remap = false)
public abstract class RenderCompressedWindGeneratorMixin {

    /**
     * Early distance culling and dense-farm spatial grid decimation in BlockEntityRenderer.shouldRender (m_142163_).
     * Skips tryRender lambda allocation, LevelRenderer.getLightColor, and profiler push/pop for culled windmills.
     */
    public boolean m_142163_(BlockEntity tile, Vec3 cameraPos) {
        return FastWindGeneratorRenderer.shouldRenderWindGenerator(tile, cameraPos);
    }

    @Inject(
            method = "m_5932_(Lnet/minecraft/world/level/block/entity/BlockEntity;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void shouldRenderOffScreenOptimized(BlockEntity tile, CallbackInfoReturnable<Boolean> cir) {
        if (FastWindGeneratorRenderer.isRenderCullingEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void renderOptimized(
            BlockEntity tile,
            float partialTick,
            PoseStack matrix,
            MultiBufferSource renderer,
            int light,
            int overlayLight,
            ProfilerFiller profiler,
            CallbackInfo ci
    ) {
        if (FastWindGeneratorRenderer.renderFastAddon(this, tile, 4.0F, partialTick, matrix, renderer, light, overlayLight)) {
            ci.cancel();
        }
    }
}
