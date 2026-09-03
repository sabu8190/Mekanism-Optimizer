package com.mekanismoptimizer.mixin;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasicFluidTank.class, remap = false)
public abstract class BasicFluidTankMixin {

    /**
     * Fast O(1) fail-quick check for insert when tank is full, avoiding unnecessary validation executions
     */
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void onInsertFast(FluidStack stack, Action action, AutomationType automationType, CallbackInfoReturnable<FluidStack> cir) {
        if (stack.isEmpty() || ((IExtendedFluidTank) (Object) this).getNeeded() <= 0) {
            cir.setReturnValue(stack);
        }
    }
}
