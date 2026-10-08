package com.mekanismoptimizer.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mekanismoptimizer.core.FastTransmitterRenderCache;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "mekanism.client.render.transmitter.RenderTransmitterBase", remap = false)
public abstract class RenderTransmitterBaseMixin<TRANSMITTER extends TileEntityTransmitter> {

    @Shadow
    protected abstract void renderModel(TRANSMITTER transmitter, PoseStack matrix, VertexConsumer builder, float red, float green, float blue, float alpha, int light,
                                        int overlayLight, TextureAtlasSprite icon, List<String> visible);

    @Inject(method = "renderModel(Lmekanism/common/tile/transmitter/TileEntityTransmitter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V", at = @At("HEAD"), cancellable = true)
    private void renderModelOptimized(TRANSMITTER transmitter, PoseStack matrix, VertexConsumer builder, int rgb, float alpha, int light, int overlayLight,
                                      TextureAtlasSprite icon, CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_TRANSMITTER_RENDER_OPTIMIZATION.get()) {
            return;
        }

        List<String> visible = FastTransmitterRenderCache.getVisibleSides(transmitter.getTransmitter());
        renderModel(transmitter, matrix, builder, MekanismRenderer.getRed(rgb), MekanismRenderer.getGreen(rgb), MekanismRenderer.getBlue(rgb), alpha, light,
                overlayLight, icon, visible);
        ci.cancel();
    }
}
