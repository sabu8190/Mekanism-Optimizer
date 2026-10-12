package com.mekanismoptimizer.core;

import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Precomputed lock-free O(1) array cache for transmitter visible connection model names.
 * Eliminates Arrays.stream, 6 string concatenations, and toList allocations every frame per pipe/cable.
 */
public final class FastTransmitterRenderCache {

    // 6 directions * 2 bits per ConnectionType (4 enum values: NORMAL, PUSH, PULL, NONE) = 4^6 = 4,096 states
    @SuppressWarnings("unchecked")
    private static final List<String>[] VISIBLE_SIDES_CACHE = new List[4096];

    private FastTransmitterRenderCache() {
    }

    /**
     * Gets the cached List<String> of visible sides for the given transmitter connection states.
     * Generates 0 heap allocations and 0 lock contention on hit.
     */
    public static List<String> getVisibleSides(Transmitter<?, ?, ?> transmitter) {
        if (transmitter == null) {
            return Collections.emptyList();
        }

        // Pack 6 direction connection types into a 12-bit integer key (2 bits per direction)
        int key = 0;
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            Direction side = EnumUtils.DIRECTIONS[i];
            ConnectionType type = transmitter.getConnectionType(side);
            int ordinal = type != null ? type.ordinal() : 3;
            key |= (ordinal & 3) << (i << 1);
        }

        List<String> cached = VISIBLE_SIDES_CACHE[key];
        if (cached != null) {
            return cached;
        }

        List<String> list = new ArrayList<>(6);
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType type = transmitter.getConnectionType(side);
            String typeName = type != null ? type.getSerializedName().toUpperCase(Locale.ROOT) : "NONE";
            list.add(side.getSerializedName() + typeName);
        }
        List<String> unmodifiable = Collections.unmodifiableList(list);
        VISIBLE_SIDES_CACHE[key] = unmodifiable;
        return unmodifiable;
    }
}

