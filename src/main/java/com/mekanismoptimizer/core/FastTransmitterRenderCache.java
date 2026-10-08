package com.mekanismoptimizer.core;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Precomputed cache for transmitter visible connection model names.
 * Eliminates Arrays.stream, 6 string concatenations, and toList allocations every frame per pipe/cable.
 */
public final class FastTransmitterRenderCache {

    private static final Int2ObjectMap<List<String>> VISIBLE_SIDES_CACHE = new Int2ObjectOpenHashMap<>();

    private FastTransmitterRenderCache() {
    }

    /**
     * Gets the cached List<String> of visible sides for the given transmitter connection states.
     * Generates 0 heap allocations on hit.
     */
    public static List<String> getVisibleSides(Transmitter<?, ?, ?> transmitter) {
        if (transmitter == null) {
            return Collections.emptyList();
        }

        // Pack 6 direction connection types into a compact 18-bit integer key (3 bits per direction)
        int key = 0;
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            Direction side = EnumUtils.DIRECTIONS[i];
            ConnectionType type = transmitter.getConnectionType(side);
            int ordinal = type != null ? type.ordinal() : 0;
            key |= (ordinal & 0x7) << (i * 3);
        }

        List<String> cached = VISIBLE_SIDES_CACHE.get(key);
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
        synchronized (VISIBLE_SIDES_CACHE) {
            VISIBLE_SIDES_CACHE.put(key, unmodifiable);
        }
        return unmodifiable;
    }
}
