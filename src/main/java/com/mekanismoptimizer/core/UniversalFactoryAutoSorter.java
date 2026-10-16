package com.mekanismoptimizer.core;

import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal High-Performance Factory Auto-Sorter.
 * Provides perfectly balanced, zero-lag item distribution for all Mekanism and addon factories
 * (Standard Mekanism, Astral Mekanism, More Machine, EMExtras).
 */
public final class UniversalFactoryAutoSorter {

    private static final Map<Class<?>, Field> PROCESS_INFO_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> AME_INPUT_SLOTS_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> IS_SORTING_METHOD_CACHE = new ConcurrentHashMap<>();

    private UniversalFactoryAutoSorter() {
    }

    /**
     * Balances input slots for standard Mekanism TileEntityFactory.
     * @return true if balancing was processed
     */
    public static boolean sortMekanismFactory(TileEntityFactory<?> factory) {
        if (!factory.isSorting()) {
            return false;
        }

        TileEntityFactory.ProcessInfo[] processInfoSlots = getProcessInfoSlots(factory);
        if (processInfoSlots == null || processInfoSlots.length <= 1) {
            return false;
        }

        List<IInventorySlot> slots = new ArrayList<>(processInfoSlots.length);
        for (TileEntityFactory.ProcessInfo info : processInfoSlots) {
            slots.add(info.inputSlot());
        }

        return balanceSlots(slots);
    }

    /**
     * Balances input slots for Astral Mekanism factories (BEEnergizedSmeltingFactory / BEAstralEnergizedSmeltingFactory).
     * @return true if balancing was processed
     */
    public static boolean sortAMEFactory(Object ameFactory) {
        if (ameFactory == null) {
            return false;
        }

        // Check if machine has isSorting() method (like BlockEntityProgressFactory)
        Boolean sortingEnabled = checkSortingEnabled(ameFactory);
        if (sortingEnabled != null && !sortingEnabled) {
            return false;
        }

        List<IInventorySlot> slots = getAMEInputSlots(ameFactory);
        if (slots == null || slots.size() <= 1) {
            return false;
        }

        return balanceSlots(slots);
    }

    /**
     * Universal slot balancing algorithm:
     * Calculates total item count across all matching input slots and distributes them evenly.
     */
    public static boolean balanceSlots(List<IInventorySlot> slots) {
        if (slots == null || slots.size() <= 1) {
            return false;
        }

        // Find reference item (first non-empty slot)
        ItemStack refStack = ItemStack.EMPTY;
        int totalCount = 0;
        int nonEmptyCount = 0;
        boolean singleType = true;

        for (IInventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                ItemStack stack = slot.getStack();
                totalCount += stack.getCount();
                nonEmptyCount++;
                if (refStack.isEmpty()) {
                    refStack = stack;
                } else if (singleType && !ItemHandlerHelper.canItemStacksStack(refStack, stack)) {
                    singleType = false;
                }
            }
        }

        if (totalCount == 0 || refStack.isEmpty()) {
            return true;
        }

        // If multiple distinct item types are present, keep existing contents untouched to avoid recipe mixing
        if (!singleType) {
            return false;
        }

        int slotCount = slots.size();
        int maxStack = refStack.getMaxStackSize();
        int perSlot = totalCount / slotCount;
        int remainder = totalCount % slotCount;

        // Check if already perfectly balanced
        boolean alreadyBalanced = true;
        for (int i = 0; i < slotCount; i++) {
            int expected = perSlot + (i < remainder ? 1 : 0);
            if (expected > maxStack) {
                expected = maxStack;
            }
            if (slots.get(i).getCount() != expected) {
                alreadyBalanced = false;
                break;
            }
        }

        if (alreadyBalanced) {
            return true;
        }

        // Distribute evenly across all slots
        for (int i = 0; i < slotCount; i++) {
            int target = perSlot + (i < remainder ? 1 : 0);
            if (target > maxStack) {
                target = maxStack;
            }
            IInventorySlot slot = slots.get(i);
            if (target <= 0) {
                if (!slot.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                }
            } else {
                slot.setStack(refStack.copyWithCount(target));
            }
        }

        return true;
    }

    private static Boolean checkSortingEnabled(Object factory) {
        Class<?> clazz = factory.getClass();
        Method method = IS_SORTING_METHOD_CACHE.computeIfAbsent(clazz, c -> {
            Class<?> cur = c;
            while (cur != null && cur != Object.class) {
                try {
                    Method m = cur.getDeclaredMethod("isSorting");
                    m.setAccessible(true);
                    return m;
                } catch (NoSuchMethodException ignored) {
                    cur = cur.getSuperclass();
                } catch (Exception e) {
                    break;
                }
            }
            return null;
        });

        if (method != null) {
            try {
                Object res = method.invoke(factory);
                if (res instanceof Boolean) {
                    return (Boolean) res;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static TileEntityFactory.ProcessInfo[] getProcessInfoSlots(TileEntityFactory<?> factory) {
        Class<?> clazz = factory.getClass();
        Field field = PROCESS_INFO_FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Class<?> cur = c;
            while (cur != null && cur != Object.class) {
                try {
                    Field f = cur.getDeclaredField("processInfoSlots");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {
                    cur = cur.getSuperclass();
                } catch (Exception e) {
                    break;
                }
            }
            return null;
        });

        if (field != null) {
            try {
                return (TileEntityFactory.ProcessInfo[]) field.get(factory);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static List<IInventorySlot> getAMEInputSlots(Object factory) {
        Class<?> clazz = factory.getClass();
        Field field = AME_INPUT_SLOTS_FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Class<?> cur = c;
            while (cur != null && cur != Object.class) {
                try {
                    Field f = cur.getDeclaredField("inputSlots");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {
                    cur = cur.getSuperclass();
                } catch (Exception e) {
                    break;
                }
            }
            return null;
        });

        if (field != null) {
            try {
                Object res = field.get(factory);
                if (res instanceof Object[] array) {
                    List<IInventorySlot> list = new ArrayList<>(array.length);
                    for (Object obj : array) {
                        if (obj instanceof IInventorySlot slot) {
                            list.add(slot);
                        }
                    }
                    return list;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
