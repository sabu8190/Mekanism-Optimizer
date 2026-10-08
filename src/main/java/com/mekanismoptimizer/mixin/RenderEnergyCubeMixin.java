package com.mekanismoptimizer.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mekanismoptimizer.core.FastEnergyCubeRenderPool;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.tile.TileEntityEnergyCube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "mekanism.client.render.tileentity.RenderEnergyCube", remap = false)
public abstract class RenderEnergyCubeMixin {

    @Shadow
    @Final
    protected ModelEnergyCore model;

    @Inject(method = "render(Lmekanism/common/tile/TileEntityEnergyCube;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), cancellable = true)
    private void renderOptimized(TileEntityEnergyCube tile, float partialTicks, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
                                ProfilerFiller profiler, CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_ENERGY_CUBE_RENDER_OPTIMIZATION.get()) {
            return;
        }

        float energyScale = tile.getEnergyScale();
        Vec3 renderPos = Vec3.atCenterOf(tile.getBlockPos());
        BaseTier baseTier = tile.getTier().getBaseTier();

        RenderTickHandler.addTransparentRenderer(ModelEnergyCore.BATCHED_RENDER_TYPE,
                FastEnergyCubeRenderPool.getOrCreate(renderPos, baseTier, energyScale, overlayLight, this.model));
        ci.cancel();
    }
}
