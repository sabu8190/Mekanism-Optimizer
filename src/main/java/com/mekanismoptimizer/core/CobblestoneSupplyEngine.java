package com.mekanismoptimizer.core;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.IUpgradeTile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Universal High-Reliability Cobblestone Supply Engine.
 * Fixes Astral Mekanism's cobblestone supply upgrade issue where cobblestone is never generated
 * or inserted due to missing upgrade recalculation or invalid slot automation constraints.
 */
public final class CobblestoneSupplyEngine {

    private static final Map<TileEntityMekanism, Integer> TICK_COUNTERS = Collections.synchronizedMap(new WeakHashMap<>());

    private static boolean ameChecked = false;
    private static Upgrade cobblestoneUpgrade = null;
    private static Field ameCobbleUpgradesField = null;
    private static Field ameCobbleTickField = null;

    private CobblestoneSupplyEngine() {
    }

    private static synchronized void initAstralMekanismCompat() {
        if (ameChecked) {
            return;
        }
        ameChecked = true;
        try {
            Class<?> ameUpgradeClass = Class.forName("astral_mekanism.enums.AMEUpgrade");
            Field cobbleField = ameUpgradeClass.getField("COBBLESTONE_SUPPLY");
            Object cobbleEnum = cobbleField.get(null);
            Method getValueMethod = ameUpgradeClass.getMethod("getValue");
            Object res = getValueMethod.invoke(cobbleEnum);
            if (res instanceof Upgrade up) {
                cobblestoneUpgrade = up;
            }
        } catch (Throwable ignored) {
            // Astral Mekanism not present
        }

        try {
            Class<?> tileClass = TileEntityMekanism.class;
            for (Field f : tileClass.getDeclaredFields()) {
                if (f.getName().contains("astral_mekanism$cobblestoneUpgrades")) {
                    f.setAccessible(true);
                    ameCobbleUpgradesField = f;
                } else if (f.getName().contains("astral_mekanism$cobblestoneTick")) {
                    f.setAccessible(true);
                    ameCobbleTickField = f;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Ticked from TileEntityMekanism.onUpdateServer()
     */
    public static void tick(TileEntityMekanism tile) {
        if (!ameChecked) {
            initAstralMekanismCompat();
        }

        if (cobblestoneUpgrade == null || !tile.supportsUpgrades()) {
            return;
        }

        TileComponentUpgrade component = tile.getComponent();
        if (component == null) {
            return;
        }

        int count = component.getUpgrades(cobblestoneUpgrade);
        if (count <= 0) {
            TICK_COUNTERS.remove(tile);
            return;
        }

        // Suppress Astral Mekanism's faulty internal ticking to prevent double-generation or log spam
        suppressAMEInternal(tile);

        int baseTick = (count < 16) ? MathUtils.clampToInt(13.333333333333334 * (16 - count)) : 1;
        int currentTick = TICK_COUNTERS.getOrDefault(tile, 0) + 1;

        if (currentTick < baseTick) {
            TICK_COUNTERS.put(tile, currentTick);
            return;
        }

        TICK_COUNTERS.put(tile, 0);

        // Calculate produce count
        int produceCount = 1;
        if (count >= 17) {
            int shift = Math.min(30, count * 2 - 33);
            produceCount = Math.min(64, 1 << shift);
        }

        produceAndSupply(tile, produceCount);
    }

    private static void produceAndSupply(TileEntityMekanism tile, int amount) {
        ItemStack cobble = new ItemStack(Items.COBBLESTONE, amount);

        // 1. Try standard Mekanism TileEntityFactory
        if (tile instanceof TileEntityFactory<?> factory) {
            List<IInventorySlot> slots = getFactoryInputSlots(factory);
            if (slots != null && !slots.isEmpty()) {
                supplyToSlots(slots, cobble);
                UniversalFactoryAutoSorter.sortMekanismFactory(factory);
                return;
            }
        }

        // 2. Try Astral Mekanism Factory
        List<IInventorySlot> ameSlots = UniversalFactoryAutoSorter.getAMEInputSlots(tile);
        if (ameSlots != null && !ameSlots.isEmpty()) {
            supplyToSlots(ameSlots, cobble);
            UniversalFactoryAutoSorter.sortAMEFactory(tile);
            return;
        }

        // 3. Fallback: Generic Mekanism machine (Crusher, Energized Smelter, etc.)
        List<IInventorySlot> slots = tile.getInventorySlots(null);
        if (slots != null) {
            supplyToSlots(slots, cobble);
        }
    }

    private static void supplyToSlots(List<IInventorySlot> slots, ItemStack stack) {
        for (IInventorySlot slot : slots) {
            if (stack.isEmpty()) {
                break;
            }
            // Use INTERNAL automation type so that machine configuration does not block cobblestone generation
            ItemStack remainder = slot.insertItem(stack, Action.EXECUTE, AutomationType.INTERNAL);
            if (remainder.getCount() < stack.getCount()) {
                stack = remainder;
            }
        }
    }

    private static List<IInventorySlot> getFactoryInputSlots(TileEntityFactory<?> factory) {
        try {
            Field field = TileEntityFactory.class.getDeclaredField("processInfoSlots");
            field.setAccessible(true);
            TileEntityFactory.ProcessInfo[] infos = (TileEntityFactory.ProcessInfo[]) field.get(factory);
            if (infos != null) {
                java.util.List<IInventorySlot> list = new java.util.ArrayList<>(infos.length);
                for (TileEntityFactory.ProcessInfo info : infos) {
                    list.add(info.inputSlot());
                }
                return list;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void suppressAMEInternal(TileEntityMekanism tile) {
        try {
            if (ameCobbleUpgradesField != null) {
                ameCobbleUpgradesField.setInt(tile, 0);
            }
            if (ameCobbleTickField != null) {
                ameCobbleTickField.setInt(tile, 0);
            }
        } catch (Throwable ignored) {
        }
    }
}
