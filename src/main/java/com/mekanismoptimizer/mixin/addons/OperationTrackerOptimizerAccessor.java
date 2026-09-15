package com.mekanismoptimizer.mixin.addons;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Pseudo
@Mixin(value = OperationTracker.class, remap = false)
public interface OperationTrackerOptimizerAccessor {

    @Accessor("lastErrors")
    void setLastErrors(Set<RecipeError> lastErrors);

    @Accessor("errors")
    void setErrors(Set<RecipeError> errors);

    @Accessor("checkAll")
    void setCheckAll(boolean checkAll);

    @Accessor("checkedErrors")
    void setCheckedErrors(boolean checkedErrors);

    @Accessor("currentMax")
    void setCurrentMax(int currentMax);

    @Accessor("maxForEnergy")
    void setMaxForEnergy(int maxForEnergy);
}
