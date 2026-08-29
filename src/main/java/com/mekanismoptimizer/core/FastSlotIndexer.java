package com.mekanismoptimizer.core;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.lang.ref.WeakReference;
import java.util.BitSet;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * High-performance O(1) slot indexer for IItemHandler containers.
 * Maintains a BitSet of empty slots and a fast item-to-slot mapping.
 */
public class FastSlotIndexer {
    private static final WeakHashMap<IItemHandler, WeakReference<FastSlotIndexer>> CACHE = new WeakHashMap<>();

    private final IItemHandler handler;
    private final BitSet emptySlots;
    private final Map<Item, IntArrayList> itemSlotMap;
    private boolean valid = false;
    private final int slotCount;

    public FastSlotIndexer(IItemHandler handler) {
        this.handler = handler;
        this.slotCount = handler.getSlots();
        this.emptySlots = new BitSet(this.slotCount);
        this.itemSlotMap = new Object2ObjectOpenHashMap<>();
        rebuild();
    }

    public static synchronized FastSlotIndexer get(IItemHandler handler) {
        if (handler == null) return null;
        WeakReference<FastSlotIndexer> ref = CACHE.get(handler);
        FastSlotIndexer indexer = (ref != null) ? ref.get() : null;
        if (indexer == null) {
            indexer = new FastSlotIndexer(handler);
            CACHE.put(handler, new WeakReference<>(indexer));
        } else if (!indexer.valid) {
            indexer.rebuild();
        }
        return indexer;
    }

    public synchronized void invalidate() {
        this.valid = false;
    }

    public synchronized void rebuild() {
        emptySlots.clear();
        itemSlotMap.clear();

        for (int i = 0; i < slotCount; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) {
                emptySlots.set(i);
            } else {
                Item item = stack.getItem();
                itemSlotMap.computeIfAbsent(item, k -> new IntArrayList()).add(i);
            }
        }
        this.valid = true;
    }

    public synchronized BitSet getEmptySlots() {
        if (!valid) rebuild();
        return emptySlots;
    }

    public synchronized boolean hasEmptySlot() {
        if (!valid) rebuild();
        return !emptySlots.isEmpty();
    }

    public synchronized boolean containsItem(Item item) {
        if (!valid) rebuild();
        IntArrayList list = itemSlotMap.get(item);
        return list != null && !list.isEmpty();
    }

    public synchronized IntArrayList getSlotsWithItem(Item item) {
        if (!valid) rebuild();
        return itemSlotMap.get(item);
    }

    public int getSlotCount() {
        return slotCount;
    }
}