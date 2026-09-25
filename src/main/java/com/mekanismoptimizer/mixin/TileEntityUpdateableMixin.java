package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import com.mekanismoptimizer.core.PacketCoalescer;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = TileEntityUpdateable.class, remap = false)
public abstract class TileEntityUpdateableMixin {

    @Inject(method = "sendUpdatePacket(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("HEAD"), cancellable = true)
    private void onSendUpdatePacket(BlockEntity tracking, CallbackInfo ci) {
        TileEntityUpdateable self = (TileEntityUpdateable) (Object) this;
        if (PacketCoalescer.enqueueTileUpdate(self, tracking)) {
            ci.cancel();
        }
    }

    @Inject(method = "blockRemoved()V", at = @At("HEAD"))
    private void onBlockRemoved(CallbackInfo ci) {
        TileEntityUpdateable self = (TileEntityUpdateable) (Object) this;
        AdaptiveBackoffManager.notifyBlockUpdate(self.getTilePos());
    }
}
