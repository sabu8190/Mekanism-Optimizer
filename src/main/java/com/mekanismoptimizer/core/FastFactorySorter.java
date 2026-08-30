package com.mekanismoptimizer.core;

import mekanism.api.Action;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FastFactorySorter {
    private static final Map<Class<?>, Field> FIELD_CACHE = new ConcurrentHashMap<>();

    private FastFactorySorter() {
    }

    private static TileEntityFactory.ProcessInfo[] getProcessInfoSlots(TileEntityFactory<?> factory) {
        Class<?> clazz = factory.getClass();
        Field field = FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Class<?> current = c;
            while (current != null && current != Object.class) {
                try {
                    Field f = current.getDeclaredField("processInfoSlots");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                } catch (Exception e) {
                    MekanismOptimizerLogger.error("Error accessing processInfoSlots in " + c.getName(), e);
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

    public static boolean sortFast(TileEntityFactory<?> factory) {
        if (!MekanismOptimizerConfig.ENABLE_FACTORY_AUTO_SORT_OPTIMIZATION.get()) {
            return false;
        }

        TileEntityFactory.ProcessInfo[] processInfoSlots = getProcessInfoSlots(factory);
        if (processInfoSlots == null || processInfoSlots.length <= 1) {
            return false;
        }

        int totalProcesses = processInfoSlots.length;
        ItemStack firstItem = ItemStack.EMPTY;
        int totalCount = 0;
        boolean singleItemType = true;

        for (int i = 0; i < totalProcesses; i++) {
            FactoryInputInventorySlot slot = processInfoSlots[i].inputSlot();
            if (!slot.isEmpty()) {
                ItemStack stack = slot.getStack();
                totalCount += stack.getCount();
                if (firstItem.isEmpty()) {
                    firstItem = stack;
                } else if (singleItemType && !ItemStack.isSameItemSameTags(firstItem, stack)) {
                    singleItemType = false;
                }
            }
        }

        if (totalCount == 0) {
            return true;
        }

        // Fast path for single item type (covers 95%+ of industrial automation setups)
        if (singleItemType && !firstItem.isEmpty()) {
            int maxStackSize = firstItem.getMaxStackSize();
            int numberPerSlot = totalCount / totalProcesses;
            int remainder = totalCount % totalProcesses;

            boolean alreadyBalanced = true;
            for (int i = 0; i < totalProcesses; i++) {
                int expected = numberPerSlot + (i < remainder ? 1 : 0);
                if (expected > maxStackSize) {
                    alreadyBalanced = false;
                    break;
                }
                FactoryInputInventorySlot slot = processInfoSlots[i].inputSlot();
                if (slot.getCount() != expected) {
                    alreadyBalanced = false;
                    break;
                }
            }

            if (alreadyBalanced) {
                MekanismOptimizerLogger.recordFactorySortOptimized();
                return true;
            }

            for (int i = 0; i < totalProcesses; i++) {
                FactoryInputInventorySlot slot = processInfoSlots[i].inputSlot();
                int targetSize = numberPerSlot + (i < remainder ? 1 : 0);
                if (targetSize > maxStackSize) {
                    targetSize = maxStackSize;
                }

                if (slot.isEmpty()) {
                    if (targetSize > 0) {
                        ItemStack newStack = firstItem.copy();
                        newStack.setCount(targetSize);
                        slot.setStackUnchecked(newStack);
                    }
                } else {
                    if (targetSize == 0) {
                        slot.setEmpty();
                    } else if (slot.getCount() != targetSize) {
                        slot.setStackSize(targetSize, Action.EXECUTE);
                    }
                }
            }

            MekanismOptimizerLogger.recordFactorySortOptimized();
            return true;
        }

        return false;
    }
}
