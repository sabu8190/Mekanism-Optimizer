package com.mekanismoptimizer.mixin;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Caches client-side getClientActive() state on TileEntityMekanism (invalidated on setBlockState),
 * eliminating 450,000+ Attribute.isActive(BlockState) map lookups per second across dense machine farms.
 */
@Pseudo
@Mixin(value = TileEntityMekanism.class, remap = false)
public abstract class TileEntityMekanismMixin {

    @Unique
    private byte mekOpti$cachedClientActive = -1;

    @Inject(
            method = "m_155250_(Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void onSetBlockStateInvalidateCache(BlockState newState, CallbackInfo ci) {
        this.mekOpti$cachedClientActive = -1;
    }

    @Inject(
            method = "getClientActive()Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void getClientActiveCached(CallbackInfoReturnable<Boolean> cir) {
        byte cached = this.mekOpti$cachedClientActive;
        if (cached != -1) {
            cir.setReturnValue(cached == 1);
            return;
        }
        TileEntityMekanism self = (TileEntityMekanism) (Object) this;
        boolean active = self.isActivatable() && Attribute.isActive(self.getBlockState());
        this.mekOpti$cachedClientActive = (byte) (active ? 1 : 0);
        cir.setReturnValue(active);
    }
}
