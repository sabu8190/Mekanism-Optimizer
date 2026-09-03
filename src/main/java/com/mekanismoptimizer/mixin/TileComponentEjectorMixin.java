package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Pseudo
@Mixin(value = TileComponentEjector.class, remap = false)
public abstract class TileComponentEjectorMixin {

    @Shadow
    @Final
    private TileEntityMekanism tile;

    @Shadow
    private int tickDelay;

    @Shadow
    @Final
    private Map<TransmissionType, ConfigInfo> configInfo;

    @Shadow
    public abstract boolean isEjecting(ConfigInfo info, TransmissionType type);

    @Shadow
    protected abstract void eject(TransmissionType type, ConfigInfo info);

    @Shadow
    protected abstract void outputItems(ConfigInfo info);

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServerHead(CallbackInfo ci) {
        if (tile == null) {
            return;
        }

        // Clamp tick delay to configured value (0 or 1 for instant response)
        int configuredDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
        if (tickDelay > configuredDelay) {
            tickDelay = configuredDelay;
        }

        // Multi-burst ejection for fluid, chemical and energy
        if (MekanismOptimizerConfig.ENABLE_UNLIMITED_AUTO_EJECT.get()) {
            int burstMultiplier = MekanismOptimizerConfig.AUTO_EJECT_BURST_MULTIPLIER.get();
            if (burstMultiplier > 1) {
                for (Map.Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
                    TransmissionType type = entry.getKey();
                    ConfigInfo info = entry.getValue();
                    if (type != TransmissionType.ITEM && type != TransmissionType.HEAT && isEjecting(info, type)) {
                        for (int i = 0; i < burstMultiplier - 1; i++) {
                            eject(type, info);
                        }
                    }
                }
            }
        }
    }

    /**
     * Multi-burst auto-ejection for ITEMS when overclocking is enabled.
     * Fires up to ITEM_EJECT_MAX_STACKS_PER_TICK stacks per tick directly from machine Auto-Eject.
     */
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void onTickServerTail(CallbackInfo ci) {
        if (tile == null || !MekanismOptimizerConfig.ENABLE_UNLIMITED_AUTO_EJECT.get()) {
            return;
        }

        int maxBurst = MekanismOptimizerConfig.ITEM_EJECT_MAX_STACKS_PER_TICK.get();
        if (maxBurst <= 1) {
            return;
        }

        ConfigInfo itemConfig = configInfo.get(TransmissionType.ITEM);
        if (itemConfig != null && isEjecting(itemConfig, TransmissionType.ITEM)) {
            for (int i = 1; i < maxBurst; i++) {
                if (!hasAnyEjectableItem(itemConfig)) {
                    break; // No more items to eject or slots are empty, stop immediately (O(1))
                }
                outputItems(itemConfig);
            }
        }
    }

    /**
     * O(1) Fast check for outputItems:
     * If all output slots are completely empty, cancel immediately without running heavy search/shuffle.
     * If there ARE items to output, let native Mekanism outputItems execute with 100% fidelity.
     */
    @Inject(method = "outputItems", at = @At("HEAD"), cancellable = true)
    private void onOutputItemsHead(ConfigInfo info, CallbackInfo ci) {
        if (tile == null || tile.getLevel() == null) {
            ci.cancel();
            return;
        }

        if (!MekanismOptimizerConfig.ENABLE_ADDON_OPTIMIZATIONS.get()) {
            return;
        }

        if (!hasAnyEjectableItem(info)) {
            this.tickDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
            ci.cancel();
        }
    }

    @Inject(method = "outputItems", at = @At("TAIL"))
    private void onOutputItemsTail(ConfigInfo info, CallbackInfo ci) {
        // Reset tickDelay after ejection to avoid hardcoded 10-tick wait
        this.tickDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
    }

    private boolean hasAnyEjectableItem(ConfigInfo info) {
        for (DataType dataType : info.getSupportedDataTypes()) {
            if (!dataType.canOutput()) {
                continue;
            }
            ISlotInfo slotInfo = info.getSlotInfo(dataType);
            if (slotInfo instanceof InventorySlotInfo inventorySlotInfo) {
                List<IInventorySlot> slots = inventorySlotInfo.getSlots();
                if (slots != null && !slots.isEmpty()) {
                    for (int i = 0; i < slots.size(); i++) {
                        IInventorySlot slot = slots.get(i);
                        if (slot != null && !slot.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
