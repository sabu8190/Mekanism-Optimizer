package com.mekanismoptimizer.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * High-performance object pool for Energy Cube core LazyRender instances.
 * Eliminates per-frame anonymous class allocations for every visible energy cube.
 */
public final class FastEnergyCubeRenderPool {

    private static final float ONE_OVER_ROOT_TWO = 0.7071067811865475F;
    private static final Axis CORE_VEC = Axis.of(new Vector3f(0.0F, ONE_OVER_ROOT_TWO, ONE_OVER_ROOT_TWO));
    private static final Deque<PooledLazyRender> POOL = new ArrayDeque<>(64);

    private FastEnergyCubeRenderPool() {
    }

    public static RenderTickHandler.LazyRender getOrCreate(Vec3 renderPos, BaseTier baseTier, float energyScale, int overlayLight, ModelEnergyCore model) {
        PooledLazyRender render;
        synchronized (POOL) {
            render = POOL.pollFirst();
        }
        if (render == null) {
            render = new PooledLazyRender();
        }
        render.init(renderPos, baseTier, energyScale, overlayLight, model);
        return render;
    }

    public static void release(PooledLazyRender render) {
        if (render != null) {
            int maxCap = 256;
            try {
                if (MekanismOptimizerConfig.ENERGY_CUBE_POOL_CAPACITY != null) {
                    maxCap = MekanismOptimizerConfig.ENERGY_CUBE_POOL_CAPACITY.get();
                }
            } catch (Throwable ignored) {
            }
            synchronized (POOL) {
                if (POOL.size() < maxCap) {
                    POOL.offerLast(render);
                }
            }
        }
    }

    public static class PooledLazyRender implements RenderTickHandler.LazyRender {
        private Vec3 renderPos;
        private BaseTier baseTier;
        private float energyScale;
        private int overlayLight;
        private ModelEnergyCore model;

        public void init(Vec3 renderPos, BaseTier baseTier, float energyScale, int overlayLight, ModelEnergyCore model) {
            this.renderPos = renderPos;
            this.baseTier = baseTier;
            this.energyScale = energyScale;
            this.overlayLight = overlayLight;
            this.model = model;
        }

        @Override
        public void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profiler) {
            float ticks = renderTick + partialTick;
            float scaledTicks = 4 * ticks;
            poseStack.pushPose();
            poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.translate(0, Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(scaledTicks));
            poseStack.mulPose(CORE_VEC.rotationDegrees(36F + scaledTicks));
            if (model != null && baseTier != null) {
                model.render(poseStack, buffer, LightTexture.FULL_BRIGHT, overlayLight, baseTier, energyScale);
            }
            poseStack.popPose();
            FastEnergyCubeRenderPool.release(this);
        }

        @Override
        public Vec3 getCenterPos(float partialTick) {
            return renderPos;
        }

        @Override
        public String getProfilerSection() {
            return ProfilerConstants.ENERGY_CUBE_CORE;
        }
    }
}
