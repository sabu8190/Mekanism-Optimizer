package com.mekanismoptimizer.core;

import mekanism.api.Action;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;

public final class FastFactorySorter {
    private static Field processInfoSlotsField = null;
    private static boolean fieldLookupAttempted = false;

    private FastFactorySorter() {
    }

    private static TileEntityFactory.ProcessInfo[] getProcessInfoSlots(TileEntityFactory<?> factory) {
        if (!fieldLookupAttempted) {
            fieldLookupAttempted = true;
            try {
                processInfoSlotsField = TileEntityFactory.class.getDeclaredField("processInfoSlots");
                processInfoSlotsField.setAccessible(true);
            } catch (Exception e) {
                MekanismOptimizerLogger.error("Failed to access processInfoSlots field in TileEntityFactory", e);
            }
        }
        if (processInfoSlotsField != null) {
            try {
                return (TileEntityFactory.ProcessInfo[]) processInfoSlotsField.get(factory);
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