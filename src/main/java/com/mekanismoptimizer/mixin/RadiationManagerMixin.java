package com.mekanismoptimizer.mixin;

import com.google.common.collect.Table;
import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.LevelAndMaxMagnitude;
import mekanism.common.lib.radiation.RadiationSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = RadiationManager.class, remap = false)
public abstract class RadiationManagerMixin {

    private static final LevelAndMaxMagnitude BASELINE_MAGNITUDE = new LevelAndMaxMagnitude(RadiationManager.BASELINE, RadiationManager.BASELINE);

    @Shadow
    @Final
    private Table<Chunk3D, Coord4D, RadiationSource> radiationTable;

    @Shadow
    public abstract boolean isRadiationEnabled();

    @Inject(method = "getRadiationLevelAndMaxMagnitude(Lnet/minecraft/world/entity/Entity;)Lmekanism/common/lib/radiation/RadiationManager$LevelAndMaxMagnitude;", at = @At("HEAD"), cancellable = true)
    private void onGetRadiationEntity(Entity player, CallbackInfoReturnable<LevelAndMaxMagnitude> cir) {
        if (!MekanismOptimizerConfig.ENABLE_RADIATION_FAST_PATH.get()) {
            return;
        }
        if (!isRadiationEnabled() || radiationTable.isEmpty()) {
            MekanismOptimizerLogger.recordRadiationCheckSaved();
            cir.setReturnValue(BASELINE_MAGNITUDE);
        }
    }

    @Inject(method = "getRadiationLevelAndMaxMagnitude(Lmekanism/api/Coord4D;)Lmekanism/common/lib/radiation/RadiationManager$LevelAndMaxMagnitude;", at = @At("HEAD"), cancellable = true)
    private void onGetRadiationCoord(Coord4D coord, CallbackInfoReturnable<LevelAndMaxMagnitude> cir) {
        if (!MekanismOptimizerConfig.ENABLE_RADIATION_FAST_PATH.get()) {
            return;
        }
        if (!isRadiationEnabled() || radiationTable.isEmpty()) {
            MekanismOptimizerLogger.recordRadiationCheckSaved();
            cir.setReturnValue(BASELINE_MAGNITUDE);
        }
    }
}
