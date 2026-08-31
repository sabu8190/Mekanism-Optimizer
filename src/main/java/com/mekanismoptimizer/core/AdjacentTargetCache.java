package com.mekanismoptimizer.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ultra-fast O(1) adjacent BlockEntity cache.
 * Eliminates repetitive chunk lookups and hash map iterations during burst auto-ejection.
 */
public final class AdjacentTargetCache {

    private static final Map<BlockPos, TargetEntry[]> CACHE = new ConcurrentHashMap<>();

    private AdjacentTargetCache() {
    }

    public static BlockEntity getTarget(Level level, BlockPos origin, Direction direction) {
        if (level == null || origin == null || direction == null) {
            return null;
        }

        TargetEntry[] entries = CACHE.computeIfAbsent(origin, p -> new TargetEntry[6]);
        int dirIndex = direction.ordinal();
        TargetEntry entry = entries[dirIndex];

        if (entry != null && entry.target != null && !entry.target.isRemoved()) {
            return entry.target;
        }

        // Cache miss or invalidated: query world and update cache
        BlockPos targetPos = origin.relative(direction);
        if (!level.hasChunkAt(targetPos)) {
            entries[dirIndex] = null;
            return null;
        }

        BlockEntity target = level.getBlockEntity(targetPos);
        entries[dirIndex] = (target != null) ? new TargetEntry(target) : null;
        return target;
    }

    public static void invalidate(BlockPos origin) {
        CACHE.remove(origin);
    }

    public static void clear() {
        if (CACHE.size() > 8192) {
            CACHE.clear();
        }
    }

    private static class TargetEntry {
        final BlockEntity target;

        TargetEntry(BlockEntity target) {
            this.target = target;
        }
    }
}
