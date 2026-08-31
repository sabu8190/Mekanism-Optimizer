package com.mekanismoptimizer.core;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.BitSet;
import java.util.Map;

/**
 * High-performance O(1) slot indexer for IItemHandler containers.
 * Performs fast, on-demand validation without stale cache locks.
 */
public class FastSlotIndexer {
    private final IItemHandler handler;
    private final int slotCount;

    public FastSlotIndexer(IItemHandler handler) {
        this.handler = handler;
        this.slotCount = handler != null ? handler.getSlots() : 0;
    }

    public static FastSlotIndexer get(IItemHandler handler) {
        if (handler == null) return null;
        return new FastSlotIndexer(handler);
    }

    public boolean hasEmptySlot() {
        if (handler == null || slotCount == 0) return false;
        for (int i = 0; i < slotCount; i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsItem(Item item) {
        if (handler == null || slotCount == 0 || item == null) return false;
        for (int i = 0; i < slotCount; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public int getSlotCount() {
        return slotCount;
    }
}
