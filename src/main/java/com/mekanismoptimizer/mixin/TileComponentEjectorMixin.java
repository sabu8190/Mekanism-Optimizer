package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.inventory.TileTransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private EnumColor outputColor;

    @Shadow
    public abstract boolean isEjecting(ConfigInfo info, TransmissionType type);

    @Shadow
    protected abstract void eject(TransmissionType type, ConfigInfo info);

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServerHead(CallbackInfo ci) {
        if (tile == null) {
            return;
        }

        // Clamp tick delay to configured value
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
     * Replaces outputItems with robust Multi-Stack ejection engine.
     * Features O(1) early-exit when output slots are completely empty.
     */
    @Inject(method = "outputItems", at = @At("HEAD"), cancellable = true)
    private void onOutputItems(ConfigInfo info, CallbackInfo ci) {
        if (tile == null || tile.getLevel() == null) {
            return;
        }

        int maxStacks = MekanismOptimizerConfig.ENABLE_UNLIMITED_AUTO_EJECT.get() 
                ? MekanismOptimizerConfig.ITEM_EJECT_MAX_STACKS_PER_TICK.get() 
                : 1;

        if (maxStacks <= 0) {
            maxStacks = 1;
        }

        for (DataType dataType : info.getSupportedDataTypes()) {
            if (!dataType.canOutput()) {
                continue;
            }
            ISlotInfo slotInfo = info.getSlotInfo(dataType);
            if (slotInfo instanceof InventorySlotInfo inventorySlotInfo) {
                List<IInventorySlot> slots = inventorySlotInfo.getSlots();
                if (slots == null || slots.isEmpty()) {
                    continue;
                }

                // O(1) Fast early return if all slots for this output data type are empty
                boolean hasAnyItem = false;
                for (int i = 0; i < slots.size(); i++) {
                    IInventorySlot slot = slots.get(i);
                    if (slot != null && !slot.isEmpty()) {
                        hasAnyItem = true;
                        break;
                    }
                }

                if (!hasAnyItem) {
                    continue; // Skip without creating any TileTransitRequest or iterating sides
                }

                Set<Direction> outputs = info.getSidesForData(dataType);
                if (!outputs.isEmpty()) {
                    for (int stackCount = 0; stackCount < maxStacks; stackCount++) {
                        Direction firstSide = outputs.iterator().next();
                        TileTransitRequest ejectMap = InventoryUtils.getEjectItemMap(
                                new TileTransitRequest(tile, firstSide), 
                                slots
                        );

                        if (ejectMap.isEmpty()) {
                            break;
                        }

                        boolean ejectedAny = false;
                        for (Direction side : outputs) {
                            BlockEntity target = WorldUtils.getTileEntity(tile.getLevel(), tile.getBlockPos().relative(side));
                            if (target != null) {
                                TransitResponse response;
                                if (target instanceof TileEntityLogisticalTransporterBase transporter) {
                                    response = transporter.getTransmitter().insert(tile, ejectMap, outputColor, true, 0);
                                } else {
                                    response = ejectMap.addToInventory(target, side, 0, false);
                                }

                                if (!response.isEmpty()) {
                                    response.useAll();
                                    ejectedAny = true;
                                    if (ejectMap.isEmpty()) {
                                        break;
                                    }
                                }
                            }
                        }

                        if (!ejectedAny) {
                            break; // Target inventories or pipes are full
                        }
                    }
                }
            }
        }

        this.tickDelay = MekanismOptimizerConfig.ITEM_EJECT_TICK_DELAY.get();
        ci.cancel(); // Handled completely by our high-performance multi-stack engine
    }
}
