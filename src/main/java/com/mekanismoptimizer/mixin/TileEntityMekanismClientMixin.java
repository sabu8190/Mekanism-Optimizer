package com.mekanismoptimizer.mixin;

import it.unimi.dsi.fastutil.HashCommon;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side optimization for TileEntityMekanism.updateSound():
 * Prevents 6,966+ machines from simultaneously allocating BlockPos (getSoundPos) and TileTickableSound
 * every 20 ticks when the player is outside the 16-block sound attenuation radius, and staggers sound cooldowns.
 */
@Pseudo
@Mixin(value = TileEntityMekanism.class, remap = false)
public abstract class TileEntityMekanismClientMixin {

    @Shadow(remap = false)
    private int playSoundCooldown;

    @Shadow(remap = false)
    private SoundInstance activeSound;

    @Inject(method = "updateSound()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUpdateSoundFastPath(CallbackInfo ci) {
        if (this.activeSound == null) {
            if (this.playSoundCooldown > 1) {
                this.playSoundCooldown--;
                ci.cancel();
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            TileEntityMekanism self = (TileEntityMekanism) (Object) this;
            BlockPos pos = self.getBlockPos();
            if (player == null) {
                this.playSoundCooldown = 40;
                ci.cancel();
                return;
            }
            double dx = (pos.getX() + 0.5D) - player.getX();
            double dy = (pos.getY() + 2.5D) - player.getY();
            double dz = (pos.getZ() + 0.5D) - player.getZ();
            // 20-block radius covers 16-block attenuation + 4-block vertical sound offset of Wind Generators
            if (dx * dx + dy * dy + dz * dz > 400.0D) {
                this.playSoundCooldown = 40 + (int) (HashCommon.mix(pos.asLong()) & 31L);
                ci.cancel();
            }
        }
    }
}
