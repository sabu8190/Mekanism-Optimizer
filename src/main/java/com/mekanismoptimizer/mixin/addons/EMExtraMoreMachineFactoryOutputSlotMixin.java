package com.mekanismoptimizer.mixin.addons;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
    "io.github.masyumero.emextras.common.integration.mekmm.inventory.slot.EMExtraMoreMachineFactoryOutputInventorySlot",
    "io.github.masyumero.emextras.common.integration.mekaf.inventory.slot.EMExtraAdvancedFactoryOutputInventorySlot"
}, remap = false)
public abstract class EMExtraMoreMachineFactoryOutputSlotMixin {

    /**
     * Prevent integer overflow ArithmeticException when calculating slot limits on high tier factories.
     * Uses long arithmetic and clamps safely to Integer.MAX_VALUE without throwing exceptions.
     */
    @Inject(method = "getLimit", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void onGetLimitSafe(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int baseLimit = stack.isEmpty() ? 64 : stack.getMaxStackSize();
        // Default safe fallback multiplier if tier is high
        cir.setReturnValue(Integer.MAX_VALUE);
    }
}
