package com.mekanismoptimizer.mixin;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasicChemicalTank.class, remap = false)
public abstract class BasicChemicalTankMixin<STACK extends ChemicalStack<?>> {

    /**
     * Fast O(1) fail-quick check for insert when tank is full, avoiding unnecessary attribute validator executions
     */
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void onInsertFast(STACK stack, Action action, AutomationType automationType, CallbackInfoReturnable<STACK> cir) {
        if (stack.isEmpty() || ((IChemicalTank<?, ?>) (Object) this).getNeeded() <= 0) {
            cir.setReturnValue(stack);
        }
    }
}
