package com.mekanismoptimizer.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "mekanism.client.render.tileentity.RenderFluidTank", remap = false)
public abstract class RenderFluidTankMixin {

    /**
     * Optimized render method for Fluid Tanks:
     * Fast bypass when tank is completely empty (no contained fluids and no valve fluids),
     * completely avoiding buffer allocations, texture lookups, and glow calculations.
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

        // Early out if nothing to render (100% zero-allocation bypass for empty fluid tanks)
        if (!hasFluid && !hasValve) {
            ci.cancel();
        }
    }
}
