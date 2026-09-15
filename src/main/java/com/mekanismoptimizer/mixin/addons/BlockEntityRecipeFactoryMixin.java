package com.mekanismoptimizer.mixin.addons;

import astral_mekanism.block.blockentity.base.BlockEntityRecipeFactory;
import astral_mekanism.generalrecipe.lookup.monitor.UnifiedRecipeCacheLookupMonitor;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = BlockEntityRecipeFactory.class, remap = false)
public abstract class BlockEntityRecipeFactoryMixin<RECIPE extends Recipe<?>> {

    @Shadow @Final protected UnifiedRecipeCacheLookupMonitor<RECIPE>[] recipeCacheLookupMonitors;
    @Shadow @Final protected boolean[] activeStates;
    @Shadow @Final protected EnergyInventorySlot energySlot;
    @Shadow @Final protected MachineEnergyContainer<TileEntityConfigurableMachine> energyContainer;
    @Shadow protected FloatingLong lastUsage;

    /**
     * Optimized onUpdateServer implementation for Astral Mekanism factories.
     * Replaces 2-pass activeStates scan and avoids FloatingLong.copy() allocations.
     */
    @Inject(method = "onUpdateServer", at = @At("HEAD"), cancellable = true)
    private void onUpdateServerOptimized(CallbackInfo ci) {
        energySlot.fillContainerOrConvert();

        FloatingLong startEnergy = energyContainer.getEnergy();

        boolean anyActive = false;
        int len = recipeCacheLookupMonitors.length;
        for (int i = 0; i < len; i++) {
            boolean monitorActive = recipeCacheLookupMonitors[i].updateAndProcess();
            activeStates[i] = monitorActive;
            if (monitorActive) {
                anyActive = true;
            }
        }

        ((mekanism.common.tile.base.TileEntityMekanism) (Object) this).setActive(anyActive);
        if (anyActive) {
            FloatingLong currentEnergy = energyContainer.getEnergy();
            if (startEnergy.greaterThan(currentEnergy)) {
                this.lastUsage = startEnergy.subtract(currentEnergy);
            } else {
                this.lastUsage = FloatingLong.ZERO;
            }
        } else {
            this.lastUsage = FloatingLong.ZERO;
        }

        ci.cancel();
    }
}
