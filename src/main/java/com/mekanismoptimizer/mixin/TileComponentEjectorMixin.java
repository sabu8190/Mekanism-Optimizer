package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.AdjacentTargetCache;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = TileComponentEjector.class, remap = false)
public abstract class TileComponentEjectorMixin {

    @Shadow
    @Final
    private TileEntityMekanism tile;

    @Shadow
    private int tickDelay;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServerHead(CallbackInfo ci) {
        if (tile == null) {
            return;
        }

        // Clamp tick delay to configured value (default 0 for instant responsive ejection)
        int configuredDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
        if (tickDelay > configuredDelay) {
            tickDelay = configuredDelay;
        }
    }

    /**
     * Resets tickDelay after item ejection to eliminate hardcoded 10-tick (0.5s) lag.
     */
    @Inject(method = "outputItems", at = @At("TAIL"))
    private void onOutputItemsTail(ConfigInfo info, CallbackInfo ci) {
        this.tickDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
    }

    /**
     * O(1) Fast path for adjacent target BlockEntity lookup during item auto-ejection.
     */
    @Redirect(method = "outputItems", at = @At(value = "INVOKE", target = "Lmekanism/common/util/WorldUtils;getTileEntity(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity onGetTileEntityInOutputItems(BlockGetter world, BlockPos targetPos) {
        if (tile != null && world instanceof Level level) {
            BlockPos origin = tile.getBlockPos();
            for (Direction dir : Direction.values()) {
                if (origin.relative(dir).equals(targetPos)) {
                    return AdjacentTargetCache.getTarget(level, origin, dir);
                }
            }
        }
        return WorldUtils.getTileEntity(world, targetPos);
    }
}
