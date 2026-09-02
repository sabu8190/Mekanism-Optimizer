package com.mekanismoptimizer.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FastRecipeLookupCache {

    private static final Map<Item, Object> SINGLE_ITEM_RECIPE_CACHE = new ConcurrentHashMap<>(1024);
    private static final Object NEGATIVE_CACHE_SENTINEL = new Object();

    // Astral Mekanism CropSoil pair cache (CropItem, SoilItem -> Recipe)
    private static final Map<Long, Object> CROP_SOIL_RECIPE_CACHE = new ConcurrentHashMap<>(512);

    private static boolean initialized = false;

    private FastRecipeLookupCache() {
    }

    public static synchronized void init() {
        if (!initialized) {
            MinecraftForge.EVENT_BUS.register(FastRecipeLookupCache.class);
            initialized = true;
            MekanismOptimizerLogger.info("FastRecipeLookupCache registered for dynamic datapack reload synchronization.");
        }
    }

    public static void clear() {
        SINGLE_ITEM_RECIPE_CACHE.clear();
        CROP_SOIL_RECIPE_CACHE.clear();
        MekanismOptimizerLogger.info("FastRecipeLookupCache flushed.");
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        clear();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        clear();
    }

    @SuppressWarnings("unchecked")
    public static <R> R getSingleItemRecipe(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        Item item = stack.getItem();
        if (item == Items.AIR) {
            return null;
        }
        Object cached = SINGLE_ITEM_RECIPE_CACHE.get(item);
        if (cached == NEGATIVE_CACHE_SENTINEL) {
            return null;
        }
        return (R) cached;
    }

    public static void putSingleItemRecipe(ItemStack stack, Object recipe) {
        if (stack.isEmpty()) {
            return;
        }
        Item item = stack.getItem();
        if (item != Items.AIR) {
            SINGLE_ITEM_RECIPE_CACHE.put(item, recipe != null ? recipe : NEGATIVE_CACHE_SENTINEL);
        }
    }

    @SuppressWarnings("unchecked")
    public static <R> R getCropSoilRecipe(ItemStack crop, ItemStack soil) {
        if (crop.isEmpty() || soil.isEmpty()) {
            return null;
        }
        long key = (((long) Item.getId(crop.getItem())) << 32) | (Item.getId(soil.getItem()) & 0xFFFFFFFFL);
        Object cached = CROP_SOIL_RECIPE_CACHE.get(key);
        if (cached == NEGATIVE_CACHE_SENTINEL) {
            return null;
        }
        return (R) cached;
    }

    public static void putCropSoilRecipe(ItemStack crop, ItemStack soil, Object recipe) {
        if (crop.isEmpty() || soil.isEmpty()) {
            return;
        }
        long key = (((long) Item.getId(crop.getItem())) << 32) | (Item.getId(soil.getItem()) & 0xFFFFFFFFL);
        CROP_SOIL_RECIPE_CACHE.put(key, recipe != null ? recipe : NEGATIVE_CACHE_SENTINEL);
    }
}
