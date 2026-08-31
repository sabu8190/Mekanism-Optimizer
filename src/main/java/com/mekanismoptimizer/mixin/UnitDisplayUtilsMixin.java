package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Pseudo
@Mixin(value = UnitDisplayUtils.class, remap = false)
public abstract class UnitDisplayUtilsMixin {

    @Unique
    private static final Map<String, Component> mekanismOptimizer$DISPLAY_CACHE = new ConcurrentHashMap<>();

    @Inject(method = "getDisplay(Lmekanism/api/math/FloatingLong;Lmekanism/common/util/UnitDisplayUtils$EnergyUnit;IZ)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private static void onGetDisplay(FloatingLong value, EnergyUnit unit, int decimalPlaces, boolean isShort, CallbackInfoReturnable<Component> cir) {
        if (!MekanismOptimizerConfig.ENABLE_LOOKING_AT_CACHE.get() || value == null || unit == null) {
            return;
        }

        // Fast zero check
        if (value.isZero()) {
            return;
        }

        String cacheKey = value.toString() + "_" + unit.name() + "_" + decimalPlaces + "_" + isShort;
        Component cached = mekanismOptimizer$DISPLAY_CACHE.get(cacheKey);
        if (cached != null) {
            MekanismOptimizerLogger.recordUnitDisplayCached();
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getDisplay(Lmekanism/api/math/FloatingLong;Lmekanism/common/util/UnitDisplayUtils$EnergyUnit;IZ)Lnet/minecraft/network/chat/Component;", at = @At("RETURN"))
    private static void onGetDisplayReturn(FloatingLong value, EnergyUnit unit, int decimalPlaces, boolean isShort, CallbackInfoReturnable<Component> cir) {
        if (!MekanismOptimizerConfig.ENABLE_LOOKING_AT_CACHE.get() || value == null || unit == null || value.isZero()) {
            return;
        }
        Component result = cir.getReturnValue();
        if (result != null) {
            if (mekanismOptimizer$DISPLAY_CACHE.size() > 2048) {
                mekanismOptimizer$DISPLAY_CACHE.clear();
            }
            String cacheKey = value.toString() + "_" + unit.name() + "_" + decimalPlaces + "_" + isShort;
            mekanismOptimizer$DISPLAY_CACHE.put(cacheKey, result);
        }
    }
}
