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
 * Optimizes rendering for Mekanism More Machine's Large Wind Generator (com.jerry.meklg.client.render.RenderLargeWindGenerator).
 */
@Pseudo
@Mixin(targets = "com.jerry.meklg.client.render.RenderLargeWindGenerator", remap = false)
public abstract class RenderLargeWindGeneratorMixin {

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
        if (FastWindGeneratorRenderer.renderFastAddon(this, tile, 36.0F, partialTick, matrix, renderer, light, overlayLight)) {
            ci.cancel();
        }
    }
}
