package com.mekanismoptimizer.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "mekanism.client.render.tileentity.RenderFluidTank", remap = false)
public abstract class RenderFluidTankMixin {

    @Shadow
    public static MekanismRenderer.Model3D getFluidModel(FluidStack fluid, float fluidScale) {
        throw new AbstractMethodError();
    }

    @Shadow
    private MekanismRenderer.Model3D getValveModel(FluidStack fluid, float fluidScale) {
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract Camera getCamera();

    /**
     * Optimized render method for Fluid Tanks:
     * 1. Early-out when tank is empty (no fluids and no valve fluids) to avoid buffer lookup & allocations.
     * 2. Direct VertexConsumer retrieval without allocating Lazy objects every frame.
     */
    @Inject(method = "render(Lmekanism/common/tile/TileEntityFluidTank;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), cancellable = true)
    private void renderOptimized(TileEntityFluidTank tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
                                ProfilerFiller profiler, CallbackInfo ci) {
        if (!MekanismOptimizerConfig.ENABLE_FLUID_TANK_RENDER_OPTIMIZATION.get()) {
            return;
        }

        FluidStack fluid = tile.fluidTank.getFluid();
        float fluidScale = tile.prevScale;
        boolean hasFluid = !fluid.isEmpty() && fluidScale > 0;
        boolean hasValve = !tile.valveFluid.isEmpty() && !MekanismUtils.lighterThanAirGas(tile.valveFluid);

        // Early out if nothing to render
        if (!hasFluid && !hasValve) {
            ci.cancel();
            return;
        }

        VertexConsumer buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
        Camera camera = getCamera();

        if (hasFluid) {
            MekanismRenderer.renderObject(getFluidModel(fluid, fluidScale), matrix, buffer, MekanismRenderer.getColorARGB(fluid, fluidScale),
                    MekanismRenderer.calculateGlowLight(light, fluid), overlayLight, RenderResizableCuboid.FaceDisplay.FRONT, camera, tile.getBlockPos());
        }
        if (hasValve) {
            MekanismRenderer.renderObject(getValveModel(tile.valveFluid, fluidScale), matrix, buffer,
                    MekanismRenderer.getColorARGB(tile.valveFluid), MekanismRenderer.calculateGlowLight(light, tile.valveFluid), overlayLight,
                    RenderResizableCuboid.FaceDisplay.FRONT, camera, tile.getBlockPos());
        }
        ci.cancel();
    }
}
