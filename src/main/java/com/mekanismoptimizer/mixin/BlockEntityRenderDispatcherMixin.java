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
 * Intercepts BlockEntityRenderDispatcher.render (m_112267_) at priority 800 (before EntityCulling)
 * to render Wind Generators and distance-cull Transmitters directly, bypassing:
 * 1. EntityCulling getRenderer(blockEntity) + shouldRenderOffScreen + System.currentTimeMillis() (13,932x/frame)
 * 2. ImmutableMap.get(blockEntity.getType()) renderer lookup (13,932x/frame)
 * 3. BlockEntityType.isValid(blockState) set lookup (13,932x/frame)
 * 4. tryRender capturing Runnable lambda allocation (13,932x/frame)
 * 5. Per-frame LevelRenderer.getLightColor chunk light engine lookups (replaced with a 16,384-slot 32-tick spatial cache)
 * 6. MekanismTileEntityRenderer profiler.push/pop overhead (13,932x/frame)
 */
@Mixin(value = BlockEntityRenderDispatcher.class, priority = 800, remap = false)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(
            method = {
                    "m_112267_(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
                    "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
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
