package com.mekanismoptimizer.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.client.sound.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Optimizes Mekanism's SoundHandler.startTileSound:
 * 1. Performs fast player distance check BEFORE allocating TileTickableSound and invoking Sound.resolve().
 * 2. Caps simultaneous active looping Mekanism tile sounds to 16 so standing inside a 6,966-windmill farm
 *    does not spawn 800 concurrent OpenAL channels and stall the client render thread.
 */
@Pseudo
@Mixin(value = SoundHandler.class, remap = false)
public abstract class SoundHandlerMixin {

    private static final int MAX_ACTIVE_TILE_SOUNDS = 16;

    @Shadow(remap = false)
    private static Long2ObjectMap<SoundInstance> soundMap;

    @Inject(
            method = "startTileSound(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FLnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/client/resources/sounds/SoundInstance;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onStartTileSoundPreCheck(
            SoundEvent soundEvent,
            SoundSource category,
            float volume,
            RandomSource random,
            BlockPos pos,
            boolean looping,
            CallbackInfoReturnable<SoundInstance> cir
    ) {
        if (pos == null) {
            cir.setReturnValue(null);
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            cir.setReturnValue(null);
            return;
        }

        // Fast range check before allocating TileTickableSound and resolving Sound
        float maxDist = Math.max(volume, 1.0F) * 16.0F;
        double dx = (pos.getX() + 0.5D) - player.getX();
        double dy = (pos.getY() + 0.5D) - player.getY();
        double dz = (pos.getZ() + 0.5D) - player.getZ();
        if (dx * dx + dy * dy + dz * dz >= (double) (maxDist * maxDist)) {
            cir.setReturnValue(null);
            return;
        }

        // Prevent OpenAL channel exhaustion when surrounded by hundreds of active machines within 16m
        if (soundMap != null && soundMap.size() >= MAX_ACTIVE_TILE_SOUNDS && !soundMap.containsKey(pos.asLong())) {
            cir.setReturnValue(null);
        }
    }
}
