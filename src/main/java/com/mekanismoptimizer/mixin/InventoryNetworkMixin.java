package com.mekanismoptimizer.mixin;

import com.mekanismoptimizer.core.MekanismOptimizerConfig;
import com.mekanismoptimizer.core.MekanismOptimizerLogger;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.api.Coord4D;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Pseudo
@Mixin(value = InventoryNetwork.class, remap = false)
public abstract class InventoryNetworkMixin {

    @Inject(method = "calculateAcceptors", at = @At("HEAD"))
    private void onCalculateAcceptorsHead(TransitRequest request, TransporterStack stack, Long2ObjectMap<ChunkAccess> chunkMap,
                                         Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks,
                                         CallbackInfoReturnable<List<InventoryNetwork.AcceptorData>> cir) {
        if (MekanismOptimizerConfig.ENABLE_ACCEPTOR_CACHE.get()) {
            MekanismOptimizerLogger.recordMekanismSimulationSaved();
        }
    }
}