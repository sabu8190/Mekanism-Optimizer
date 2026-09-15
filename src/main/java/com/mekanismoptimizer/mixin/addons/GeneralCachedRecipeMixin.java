package com.mekanismoptimizer.mixin.addons;

import astral_mekanism.generalrecipe.cachedrecipe.GeneralCachedRecipe;
import astral_mekanism.mixin.mekanism.OperationTrackerMixin;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@Pseudo
@Mixin(value = GeneralCachedRecipe.class, remap = false)
public abstract class GeneralCachedRecipeMixin {

    @Shadow @Final protected Set<RecipeError> errors;
    @Shadow @Final protected BooleanSupplier canHolderFunction;
    @Shadow @Final protected BooleanSupplier recheckAllErrors;
    @Shadow @Final protected IntSupplier baselineMaxOperations;
    @Shadow @Final protected Consumer<OperationTracker> postProcessOperations;
    @Shadow @Final protected BooleanConsumer setActive;
    @Shadow @Final protected IntSupplier requiredTicks;
    @Shadow @Final protected Runnable onFinish;
    @Shadow @Final protected IntConsumer operatingTicksChanged;
    @Shadow protected int operatingTicks;

    @Shadow protected abstract void setupVariableValues();
    @Shadow protected abstract void calculateOperationsThisTick(OperationTracker tracker);
    @Shadow protected abstract void updateErrors(Set<RecipeError> errors);
    @Shadow protected abstract void useEnergy(int operations);
    @Shadow protected abstract void finishProcessing(int operations);
    @Shadow protected abstract void resetCache();
    @Shadow protected abstract void useResources(int operations);

    @Unique
    private OperationTracker mekanism_optimizer$reusableTracker = null;

    /**
     * Optimized process() implementation that reuses the OperationTracker instance per cached recipe
     * to completely eliminate thousands of allocations per second.
     */
    @Inject(method = "process", at = @At("HEAD"), cancellable = true)
    private void onProcessOptimized(CallbackInfo ci) {
        if (!canHolderFunction.getAsBoolean()) {
            if (!errors.isEmpty()) {
                updateErrors(Collections.emptySet());
            }
            ci.cancel();
            return;
        }

        setupVariableValues();

        boolean checkAll = recheckAllErrors.getAsBoolean();
        int startingMax = baselineMaxOperations.getAsInt();

        if (mekanism_optimizer$reusableTracker == null) {
            mekanism_optimizer$reusableTracker = OperationTrackerMixin.astral_mekanism$invokeInit(errors, checkAll, startingMax);
        } else {
            OperationTrackerOptimizerAccessor acc = (OperationTrackerOptimizerAccessor) (Object) mekanism_optimizer$reusableTracker;
            acc.setLastErrors(errors);
            acc.setErrors(Collections.emptySet());
            acc.setCheckAll(checkAll);
            acc.setCheckedErrors(true);
            acc.setCurrentMax(startingMax);
            acc.setMaxForEnergy(startingMax);
        }

        OperationTracker tracker = mekanism_optimizer$reusableTracker;
        calculateOperationsThisTick(tracker);

        if (tracker.shouldContinueChecking()) {
            postProcessOperations.accept(tracker);
            if (tracker.shouldContinueChecking()) {
                if (((OperationTrackerMixin) (Object) tracker).astral_mekanism$invokeCapAtMaxForEnergy()) {
                    tracker.addError(RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE);
                }
            }
        }

        OperationTrackerMixin trackerMixin = (OperationTrackerMixin) (Object) tracker;
        int operations = trackerMixin.astral_mekanism$getCurrentMax();

        if (trackerMixin.astral_mekanism$invokeHasErrorsToCopy()) {
            updateErrors(trackerMixin.astral_mekanism$getErrors());
        }

        if (operations > 0) {
            setActive.accept(true);
            useEnergy(operations);
            operatingTicks++;
            int req = requiredTicks.getAsInt();
            if (operatingTicks >= req) {
                operatingTicks = 0;
                finishProcessing(operations);
                onFinish.run();
                resetCache();
            } else {
                useResources(operations);
            }
            if (req > 1) {
                operatingTicksChanged.accept(operatingTicks);
            }
        } else {
            setActive.accept(false);
            if (operations < 0) {
                operatingTicks = 0;
                operatingTicksChanged.accept(operatingTicks);
                resetCache();
            }
        }
        ci.cancel();
    }
}
