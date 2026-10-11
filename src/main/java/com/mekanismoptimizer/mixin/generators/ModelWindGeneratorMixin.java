package com.mekanismoptimizer.mixin.generators;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mekanismoptimizer.core.FastWindGeneratorRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "mekanism.generators.client.model.ModelWindGenerator", remap = false)
public abstract class ModelWindGeneratorMixin {

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;DIIZ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderPrebakedDirect(
            PoseStack matrix,
            MultiBufferSource renderer,
            double angle,
            int light,
            int overlayLight,
            boolean hasEffect,
            CallbackInfo ci
    ) {
        if (FastWindGeneratorRenderer.renderModelDirect(this, matrix, renderer, angle, light, overlayLight, hasEffect)) {
            ci.cancel();
        }
    }
}
