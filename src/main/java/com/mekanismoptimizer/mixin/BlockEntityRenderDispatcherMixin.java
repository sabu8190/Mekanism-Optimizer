package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.FastWindGeneratorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts BlockEntityRenderDispatcher.render at priority 800 (before EntityCulling)
 * to render Wind Generators directly, bypassing:
 * 1. EntityCulling getRenderer(blockEntity) + shouldRenderOffScreen + System.currentTimeMillis()
 * 2. ImmutableMap.get(blockEntity.getType()) renderer lookup
 * 3. BlockEntityType.isValid(blockState) set lookup
 * 4. tryRender capturing Runnable lambda allocation
 * 5. Per-frame LevelRenderer.getLightColor chunk light engine lookups
 * 6. MekanismTileEntityRenderer profiler.push/pop overhead
 */
@Mixin(value = BlockEntityRenderDispatcher.class, priority = 800)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends BlockEntity> void renderWindGeneratorFast(
            E blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            CallbackInfo ci
    ) {
        if (FastWindGeneratorRenderer.tryRenderFromDispatcher(blockEntity, partialTick, poseStack, bufferSource)) {
            ci.cancel();
        }
    }
}
