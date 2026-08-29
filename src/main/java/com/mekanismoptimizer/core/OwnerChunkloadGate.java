package com.mekanismoptimizer.core;

import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class OwnerChunkloadGate {

    private OwnerChunkloadGate() {
    }

    public static boolean shouldAllowChunkloading(TileEntityMekanism tile) {
        if (!MekanismOptimizerConfig.ENABLE_OWNER_CHUNKLOAD_GATING.get() || tile == null) {
            return true;
        }

        if (tile.getLevel() == null || tile.getLevel().isClientSide) {
            return true;
        }

        MinecraftServer server = tile.getLevel().getServer();
        if (server == null) {
            return true;
        }

        Object obj = tile;
        if (obj instanceof ISecurityTile) {
            ISecurityTile securityTile = (ISecurityTile) obj;
            UUID ownerUUID = securityTile.getOwnerUUID();
            if (ownerUUID != null) {
                ServerPlayer player = server.getPlayerList().getPlayer(ownerUUID);
                boolean isOnline = player != null;
                if (!isOnline) {
                    MekanismOptimizerLogger.recordChunkloadGated();
                    return false;
                }
            }
        }

        return true;
    }
}