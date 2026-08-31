package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Pseudo
@Mixin(value = BasicFluidTank.class, remap = false)
public abstract class BasicFluidTankMixin {

    @Shadow
    @Final
    private Predicate<@NotNull FluidStack> validator;

    @Unique
    private final Map<Fluid, Boolean> mekanismOptimizer$VALID_CACHE = new ConcurrentHashMap<>();

    @Inject(method = "isFluidValid", at = @At("HEAD"), cancellable = true)
    private void onIsFluidValid(@NotNull FluidStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!MekanismOptimizerConfig.ENABLE_LOOKING_AT_CACHE.get() || stack.isEmpty() || validator == null) {
            return;
        }

        Fluid fluid = stack.getFluid();
        Boolean cached = mekanismOptimizer$VALID_CACHE.get(fluid);
        if (cached != null) {
            cir.setReturnValue(cached);
            return;
        }

        boolean result = validator.test(stack);
        if (mekanismOptimizer$VALID_CACHE.size() > 64) {
            mekanismOptimizer$VALID_CACHE.clear();
        }
        mekanismOptimizer$VALID_CACHE.put(fluid, result);
        cir.setReturnValue(result);
    }
}
