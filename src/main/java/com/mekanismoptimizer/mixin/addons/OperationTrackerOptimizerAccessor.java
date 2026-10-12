package com.mekanismoptimizer.mixin.addons;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Pseudo
@Mixin(value = OperationTracker.class, remap = false)
public interface OperationTrackerOptimizerAccessor {

    @Mutable
    @Accessor("lastErrors")
    void setLastErrors(Set<RecipeError> lastErrors);

    @Mutable
    @Accessor("errors")
    void setErrors(Set<RecipeError> errors);

    @Mutable
    @Accessor("checkAll")
    void setCheckAll(boolean checkAll);

    @Mutable
    @Accessor("checkedErrors")
    void setCheckedErrors(boolean checkedErrors);

    @Mutable
    @Accessor("currentMax")
    void setCurrentMax(int currentMax);

    @Mutable
    @Accessor("maxForEnergy")
    void setMaxForEnergy(int maxForEnergy);
}
