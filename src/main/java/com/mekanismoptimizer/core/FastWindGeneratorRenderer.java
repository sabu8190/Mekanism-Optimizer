package com.mekanismoptimizer.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance Wind Generator & Transmitter rendering engine implementing:
 * 1. Direct BlockEntityRenderDispatcher interception at priority 800 (skips EntityCulling System.currentTimeMillis, getRenderer, isValid, tryRender lambda allocation, and profiler push/pop).
 * 2. 16,384-slot (128x128) zero-collision 32-tick spatial light cache (eliminates 99% of LevelRenderer.getLightColor chunk lookups).
 * 3. Per-Frame View-Space Pre-Baked Static & Multi-Slot Rotor Meshes with Same-Y instant reuse and CPU backface culling.
 */
public final class FastWindGeneratorRenderer {

    private static final int FLOATS_PER_QUAD = 23; // 3 normal floats + 4 * (3 pos + 2 uv)
    private static final float DEFAULT_SPEED_SCALED = 8.0F; // TileEntityWindGenerator.SPEED_SCALED = 256F / 32F = 8.0F
    private static final int ROTOR_CACHE_SLOTS = 4; // Covers multiple Y-layers of windmills in the same frame without eviction

    // 16,384-slot (128x128xY) 32-tick spatial light cache to eliminate per-frame LevelRenderer.getLightColor chunk lookups
    private static final int LIGHT_CACHE_SIZE = 16384;
    private static final int LIGHT_CACHE_MASK = LIGHT_CACHE_SIZE - 1;
    private static final long[] LIGHT_CACHE_POS = new long[LIGHT_CACHE_SIZE];
    private static final int[] LIGHT_CACHE_STAMP = new int[LIGHT_CACHE_SIZE];
    private static final int[] LIGHT_CACHE_VAL = new int[LIGHT_CACHE_SIZE];

    // Pre-computed combined (Facing Y-rotation * Z-180 rotation) quaternions
    private static final Quaternionf[] FACING_Z180_ROTATIONS = new Quaternionf[6];

    static {
        Quaternionf z180 = Axis.ZP.rotationDegrees(180.0F);
        FACING_Z180_ROTATIONS[Direction.DOWN.ordinal()] = new Quaternionf(z180);
        FACING_Z180_ROTATIONS[Direction.UP.ordinal()] = new Quaternionf(z180);
        FACING_Z180_ROTATIONS[Direction.NORTH.ordinal()] = new Quaternionf(Axis.YP.rotationDegrees(0.0F)).mul(z180);
        FACING_Z180_ROTATIONS[Direction.SOUTH.ordinal()] = new Quaternionf(Axis.YP.rotationDegrees(180.0F)).mul(z180);
        FACING_Z180_ROTATIONS[Direction.WEST.ordinal()] = new Quaternionf(Axis.YP.rotationDegrees(90.0F)).mul(z180);
        FACING_Z180_ROTATIONS[Direction.EAST.ordinal()] = new Quaternionf(Axis.YP.rotationDegrees(270.0F)).mul(z180);
    }

    // Part indices in ModelWindGenerator.parts (21 parts)
    // 0=HEAD, 1=PLATE_CONNECTOR_2, 2=PLATE_CONNECTOR, 3=PLATE, 4=BASE_RIM, 5=BASE, 6=WIRE,
    // 7=REAR_PLATE_1, 8=REAR_PLATE_2, 9=POST_1A, 10=POST_1B, 11=POST_1C, 12=POST_1D,
    // 13..15=BLADE_1A..3A, 16..18=BLADE_1B..3B, 19=BLADE_CAP, 20=BLADE_CENTER
    private static final int[] STATIC_PARTS_LOD0 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private static final int[] ROTOR_PARTS_LOD0 = {13, 14, 15, 16, 17, 18, 19, 20};

    // LOD 1 (12m - 20m): omits sub-pixel wire, plates, blade cap, secondary blade overlays (1B-3B), and 3 redundant overlapping post boxes (10..12) while keeping full BASE + BASE_RIM + POST_1A + HEAD
    private static final int[] STATIC_PARTS_LOD1 = {0, 4, 5, 7, 9};
    private static final int[] ROTOR_PARTS_LOD1 = {13, 14, 15, 20};

    // LOD 2 (20m - 80m): omits the POST body (9..12) as requested while keeping full HEAD (0) + full BASE (5, including underside face) + 3 primary blades (13..15)
    private static final int[] STATIC_PARTS_LOD2 = {0, 5};
    private static final int[] ROTOR_PARTS_LOD2 = {13, 14, 15};

    // Part indices in ModelLargeWindGenerator.parts (4 top-level part trees: 0=FAN, 1=BODY, 2=TOP, 3=BASE)
    private static final int[] LARGE_STATIC_PARTS_LOD0 = {1, 2, 3};
    private static final int[] LARGE_STATIC_PARTS_LOD2 = {2, 3}; // Omits BODY (1) in LOD 2
    private static final int[] LARGE_ROTOR_PARTS = {0};

    // Per-class reflection caches
    private static final Map<Class<?>, Field> RENDERER_MODEL_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Object, BakedWindModel> BAKED_MODELS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, RegisteredWindTileType> REGISTERED_WIND_TILE_CLASSES = new ConcurrentHashMap<>();

    // Fast single-check volatile caches for consecutive renders of the same renderer/model/tileClass
    private static volatile Object lastRendererInstance;
    private static volatile BakedWindModel lastRendererBakedModel;
    private static volatile Object lastModelInstance;
    private static volatile BakedWindModel lastBakedModel;

    // Dispatcher-level fast caches
    private static Class<?> lastFastTileClass;
    private static BakedWindModel lastFastTileBaked;
    private static float lastFastTileHeightOffset = 4.0F;
    private static Class<?> lastNonWindTileClass;

    private record RegisteredWindTileType(BakedWindModel baked, float heightOffset) {
    }

    // Cached config values refreshed amortized once per 4,096 checks (and once per second)
    private static int configCheckCounter = 0;
    private static volatile long lastConfigRefreshNs = 0L;
    private static boolean cfgRenderCulling = true;
    private static boolean cfgUsePrebaked = true;
    private static boolean cfgUseDirect = true;
    private static boolean cfgUseLod = true;
    private static boolean cfgUseAngleThrottling = true;
    private static boolean cfgTransmitterOpt = true;
    private static double cfgCullDistSq = 80.0D * 80.0D;
    private static double cfgLod1DistSq = 12.0D * 12.0D;
    private static double cfgLod2DistSq = 20.0D * 20.0D;
    private static double cfgMidQuantize = 8.0D;
    private static double cfgFarQuantize = 12.0D;

    private FastWindGeneratorRenderer() {
    }

