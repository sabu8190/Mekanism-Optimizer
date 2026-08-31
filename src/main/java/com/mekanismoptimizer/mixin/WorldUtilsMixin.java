package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Pseudo
@Mixin(value = WorldUtils.class, remap = false)
public abstract class WorldUtilsMixin {

    @Unique
    private static final Map<BlockPos, BlockEntity> mekanismOptimizer$TILE_CACHE = new ConcurrentHashMap<>();

    @Inject(method = "getTileEntity(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At("HEAD"), cancellable = true)
    private static void onGetTileEntity(BlockGetter world, BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        if (!MekanismOptimizerConfig.ENABLE_INVENTORY_ACCEPTOR_CACHE.get() || pos == null || world == null) {
            return;
        }

        BlockEntity cached = mekanismOptimizer$TILE_CACHE.get(pos);
        if (cached != null) {
            if (!cached.isRemoved()) {
                cir.setReturnValue(cached);
                return;
            } else {
                mekanismOptimizer$TILE_CACHE.remove(pos);
            }
        }
    }

    @Inject(method = "getTileEntity(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At("RETURN"))
    private static void onGetTileEntityReturn(BlockGetter world, BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        if (!MekanismOptimizerConfig.ENABLE_INVENTORY_ACCEPTOR_CACHE.get() || pos == null || world == null) {
            return;
        }

        BlockEntity result = cir.getReturnValue();
        if (result != null && !result.isRemoved()) {
            if (mekanismOptimizer$TILE_CACHE.size() > 4096) {
                mekanismOptimizer$TILE_CACHE.clear();
            }
            mekanismOptimizer$TILE_CACHE.put(pos.immutable(), result);
        }
    }
}
