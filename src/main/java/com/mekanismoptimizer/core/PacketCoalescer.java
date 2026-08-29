package com.mekanismoptimizer.core;

import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketCoalescer {
    private static final Map<BlockPos, TileEntityUpdateable> PENDING_TILE_UPDATES = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> IS_FLUSHING = ThreadLocal.withInitial(() -> false);

    private PacketCoalescer() {
    }

    public static boolean isFlushing() {
        return IS_FLUSHING.get();
    }

    public static boolean enqueueTileUpdate(TileEntityUpdateable tile, BlockEntity trackingTarget) {
        if (isFlushing()) {
            return false;
        }

        if (!MekanismOptimizerConfig.ENABLE_PACKET_COALESCING.get()) {
            return false;
        }

        if (tile == null || tile.getTileWorld() == null || tile.getTileWorld().isClientSide) {
            return false;
        }

        BlockPos pos = tile.getTilePos();
        PENDING_TILE_UPDATES.put(pos, tile);
        MekanismOptimizerLogger.recordPacketCoalesced();
        return true;
    }

    public static void flushPendingUpdates() {
        if (PENDING_TILE_UPDATES.isEmpty()) {
            return;
        }

        IS_FLUSHING.set(true);
        try {
            for (Map.Entry<BlockPos, TileEntityUpdateable> entry : PENDING_TILE_UPDATES.entrySet()) {
                TileEntityUpdateable tile = entry.getValue();
                if (tile != null && tile.getTileWorld() != null && !tile.isRemoved()) {
                    try {
                        tile.sendUpdatePacket();
                    } catch (Throwable ignored) {
                    }
                }
            }
        } finally {
            IS_FLUSHING.set(false);
            PENDING_TILE_UPDATES.clear();
        }
    }
}