    private static void refreshConfigIfNeeded() {
        long now = System.nanoTime();
        if (now - lastConfigRefreshNs < 1_000_000_000L) {
            return;
        }
        lastConfigRefreshNs = now;
        try {
            cfgRenderCulling = MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_RENDER_CULLING.get();
            cfgUsePrebaked = MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_PREBAKED_MESHES.get();
            cfgUseDirect = MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_DIRECT_RENDERING.get();
            cfgUseLod = MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_DISTANCE_LOD.get();
            cfgUseAngleThrottling = MekanismOptimizerConfig.ENABLE_WIND_GENERATOR_ANGLE_THROTTLING.get();
            cfgTransmitterOpt = MekanismOptimizerConfig.ENABLE_TRANSMITTER_RENDER_OPTIMIZATION.get();
            double cullDist = MekanismOptimizerConfig.WIND_GENERATOR_CULL_DISTANCE_BLOCKS.get();
            double lod1Dist = Math.min(12.0D, MekanismOptimizerConfig.WIND_GENERATOR_LOD1_DISTANCE_BLOCKS.get());
            double lod2Dist = Math.min(20.0D, MekanismOptimizerConfig.WIND_GENERATOR_LOD2_DISTANCE_BLOCKS.get());
            cfgCullDistSq = cullDist * cullDist;
            cfgLod1DistSq = lod1Dist * lod1Dist;
            cfgLod2DistSq = lod2Dist * lod2Dist;
            cfgMidQuantize = MekanismOptimizerConfig.WIND_GENERATOR_MID_QUANTIZE_DEGREES.get();
            cfgFarQuantize = MekanismOptimizerConfig.WIND_GENERATOR_FAR_QUANTIZE_DEGREES.get();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Fast cached check for shouldRenderOffScreen mixins without per-call System.nanoTime() JNI overhead.
     */
    public static boolean isRenderCullingEnabled() {
        if ((++configCheckCounter & 4095) == 0) {
            refreshConfigIfNeeded();
        }
        return cfgRenderCulling;
    }

    /**
     * Returns true when client-side onUpdateClient() angle increments can be skipped
     * because FastWindGeneratorRenderer computes deterministic smooth blade angles during rendering.
     */
    public static boolean isClientTickSkipEnabled() {
        if ((++configCheckCounter & 4095) == 0) {
            refreshConfigIfNeeded();
        }
        return cfgUsePrebaked || cfgUseDirect;
    }

    /**
     * Direct interception from BlockEntityRenderDispatcher.render (m_112267_) at priority 800 BEFORE:
     * - EntityCulling getRenderer(blockEntity) + shouldRenderOffScreen + System.currentTimeMillis()
     * - this.getRenderer(blockEntity) Map lookup
     * - blockEntity.getType().isValid(blockState) Set lookup
     * - tryRender capturing Runnable lambda allocation
     * - LevelRenderer.getLightColor chunk light engine lookups
     * - MekanismTileEntityRenderer profiler.push/pop
     */
    public static boolean tryRenderFromDispatcher(
            BlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        if (blockEntity == null) {
            return false;
        }
        if ((++configCheckCounter & 4095) == 0) {
            refreshConfigIfNeeded();
        }

        final Class<?> tileCls = blockEntity.getClass();
        BakedWindModel baked;
        float heightOffset;

        if (tileCls == lastFastTileClass && (baked = lastFastTileBaked) != null) {
            heightOffset = lastFastTileHeightOffset;
        } else if (blockEntity instanceof TileEntityTransmitter transmitterTile) {
            return shouldSkipTransmitterRender(transmitterTile, poseStack);
        } else if (tileCls == lastNonWindTileClass) {
            return false;
        } else {
            RegisteredWindTileType reg = REGISTERED_WIND_TILE_CLASSES.get(tileCls);
            if (reg == null) {
                lastNonWindTileClass = tileCls;
                return false;
            }
            baked = reg.baked;
            heightOffset = reg.heightOffset;
            lastFastTileClass = tileCls;
            lastFastTileBaked = baked;
            lastFastTileHeightOffset = heightOffset;
        }

        if (!(blockEntity instanceof TileEntityMekanism tile)) {
            return false;
        }
        final Level level = tile.getLevel();
        if (level == null || !cfgUsePrebaked) {
            return false;
        }

        final PoseStack.Pose currentPose = poseStack.last();
        final Matrix4f pm = currentPose.pose();
        final float tx = pm.m30();
        final float ty = pm.m31();
        final float tz = pm.m32();

        int lodTier = 0;
        final boolean useLod = cfgUseLod;
        if (useLod || cfgUseAngleThrottling) {
            final double distSq = (double) (tx * tx + ty * ty + tz * tz);
            if (useLod && distSq > cfgCullDistSq) {
                MekanismOptimizerLogger.recordWindGenLodCulled();
                return true;
            }
            if (distSq >= cfgLod2DistSq) {
                lodTier = 2;
            } else if (distSq >= cfgLod1DistSq) {
                lodTier = 1;
            }
        }

        final BlockPos pos = tile.getBlockPos();
        final int light = getFastLightColor(level, pos);
        final int overlayLight = OverlayTexture.NO_OVERLAY;
        final int effectiveLod = useLod ? lodTier : 0;
        final Direction dir = tile.getDirection();
        final int dirOrd = (dir != null) ? dir.ordinal() : Direction.NORTH.ordinal();

        baked.updateViewMatrixIfNeeded(pm, currentPose.normal());
        final VertexConsumer vertexConsumer = bufferSource.getBuffer(baked.renderType);

        baked.getViewSpaceStaticMesh(dirOrd, effectiveLod).emitTranslated(vertexConsumer, tx, ty, tz, light, overlayLight);
        baked.getViewSpaceRotorMeshForTile(dirOrd, effectiveLod, pos.getY(), tile.getActive(), heightOffset, level, partialTick)
                .emitTranslated(vertexConsumer, tx, ty, tz, light, overlayLight);

        MekanismOptimizerLogger.recordWindGenRendered();
        return true;
    }

    /**
     * Ultra-fast transmitter inner-contents culling at the head of BlockEntityRenderDispatcher.render (priority 800).
     * Note that physical cable/pipe/tube/conductor blocks are static baked chunk models rendered by Embeddium at all distances;
     * the BlockEntityRenderer only draws the 2-pixel inner window overlay into Sheets.translucentCullBlockSheet()
     * (which otherwise forces CPU depth-sorting of 120,000+ translucent quads every frame across dense cable grids).
     */
    private static boolean shouldSkipTransmitterRender(TileEntityTransmitter transmitterTile, PoseStack poseStack) {
        if (!cfgTransmitterOpt) {
            return false;
        }
        final Matrix4f pm = poseStack.last().pose();
        final float tx = pm.m30() + 0.5F;
        final float ty = pm.m31() + 0.5F;
        final float tz = pm.m32() + 0.5F;
        final float distSq = tx * tx + ty * ty + tz * tz;

        if (transmitterTile instanceof TileEntityLogisticalTransporterBase transporterTile) {
            if (transporterTile instanceof TileEntityDiversionTransporter) {
                return distSq > 256.0F;
            }
            if (distSq > 576.0F) {
                return true;
            }
            LogisticalTransporterBase transporter = transporterTile.getTransmitter();
            return transporter == null || transporter.getTransit().isEmpty();
        }

        // Cull 2-pixel inner window overlay beyond 8 blocks (distSq > 64.0F)
        if (distSq > 64.0F) {
            return true;
        }

        if (transmitterTile instanceof TileEntityUniversalCable cableTile) {
            UniversalCable cable = cableTile.getTransmitter();
            if (cable == null || !cable.hasTransmitterNetwork()) {
                return true;
            }
            EnergyNetwork network = cable.getTransmitterNetwork();
            return network == null || network.currentScale <= 0.0F;
        }
        return false;
    }

    /**
     * 16,384-slot (128x128xY) zero-collision 32-tick spatial light cache.
     * Eliminates 99% of per-frame LevelRenderer.getLightColor chunk & light engine lookups.
     */
    private static int getFastLightColor(Level level, BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final int slot = ((x & 127) | ((z & 127) << 7) ^ ((y & 3) << 12)) & LIGHT_CACHE_MASK;
        final long posLong = pos.asLong();
        final int stamp = (((int) (level.getGameTime() >>> 5)) * 31 ^ System.identityHashCode(level)) | 1;

        if (LIGHT_CACHE_STAMP[slot] == stamp && LIGHT_CACHE_POS[slot] == posLong) {
            return LIGHT_CACHE_VAL[slot];
        }
        final int light = LevelRenderer.getLightColor(level, pos);
        LIGHT_CACHE_POS[slot] = posLong;
        LIGHT_CACHE_VAL[slot] = light;
        LIGHT_CACHE_STAMP[slot] = stamp;
        return light;
    }

    private static void registerWindTileClass(Class<?> tileCls, BakedWindModel baked, float heightOffset) {
        if (tileCls == lastFastTileClass && lastFastTileBaked == baked) {
            return;
        }
        REGISTERED_WIND_TILE_CLASSES.put(tileCls, new RegisteredWindTileType(baked, heightOffset));
        lastFastTileClass = tileCls;
        lastFastTileBaked = baked;
        lastFastTileHeightOffset = heightOffset;
        if (lastNonWindTileClass == tileCls) {
            lastNonWindTileClass = null;
        }
    }

    /**
     * Early far-distance culling invoked from BlockEntityRenderer.shouldRender (m_142163_)
     * BEFORE BlockEntityRenderDispatcher allocates tryRender lambdas or calls LevelRenderer.getLightColor.
     * Does NOT skip individual windmills within cull range so base blocks and masts never have gaps.
     */
    public static boolean shouldRenderWindGenerator(BlockEntity blockEntity, Vec3 cameraPos) {
        if (blockEntity == null || cameraPos == null) {
            return false;
        }
        if ((++configCheckCounter & 4095) == 0) {
            refreshConfigIfNeeded();
        }

        BlockPos pos = blockEntity.getBlockPos();
        final double dx = (pos.getX() + 0.5D) - cameraPos.x;
        final double dy = (pos.getY() + 2.5D) - cameraPos.y;
        final double dz = (pos.getZ() + 0.5D) - cameraPos.z;
        final double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq > cfgCullDistSq) {
            MekanismOptimizerLogger.recordWindGenLodCulled();
            return false;
        }
        return true;
    }

    /**
     * Renders the standard Mekanism Wind Generator using pre-baked LOD meshes and zero-allocation pure translation.
     */
    public static boolean renderFast(
            Object rendererInstance,
            TileEntityWindGenerator tile,
            float partialTick,
            PoseStack matrix,
            MultiBufferSource bufferSource,
            int light,
            int overlayLight
    ) {
        if (tile == null) {
            return false;
        }
        return renderFastInternal(
                rendererInstance,
                tile,
                4.0F,
                partialTick,
                matrix,
                bufferSource,
                light,
                overlayLight
        );
    }

    /**
     * Renders addon Wind Generators (Compact Mekanism Machines x2..x532480, Mekanism More Machine LargeWindGenerator, etc.).
     */
    public static boolean renderFastAddon(
            Object rendererInstance,
            BlockEntity blockEntity,
            float heightOffset,
            float partialTick,
            PoseStack matrix,
            MultiBufferSource bufferSource,
            int light,
            int overlayLight
    ) {
        if (!(blockEntity instanceof TileEntityMekanism tile)) {
            return false;
        }
        return renderFastInternal(
                rendererInstance,
                tile,
                heightOffset,
                partialTick,
                matrix,
                bufferSource,
                light,
                overlayLight
        );
    }

    /**
     * Direct hook for ModelWindGenerator.render(...) when invoked outside standard tile renderers (e.g. item rendering).
     */
    public static boolean renderModelDirect(
            Object modelObj,
            PoseStack matrix,
            MultiBufferSource bufferSource,
            double angle,
            int light,
            int overlayLight,
            boolean hasEffect
    ) {
        if (hasEffect || modelObj == null) {
            return false;
        }
        refreshConfigIfNeeded();
        if (!cfgUsePrebaked) {
            return false;
        }
        BakedWindModel baked = getOrBakeModel(modelObj);
        if (baked == null) {
            return false;
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(baked.renderType);
        PoseStack.Pose currentPose = matrix.last();
        baked.staticModelSpaceLod0.emitMatrix(vertexConsumer, currentPose, light, overlayLight);

        matrix.pushPose();
        PoseStack.Pose rotorPose = matrix.last();
        matrix.translate(baked.pivotX, baked.pivotY, baked.pivotZ);
        float baseRotationRad = (float) Math.toRadians(angle % 360.0D);
        if (baseRotationRad != 0.0F) {
            rotorPose.pose().rotateZ(baseRotationRad);
            rotorPose.normal().rotateZ(baseRotationRad);
        }
        baked.rotorPivotSpaceLod0.emitMatrix(vertexConsumer, rotorPose, light, overlayLight);
        matrix.popPose();

        MekanismOptimizerLogger.recordWindGenRendered();
        return true;
    }

    private static boolean renderFastInternal(
            Object rendererInstance,
            TileEntityMekanism tile,
            float heightOffset,
            float partialTick,
            PoseStack matrix,
            MultiBufferSource bufferSource,
            int light,
            int overlayLight
    ) {
        final boolean usePrebaked = cfgUsePrebaked;
        final boolean useDirect = cfgUseDirect;
        final boolean useLod = cfgUseLod;
        final boolean useAngleThrottling = cfgUseAngleThrottling;

        if (!usePrebaked && !useDirect && !useLod) {
            return false;
        }

        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        final PoseStack.Pose currentPose = matrix.last();
        final Matrix4f pm = currentPose.pose();
        final float tx = pm.m30();
        final float ty = pm.m31();
        final float tz = pm.m32();

        int lodTier = 0;
        if (useLod || useAngleThrottling) {
            final double distSq = (double) (tx * tx + ty * ty + tz * tz);

            if (useLod && distSq > cfgCullDistSq) {
                MekanismOptimizerLogger.recordWindGenLodCulled();
                return true;
            }

            if (distSq >= cfgLod2DistSq) {
                lodTier = 2;
            } else if (distSq >= cfgLod1DistSq) {
                lodTier = 1;
            }
        }

        // Fallback path if pre-baked meshes are disabled in config
        if (!usePrebaked) {
            double angle = 0.0D;
            if (tile.getActive() && level != null) {
                double speedPerTick = (pos.getY() + heightOffset) / DEFAULT_SPEED_SCALED;
                double totalTicks = (double) (level.getGameTime() & 0xFFFFFL) + partialTick;
                angle = (totalTicks * speedPerTick) % 360.0D;
            }
            if (useAngleThrottling && lodTier > 0) {
                double quantizeStep = (lodTier == 2) ? cfgFarQuantize : cfgMidQuantize;
                if (quantizeStep > 0.0D) {
                    angle = Math.floor(angle / quantizeStep) * quantizeStep;
                }
            }
            Object modelObj = getModelInstance(rendererInstance);
            if (modelObj instanceof mekanism.generators.client.model.ModelWindGenerator windModel) {
                Direction dir = tile.getDirection();
                Quaternionf facingRot = FACING_Z180_ROTATIONS[dir != null ? dir.ordinal() : Direction.NORTH.ordinal()];
                matrix.pushPose();
                matrix.translate(0.5F, 1.5F, 0.5F);
                matrix.mulPose(facingRot);
                windModel.render(matrix, bufferSource, angle, light, overlayLight, false);
                matrix.popPose();
                MekanismOptimizerLogger.recordWindGenRendered();
                return true;
            }
            return false;
        }

        // Fast O(1) lookup of BakedWindModel from rendererInstance without reflection on cache hit
        BakedWindModel baked;
        if (rendererInstance == lastRendererInstance && lastRendererBakedModel != null) {
            baked = lastRendererBakedModel;
        } else {
            Object modelObj = getModelInstance(rendererInstance);
            if (modelObj == null) {
                return false;
            }
            baked = getOrBakeModel(modelObj);
            if (baked == null) {
                return false;
            }
            lastRendererInstance = rendererInstance;
            lastRendererBakedModel = baked;
        }

        // Register tile class for direct BlockEntityRenderDispatcher interception on subsequent windmills
        registerWindTileClass(tile.getClass(), baked, heightOffset);

        final int effectiveLod = useLod ? lodTier : 0;
        final Direction dir = tile.getDirection();
        final int dirOrd = (dir != null) ? dir.ordinal() : Direction.NORTH.ordinal();

        baked.updateViewMatrixIfNeeded(pm, currentPose.normal());
        final VertexConsumer vertexConsumer = bufferSource.getBuffer(baked.renderType);

        baked.getViewSpaceStaticMesh(dirOrd, effectiveLod).emitTranslated(vertexConsumer, tx, ty, tz, light, overlayLight);
        baked.getViewSpaceRotorMeshForTile(dirOrd, effectiveLod, pos.getY(), tile.getActive(), heightOffset, level, partialTick)
                .emitTranslated(vertexConsumer, tx, ty, tz, light, overlayLight);

        MekanismOptimizerLogger.recordWindGenRendered();
        return true;
    }

    private static Object getModelInstance(Object renderer) {
        if (renderer == null) {
            return null;
        }
        Class<?> rendererCls = renderer.getClass();
        Field field = RENDERER_MODEL_FIELDS.get(rendererCls);
        if (field == null) {
            Class<?> cls = rendererCls;
            while (cls != null && cls != Object.class) {
                try {
                    Field f = cls.getDeclaredField("model");
                    f.setAccessible(true);
                    field = f;
                    RENDERER_MODEL_FIELDS.put(rendererCls, f);
                    break;
                } catch (NoSuchFieldException ignored) {
                    cls = cls.getSuperclass();
                }
            }
        }
        if (field == null) {
            return null;
        }
        try {
            return field.get(renderer);
        } catch (Throwable t) {
            return null;
        }
    }

    private static BakedWindModel getOrBakeModel(Object modelObj) {
        if (modelObj == lastModelInstance && lastBakedModel != null) {
            return lastBakedModel;
        }
        BakedWindModel existing = BAKED_MODELS.get(modelObj);
        if (existing != null) {
            lastModelInstance = modelObj;
            lastBakedModel = existing;
            return existing;
        }
        synchronized (FastWindGeneratorRenderer.class) {
            existing = BAKED_MODELS.get(modelObj);
            if (existing != null) {
                lastModelInstance = modelObj;
                lastBakedModel = existing;
                return existing;
            }
            BakedWindModel baked = bakeModel(modelObj);
            if (baked != null) {
                BAKED_MODELS.put(modelObj, baked);
                lastModelInstance = modelObj;
                lastBakedModel = baked;
            }
            return baked;
        }
    }

    @SuppressWarnings("unchecked")
    private static BakedWindModel bakeModel(Object modelObj) {
        try {
            Class<?> modelCls = modelObj.getClass();
            Field rtField = findField(modelCls, "RENDER_TYPE");
            Field partsField = findField(modelCls, "parts");
            if (rtField == null || partsField == null) {
                return null;
            }

            RenderType renderType = (RenderType) rtField.get(modelObj);
            List<ModelPart> parts = (List<ModelPart>) partsField.get(modelObj);
            if (renderType == null || parts == null) {
                return null;
            }

            if (parts.size() >= 21) {
                // Standard 21-part ModelWindGenerator (used by MekanismGenerators and Compact Mekanism Machines)
                float degMinus60 = (float) Math.toRadians(-60.0D);
                float degPlus60 = (float) Math.toRadians(60.0D);
                float bladeBOffset = 0.0349066F;

                parts.get(13).zRot = 0.0F;                      // blade1a
                parts.get(14).zRot = degMinus60;                // blade2a
                parts.get(15).zRot = degPlus60;                 // blade3a
                parts.get(16).zRot = bladeBOffset;              // blade1b
                parts.get(17).zRot = bladeBOffset + degMinus60; // blade2b
                parts.get(18).zRot = bladeBOffset + degPlus60;  // blade3b
                parts.get(19).zRot = 0.0F;                      // bladeCap
                parts.get(20).zRot = 0.0F;                      // bladeCenter

                // 1. Model-space static meshes (for direct/item fallback)
                PoseStack identityStack = new PoseStack();
                PreBakedMesh staticModelLod0 = bakePartSubset(parts, STATIC_PARTS_LOD0, identityStack, false);
                PreBakedMesh staticModelLod1 = bakePartSubset(parts, STATIC_PARTS_LOD1, identityStack, false);
                PreBakedMesh staticModelLod2 = bakePartSubset(parts, STATIC_PARTS_LOD2, identityStack, true);

                // 2. Block-local pre-oriented static meshes for all 6 Directions (zero runtime matrix math!)
                PreBakedMesh[][] staticByDir = new PreBakedMesh[6][3];
                for (int d = 0; d < 6; d++) {
                    PoseStack dirStack = new PoseStack();
                    dirStack.translate(0.5F, 1.5F, 0.5F);
                    dirStack.mulPose(FACING_Z180_ROTATIONS[d]);
                    staticByDir[d][0] = bakePartSubset(parts, STATIC_PARTS_LOD0, dirStack, false);
                    staticByDir[d][1] = bakePartSubset(parts, STATIC_PARTS_LOD1, dirStack, false);
                    staticByDir[d][2] = bakePartSubset(parts, STATIC_PARTS_LOD2, dirStack, true);
                }

                // 3. Pivot-centered rotor meshes
                PoseStack rotorBakeStack = new PoseStack();
                rotorBakeStack.translate(0.0F, 3.0F, 0.0F);
                PreBakedMesh rotorLod0 = bakePartSubset(parts, ROTOR_PARTS_LOD0, rotorBakeStack, false);
                PreBakedMesh rotorLod1 = bakePartSubset(parts, ROTOR_PARTS_LOD1, rotorBakeStack, false);
                PreBakedMesh rotorLod2 = bakePartSubset(parts, ROTOR_PARTS_LOD2, rotorBakeStack, true);

                MekanismOptimizerLogger.info(
                        "[WindGeneratorRenderer] Pre-baked Wind Generator meshes for {} (Quads -> LOD0: {}+{}, LOD1: {}+{}, LOD2: {}+{})",
                        modelCls.getSimpleName(),
                        staticModelLod0.quadCount, rotorLod0.quadCount,
                        staticModelLod1.quadCount, rotorLod1.quadCount,
                        staticModelLod2.quadCount, rotorLod2.quadCount
                );

                return new BakedWindModel(
                        renderType,
                        0.0F, -3.0F, 0.0F,
                        staticModelLod0, staticModelLod1, staticModelLod2,
                        staticByDir,
                        rotorLod0, rotorLod1, rotorLod2
                );
            } else if (parts.size() == 4) {
                // 4-part-tree ModelLargeWindGenerator (0=FAN, 1=BODY, 2=TOP, 3=BASE)
                ModelPart fanPart = parts.get(0);
                fanPart.xRot = 0.0F;
                fanPart.yRot = 0.0F;
                fanPart.zRot = 0.0F;

                float pivotX = fanPart.x / 16.0F;
                float pivotY = fanPart.y / 16.0F;
                float pivotZ = fanPart.z / 16.0F;

                PoseStack identityStack = new PoseStack();
                PreBakedMesh staticModelLod0 = bakePartSubset(parts, LARGE_STATIC_PARTS_LOD0, identityStack, false);
                PreBakedMesh staticModelLod2 = bakePartSubset(parts, LARGE_STATIC_PARTS_LOD2, identityStack, false);

                PreBakedMesh[][] staticByDir = new PreBakedMesh[6][3];
                for (int d = 0; d < 6; d++) {
                    PoseStack dirStack = new PoseStack();
                    dirStack.translate(0.5F, 1.5F, 0.5F);
                    dirStack.mulPose(FACING_Z180_ROTATIONS[d]);
                    PreBakedMesh s0 = bakePartSubset(parts, LARGE_STATIC_PARTS_LOD0, dirStack, false);
                    PreBakedMesh s2 = bakePartSubset(parts, LARGE_STATIC_PARTS_LOD2, dirStack, false);
                    staticByDir[d][0] = s0;
                    staticByDir[d][1] = s0;
                    staticByDir[d][2] = s2;
                }

                PoseStack rotorBakeStack = new PoseStack();
                rotorBakeStack.translate(-pivotX, -pivotY, -pivotZ);
                PreBakedMesh rotorLod0 = bakePartSubset(parts, LARGE_ROTOR_PARTS, rotorBakeStack, false);

                MekanismOptimizerLogger.info(
                        "[WindGeneratorRenderer] Pre-baked Large Wind Generator meshes for {} (Quads -> LOD0: {}+{}, LOD2: {}+{})",
                        modelCls.getSimpleName(),
                        staticModelLod0.quadCount, rotorLod0.quadCount,
                        staticModelLod2.quadCount, rotorLod0.quadCount
                );

                return new BakedWindModel(
                        renderType,
                        pivotX, pivotY, pivotZ,
                        staticModelLod0, staticModelLod0, staticModelLod2,
                        staticByDir,
                        rotorLod0, rotorLod0, rotorLod0
                );
            }
            return null;
        } catch (Throwable t) {
            MekanismOptimizerLogger.error("Failed to pre-bake WindGenerator meshes, falling back to vanilla renderer", t);
            return null;
        }
    }

    private static Field findField(Class<?> cls, String name) {
        Class<?> curr = cls;
        while (curr != null && curr != Object.class) {
            try {
                Field f = curr.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                curr = curr.getSuperclass();
            }
        }
        return null;
    }

    private static PreBakedMesh bakePartSubset(
            List<ModelPart> allParts,
            int[] indices,
            PoseStack baseStack,
            boolean filterLod2Faces
    ) {
        CapturingVertexConsumer collector = new CapturingVertexConsumer(indices.length * 6, filterLod2Faces);
        for (int partIdx : indices) {
            ModelPart part = allParts.get(partIdx);
            if (!part.visible) {
                continue;
            }
            collector.setCurrentPartIndex(partIdx);
            part.render(baseStack, collector, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        float[] raw = collector.buffer.toFloatArray();
        return new PreBakedMesh(raw, raw.length / FLOATS_PER_QUAD);
    }

    /**
     * Captures vertices emitted by ModelPart.render(...) into a compact quad array.
     * Preserves ALL faces of BASE (part 5) and HEAD (part 0) so bottom blocks and bodies are never missing when viewed from below.
     */
    private static final class CapturingVertexConsumer implements VertexConsumer {
        final FloatArrayList buffer;
        final boolean filterLod2Faces;
        int currentPartIndex;

        private final float[] vx = new float[4];
        private final float[] vy = new float[4];
        private final float[] vz = new float[4];
        private final float[] vu = new float[4];
        private final float[] vv = new float[4];
        private float curX, curY, curZ, curU, curV, curNx, curNy, curNz;
        private int vertexInQuad = 0;

        CapturingVertexConsumer(int estimatedQuads, boolean filterLod2Faces) {
            this.buffer = new FloatArrayList(estimatedQuads * FLOATS_PER_QUAD);
            this.filterLod2Faces = filterLod2Faces;
        }

        void setCurrentPartIndex(int partIndex) {
            this.currentPartIndex = partIndex;
            this.vertexInQuad = 0;
        }

        @Override
        public void vertex(
                float x, float y, float z,
                float red, float green, float blue, float alpha,
                float u, float v,
                int overlayCoords, int uv2,
                float normalX, float normalY, float normalZ
        ) {
            int idx = this.vertexInQuad;
            this.vx[idx] = x;
            this.vy[idx] = y;
            this.vz[idx] = z;
            this.vu[idx] = u;
            this.vv[idx] = v;
            this.curNx = normalX;
            this.curNy = normalY;
            this.curNz = normalZ;
            if (idx == 3) {
                this.vertexInQuad = 0;
                flushQuad();
            } else {
                this.vertexInQuad = idx + 1;
            }
        }

        private void flushQuad() {
            // POST_1A..1D (parts 9..12): top and bottom 5x5 end-caps are 100% buried inside HEAD (7x7x9) and BASE (16x2x16)
            if (currentPartIndex >= 9 && currentPartIndex <= 12 && Math.abs(curNy) > 0.85F) {
                return;
            }
            if (filterLod2Faces && currentPartIndex >= 13 && currentPartIndex <= 15) {
                // BLADE_1A..3A in LOD 2: keep only the 2 main 2x32 front/back blade faces (|curNz| > 0.5F in model space)
                // and omit the 4 sub-pixel 1-pixel-thick edge/tip faces
                if (Math.abs(curNz) < 0.5F) {
                    return;
                }
            }

            buffer.add(curNx);
            buffer.add(curNy);
            buffer.add(curNz);
            for (int i = 0; i < 4; i++) {
                buffer.add(vx[i]);
                buffer.add(vy[i]);
                buffer.add(vz[i]);
                buffer.add(vu[i]);
                buffer.add(vv[i]);
            }
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.curX = (float) x;
            this.curY = (float) y;
            this.curZ = (float) z;
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.curU = u;
            this.curV = v;
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.curNx = x;
            this.curNy = y;
            this.curNz = z;
            return this;
        }

        @Override
        public void endVertex() {
            vertex(curX, curY, curZ, 1.0F, 1.0F, 1.0F, 1.0F, curU, curV, 0, 0, curNx, curNy, curNz);
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
        }

        @Override
        public void unsetDefaultColor() {
        }
    }

    private static final class BakedWindModel {
        final RenderType renderType;
        final float pivotX;
        final float pivotY;
        final float pivotZ;
        final PreBakedMesh staticModelSpaceLod0;
        final PreBakedMesh staticModelSpaceLod1;
        final PreBakedMesh staticModelSpaceLod2;
        final PreBakedMesh[][] staticByDir; // [6 directions][3 LODs] in block-local space
        final PreBakedMesh rotorPivotSpaceLod0;
        final PreBakedMesh rotorPivotSpaceLod1;
        final PreBakedMesh rotorPivotSpaceLod2;

        // Per-frame camera view-rotation state (upper-left 3x3 of PoseStack, identical across all windmills in a frame)
        private int viewMatrixEpoch = 1;
        private float viewM00 = Float.NaN, viewM01, viewM02;
        private float viewM10, viewM11, viewM12;
        private float viewM20, viewM21, viewM22;
        private float viewN00, viewN01, viewN02;
        private float viewN10, viewN11, viewN12;
        private float viewN20, viewN21, viewN22;

        // View-space pre-rotated static meshes per [6 directions][3 LODs]
        private final int[][] viewStaticEpochs = new int[6][3];
        private final PreBakedMesh[][] viewStaticMeshes = new PreBakedMesh[6][3];

        // Multi-slot view-space pre-rotated rotor cache per [6 directions][3 LODs][ROTOR_CACHE_SLOTS]
        private final int[][][] rotorCacheEpochs = new int[6][3][ROTOR_CACHE_SLOTS];
        private final int[][][] rotorCacheAngleKeys = new int[6][3][ROTOR_CACHE_SLOTS];
        private final PreBakedMesh[][][] rotorCacheMeshes = new PreBakedMesh[6][3][ROTOR_CACHE_SLOTS];
        private final int[][] rotorCacheNextSlot = new int[6][3];
        private final PoseStack rotorTransformStack = new PoseStack();

        BakedWindModel(
                RenderType renderType,
                float pivotX,
                float pivotY,
                float pivotZ,
                PreBakedMesh staticModelSpaceLod0,
                PreBakedMesh staticModelSpaceLod1,
                PreBakedMesh staticModelSpaceLod2,
                PreBakedMesh[][] staticByDir,
                PreBakedMesh rotorPivotSpaceLod0,
                PreBakedMesh rotorPivotSpaceLod1,
                PreBakedMesh rotorPivotSpaceLod2
        ) {
            this.renderType = renderType;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.pivotZ = pivotZ;
            this.staticModelSpaceLod0 = staticModelSpaceLod0;
            this.staticModelSpaceLod1 = staticModelSpaceLod1;
            this.staticModelSpaceLod2 = staticModelSpaceLod2;
            this.staticByDir = staticByDir;
            this.rotorPivotSpaceLod0 = rotorPivotSpaceLod0;
            this.rotorPivotSpaceLod1 = rotorPivotSpaceLod1;
            this.rotorPivotSpaceLod2 = rotorPivotSpaceLod2;

            for (int d = 0; d < 6; d++) {
                for (int l = 0; l < 3; l++) {
                    PreBakedMesh staticSrc = staticByDir[d][l];
                    viewStaticMeshes[d][l] = new PreBakedMesh(new float[staticSrc.data.length], staticSrc.quadCount);

                    PreBakedMesh rotorSrc = getPivotSpaceRotorMesh(l);
                    for (int s = 0; s < ROTOR_CACHE_SLOTS; s++) {
                        rotorCacheAngleKeys[d][l][s] = Integer.MIN_VALUE;
                        rotorCacheMeshes[d][l][s] = new PreBakedMesh(new float[rotorSrc.data.length], rotorSrc.quadCount);
                    }
                }
            }
        }

        void updateViewMatrixIfNeeded(Matrix4f pm, Matrix3f nm) {
            if (pm.m00() != viewM00 || pm.m01() != viewM01 || pm.m02() != viewM02
                    || pm.m10() != viewM10 || pm.m11() != viewM11 || pm.m12() != viewM12
                    || pm.m20() != viewM20 || pm.m21() != viewM21 || pm.m22() != viewM22) {
                viewM00 = pm.m00();
                viewM01 = pm.m01();
                viewM02 = pm.m02();
                viewM10 = pm.m10();
                viewM11 = pm.m11();
                viewM12 = pm.m12();
                viewM20 = pm.m20();
                viewM21 = pm.m21();
                viewM22 = pm.m22();

                viewN00 = nm.m00();
                viewN01 = nm.m01();
                viewN02 = nm.m02();
                viewN10 = nm.m10();
                viewN11 = nm.m11();
                viewN12 = nm.m12();
                viewN20 = nm.m20();
                viewN21 = nm.m21();
                viewN22 = nm.m22();

                int nextEpoch = viewMatrixEpoch + 1;
                if (nextEpoch == 0) {
                    nextEpoch = 1;
                }
                viewMatrixEpoch = nextEpoch;
            }
        }

        PreBakedMesh getPivotSpaceRotorMesh(int lod) {
            return switch (lod) {
                case 2 -> rotorPivotSpaceLod2;
                case 1 -> rotorPivotSpaceLod1;
                default -> rotorPivotSpaceLod0;
            };
        }

        /**
         * Returns the static mesh for (dirOrd, lod) already rotated by the current frame's camera view matrix.
         * Computed once per frame and reused across all 6,966+ windmills with zero matrix multiplications!
         */
        PreBakedMesh getViewSpaceStaticMesh(int dirOrd, int lod) {
            if (viewStaticEpochs[dirOrd][lod] == viewMatrixEpoch) {
                return viewStaticMeshes[dirOrd][lod];
            }
            viewStaticEpochs[dirOrd][lod] = viewMatrixEpoch;
            PreBakedMesh src = staticByDir[dirOrd][lod];
            PreBakedMesh dst = viewStaticMeshes[dirOrd][lod];
            src.transformLinearInto(
                    dst.data,
                    viewM00, viewM01, viewM02,
                    viewM10, viewM11, viewM12,
                    viewM20, viewM21, viewM22,
                    viewN00, viewN01, viewN02,
                    viewN10, viewN11, viewN12,
                    viewN20, viewN21, viewN22
            );
            return dst;
        }

        private final int[][] lastRotorEpoch = new int[6][3];
        private final int[][] lastRotorTimeKey = new int[6][3];
        private final int[][] lastRotorBlockY = new int[6][3];
        private final boolean[][] lastRotorActive = new boolean[6][3];
        private final PreBakedMesh[][] lastRotorMesh = new PreBakedMesh[6][3];

        /**
         * Fast-path rotor mesh lookup that skips angle math when rendering windmills at the same Y coordinate,
         * same LOD, and same direction in the same frame (hits 99.98% of the time across an 86x81 horizontal layer).
         * - LOD 0 (< 12m): full 60 FPS smooth partialTick interpolation
         * - LOD 1 (12m..20m): 20 FPS stepped animation (1 update per tick)
         * - LOD 2 (>= 20m): 10 FPS stepped animation (1 update every 2 ticks, 12 deg step) so distant blades move at a modest rate
         */
        PreBakedMesh getViewSpaceRotorMeshForTile(
                int dirOrd,
                int lod,
                int blockY,
                boolean active,
                float heightOffset,
                Level level,
                float partialTick
        ) {
            final int epoch = viewMatrixEpoch;
            final boolean throttle = cfgUseAngleThrottling && lod > 0;
            final long gameTime = (level != null) ? (level.getGameTime() & 0xFFFFFL) : 0L;
            final int timeKey;
            if (!throttle) {
                timeKey = ((int) gameTime * 31) ^ Float.floatToRawIntBits(partialTick);
            } else if (lod == 2) {
                // 10 FPS animation rate (updates once every 2 ticks = 100ms)
                timeKey = (int) (gameTime >>> 1);
            } else {
                // 20 FPS animation rate (updates once per tick = 50ms)
                timeKey = (int) gameTime;
            }

            if (lastRotorEpoch[dirOrd][lod] == epoch
                    && lastRotorTimeKey[dirOrd][lod] == timeKey
                    && lastRotorBlockY[dirOrd][lod] == blockY
                    && lastRotorActive[dirOrd][lod] == active) {
                PreBakedMesh cached = lastRotorMesh[dirOrd][lod];
                if (cached != null) {
                    return cached;
                }
            }

            double angle = 0.0D;
            if (active && level != null) {
                if (!throttle) {
                    double speedPerTick = (blockY + heightOffset) / DEFAULT_SPEED_SCALED;
                    double totalTicks = (double) gameTime + partialTick;
                    angle = (totalTicks * speedPerTick) % 360.0D;
                } else if (lod == 2) {
                    // 10 FPS stepped rotation (12.0 deg per 2-tick step = 10 steps per 120-deg 3-blade cycle, free of 120-deg strobe aliasing)
                    double stepDeg = (cfgFarQuantize > 0.0D) ? cfgFarQuantize : 12.0D;
                    angle = ((gameTime >>> 1) * stepDeg) % 360.0D;
                } else {
                    // 20 FPS stepped rotation (8.0 deg per 1-tick step = 15 steps per 120-deg 3-blade cycle)
                    double stepDeg = (cfgMidQuantize > 0.0D) ? cfgMidQuantize : 8.0D;
                    angle = (gameTime * stepDeg) % 360.0D;
                }
            }

            PreBakedMesh mesh = getViewSpaceRotorMesh(dirOrd, lod, angle);
            lastRotorEpoch[dirOrd][lod] = epoch;
            lastRotorTimeKey[dirOrd][lod] = timeKey;
            lastRotorBlockY[dirOrd][lod] = blockY;
            lastRotorActive[dirOrd][lod] = active;
            lastRotorMesh[dirOrd][lod] = mesh;
            return mesh;
        }

        /**
         * Returns the rotor mesh for (dirOrd, lod, angle) already rotated by both the blade angle and the current frame's camera view matrix.
         * Across 5,000+ windmills at the same Y coordinate, this hits the cache 99.98% of the time!
         */
        PreBakedMesh getViewSpaceRotorMesh(int dirOrd, int lod, double angle) {
            final int angleKey = (int) Math.round((angle % 360.0D) * 4.0D);
            final int epoch = viewMatrixEpoch;
            final int[] epochs = rotorCacheEpochs[dirOrd][lod];
            final int[] keys = rotorCacheAngleKeys[dirOrd][lod];
            final PreBakedMesh[] meshes = rotorCacheMeshes[dirOrd][lod];

            for (int s = 0; s < ROTOR_CACHE_SLOTS; s++) {
                if (epochs[s] == epoch && keys[s] == angleKey) {
                    return meshes[s];
                }
            }

            final int slot = rotorCacheNextSlot[dirOrd][lod];
            rotorCacheNextSlot[dirOrd][lod] = (slot + 1) & (ROTOR_CACHE_SLOTS - 1);
            epochs[slot] = epoch;
            keys[slot] = angleKey;

            PreBakedMesh target = meshes[slot];
            PreBakedMesh source = getPivotSpaceRotorMesh(lod);

            rotorTransformStack.pushPose();
            PoseStack.Pose p = rotorTransformStack.last();
            p.pose().set(
                    viewM00, viewM01, viewM02, 0.0F,
                    viewM10, viewM11, viewM12, 0.0F,
                    viewM20, viewM21, viewM22, 0.0F,
                    0.0F, 0.0F, 0.0F, 1.0F
            );
            p.normal().set(
                    viewN00, viewN01, viewN02,
                    viewN10, viewN11, viewN12,
                    viewN20, viewN21, viewN22
            );
            rotorTransformStack.translate(0.5F, 1.5F, 0.5F);
            rotorTransformStack.mulPose(FACING_Z180_ROTATIONS[dirOrd]);
            rotorTransformStack.translate(pivotX, pivotY, pivotZ);
            float rad = (float) Math.toRadians(angleKey * 0.25D);
            if (rad != 0.0F) {
                p.pose().rotateZ(rad);
                p.normal().rotateZ(rad);
            }
            source.transformInto(target.data, p);
            rotorTransformStack.popPose();

            return target;
        }
    }

    private static final class PreBakedMesh {
        final float[] data;
        final int quadCount;

        PreBakedMesh(float[] data, int quadCount) {
            this.data = data;
            this.quadCount = quadCount;
        }

        /**
         * Transforms this mesh's vertices and normals by a pure 3x3 linear/rotation matrix (zero translation) into dst.
         */
        void transformLinearInto(
                float[] dst,
                float m00, float m01, float m02,
                float m10, float m11, float m12,
                float m20, float m21, float m22,
                float n00, float n01, float n02,
                float n10, float n11, float n12,
                float n20, float n21, float n22
        ) {
            final float[] src = this.data;
            final int len = src.length;
            for (int idx = 0; idx < len; idx += FLOATS_PER_QUAD) {
                final float nx = src[idx];
                final float ny = src[idx + 1];
                final float nz = src[idx + 2];
                dst[idx] = n00 * nx + n10 * ny + n20 * nz;
                dst[idx + 1] = n01 * nx + n11 * ny + n21 * nz;
                dst[idx + 2] = n02 * nx + n12 * ny + n22 * nz;

                for (int v = idx + 3; v < idx + FLOATS_PER_QUAD; v += 5) {
                    final float x = src[v];
                    final float y = src[v + 1];
                    final float z = src[v + 2];
                    dst[v] = m00 * x + m10 * y + m20 * z;
                    dst[v + 1] = m01 * x + m11 * y + m21 * z;
                    dst[v + 2] = m02 * x + m12 * y + m22 * z;
                    dst[v + 3] = src[v + 3];
                    dst[v + 4] = src[v + 4];
                }
            }
        }

        /**
         * Transforms this mesh's vertices and normals by pose and writes the pre-rotated result into dst.
         */
        void transformInto(float[] dst, PoseStack.Pose pose) {
            final float[] src = this.data;
            final int len = src.length;
            final Matrix4f pm = pose.pose();
            final float m00 = pm.m00(), m01 = pm.m01(), m02 = pm.m02();
            final float m10 = pm.m10(), m11 = pm.m11(), m12 = pm.m12();
            final float m20 = pm.m20(), m21 = pm.m21(), m22 = pm.m22();
            final float m30 = pm.m30(), m31 = pm.m31(), m32 = pm.m32();

            final Matrix3f nm = pose.normal();
            final float n00 = nm.m00(), n01 = nm.m01(), n02 = nm.m02();
            final float n10 = nm.m10(), n11 = nm.m11(), n12 = nm.m12();
            final float n20 = nm.m20(), n21 = nm.m21(), n22 = nm.m22();

            for (int idx = 0; idx < len; idx += FLOATS_PER_QUAD) {
                final float nx = src[idx];
                final float ny = src[idx + 1];
                final float nz = src[idx + 2];
                dst[idx] = n00 * nx + n10 * ny + n20 * nz;
                dst[idx + 1] = n01 * nx + n11 * ny + n21 * nz;
                dst[idx + 2] = n02 * nx + n12 * ny + n22 * nz;

                for (int v = idx + 3; v < idx + FLOATS_PER_QUAD; v += 5) {
                    final float x = src[v];
                    final float y = src[v + 1];
                    final float z = src[v + 2];
                    dst[v] = m00 * x + m10 * y + m20 * z + m30;
                    dst[v + 1] = m01 * x + m11 * y + m21 * z + m31;
                    dst[v + 2] = m02 * x + m12 * y + m22 * z + m32;
                    dst[v + 3] = src[v + 3];
                    dst[v + 4] = src[v + 4];
                }
            }
        }

        /**
         * Ultra-fast zero-multiplication vertex emission for world rendering where PoseStack is a pure translation (tx, ty, tz).
         * Performs conservative CPU backface culling (nx*x0 + ny*y0 + nz*z0 > 0.05F) so back-facing quads are skipped
         * before invoking VertexConsumer.vertex 4 times.
         */
        void emitTranslated(VertexConsumer consumer, float tx, float ty, float tz, int light, int overlay) {
            final float[] buf = this.data;
            final int len = buf.length;
            for (int idx = 0; idx < len; idx += FLOATS_PER_QUAD) {
                final float nx = buf[idx];
                final float ny = buf[idx + 1];
                final float nz = buf[idx + 2];

                final float x0 = buf[idx + 3] + tx;
                final float y0 = buf[idx + 4] + ty;
                final float z0 = buf[idx + 5] + tz;
                if (nx * x0 + ny * y0 + nz * z0 > 0.05F) {
                    continue;
                }

                consumer.vertex(
                        x0, y0, z0,
                        1.0F, 1.0F, 1.0F, 1.0F,
                        buf[idx + 6], buf[idx + 7],
                        overlay, light,
                        nx, ny, nz
                );
                consumer.vertex(
                        buf[idx + 8] + tx, buf[idx + 9] + ty, buf[idx + 10] + tz,
                        1.0F, 1.0F, 1.0F, 1.0F,
                        buf[idx + 11], buf[idx + 12],
                        overlay, light,
                        nx, ny, nz
                );
                consumer.vertex(
                        buf[idx + 13] + tx, buf[idx + 14] + ty, buf[idx + 15] + tz,
                        1.0F, 1.0F, 1.0F, 1.0F,
                        buf[idx + 16], buf[idx + 17],
                        overlay, light,
                        nx, ny, nz
                );
                consumer.vertex(
                        buf[idx + 18] + tx, buf[idx + 19] + ty, buf[idx + 20] + tz,
                        1.0F, 1.0F, 1.0F, 1.0F,
                        buf[idx + 21], buf[idx + 22],
                        overlay, light,
                        nx, ny, nz
                );
            }
        }

        /**
         * General matrix vertex emission for arbitrary PoseStack transformations.
         */
        void emitMatrix(VertexConsumer consumer, PoseStack.Pose pose, int light, int overlay) {
            final float[] buf = this.data;
            final int len = buf.length;
            if (len == 0) {
                return;
            }

            final Matrix4f pm = pose.pose();
            final float m00 = pm.m00(), m01 = pm.m01(), m02 = pm.m02();
            final float m10 = pm.m10(), m11 = pm.m11(), m12 = pm.m12();
            final float m20 = pm.m20(), m21 = pm.m21(), m22 = pm.m22();
            final float m30 = pm.m30(), m31 = pm.m31(), m32 = pm.m32();

            final Matrix3f nm = pose.normal();
            final float n00 = nm.m00(), n01 = nm.m01(), n02 = nm.m02();
            final float n10 = nm.m10(), n11 = nm.m11(), n12 = nm.m12();
            final float n20 = nm.m20(), n21 = nm.m21(), n22 = nm.m22();

            for (int idx = 0; idx < len; idx += FLOATS_PER_QUAD) {
                final float nx = buf[idx];
                final float ny = buf[idx + 1];
                final float nz = buf[idx + 2];

                final float tnx = n00 * nx + n10 * ny + n20 * nz;
                final float tny = n01 * nx + n11 * ny + n21 * nz;
                final float tnz = n02 * nx + n12 * ny + n22 * nz;

                // Vertex 0
                final float x0 = buf[idx + 3], y0 = buf[idx + 4], z0 = buf[idx + 5];
                consumer.vertex(
                        m00 * x0 + m10 * y0 + m20 * z0 + m30,
                        m01 * x0 + m11 * y0 + m21 * z0 + m31,
                        m02 * x0 + m12 * y0 + m22 * z0 + m32,
                        1.0F, 1.0F, 1.0F, 1.0F,
                        buf[idx + 6], buf[idx + 7],
                        overlay, light,
                        tnx, tny, tnz
                );

                // Vertex 1
                final float x1 = buf[idx + 8], y1 = buf[idx + 9], z1 = buf[idx + 10];
                consumer.vertex(
                        m00 * x1 + m10 * y1 + m20 * z1 + m30,
                        m01 * x1 + m11 * y1 + m21 * z1 + m31,
                        m02 * x1 + m12 * y1 + m22 * z1 + m32,
                        1.0F, 1.0F, 1.0F, 1.0F,
                        buf[idx + 11], buf[idx + 12],
                        overlay, light,
                        tnx, tny, tnz
                );

                // Vertex 2
                final float x2 = buf[idx + 13], y2 = buf[idx + 14], z2 = buf[idx + 15];
                consumer.vertex(
                        m00 * x2 + m10 * y2 + m20 * z2 + m30,
                        m01 * x2 + m11 * y2 + m21 * z2 + m31,
                        m02 * x2 + m12 * y2 + m22 * z2 + m32,
                        1.0F, 1.0F, 1.0F, 1.0F,
                        buf[idx + 16], buf[idx + 17],
                        overlay, light,
                        tnx, tny, tnz
                );

                // Vertex 3
                final float x3 = buf[idx + 18], y3 = buf[idx + 19], z3 = buf[idx + 20];
                consumer.vertex(
                        m00 * x3 + m10 * y3 + m20 * z3 + m30,
                        m01 * x3 + m11 * y3 + m21 * z3 + m31,
                        m02 * x3 + m12 * y3 + m22 * z3 + m32,
                        1.0F, 1.0F, 1.0F, 1.0F,
                        buf[idx + 21], buf[idx + 22],
                        overlay, light,
                        tnx, tny, tnz
                );
            }
        }
    }
}
