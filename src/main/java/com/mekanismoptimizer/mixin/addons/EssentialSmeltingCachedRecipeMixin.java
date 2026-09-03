package com.mekanismoptimizer.mixin.addons;

import astral_mekanism.generalrecipe.cachedrecipe.EssentialSmeltingCachedRecipe;
import astral_mekanism.generalrecipe.cachedrecipe.GeneralCachedRecipe;
import astral_mekanism.recipes.output.ItemInfuseOutput;
import astral_mekanism.registries.AMEInfuseTypes;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.IntSupplier;

@Pseudo
@Mixin(value = EssentialSmeltingCachedRecipe.class, remap = false)
public abstract class EssentialSmeltingCachedRecipeMixin {

    @Shadow @Final private IInputHandler<ItemStack> inputHandler;
    @Shadow @Final private IOutputHandler<ItemInfuseOutput> outputHandler;
    @Shadow @Final private ItemStackIngredient inputIngredient;
    @Shadow @Final private IntSupplier xpUpgrade;
    @Shadow private ItemStack recipeInput;
    @Shadow private ItemInfuseOutput recipeOutput;

    @Unique private long mekanism_optimizer$lastXp = -1;
    @Unique private int mekanism_optimizer$lastXpUpgrade = -1;
    @Unique private InfusionStack mekanism_optimizer$cachedXpStack = InfusionStack.EMPTY;
    @Unique private ItemStack mekanism_optimizer$cachedResultItem = ItemStack.EMPTY;

    @SuppressWarnings("unchecked")
    @Inject(method = "calculateOperationsThisTick", at = @At("HEAD"), cancellable = true)
    private void onCalculateOperationsThisTick(OperationTracker tracker, CallbackInfo ci) {
        if (!tracker.shouldContinueChecking()) {
            ci.cancel();
            return;
        }

        ItemStack inputStack = inputHandler.getInput();
        if (inputStack.isEmpty()) {
            tracker.mismatchedRecipe();
            ci.cancel();
            return;
        }

        SmeltingRecipe recipe = ((GeneralCachedRecipe<SmeltingRecipe>) (Object) this).getRecipe();
        int currentUpgrade = xpUpgrade.getAsInt();
        if (currentUpgrade != mekanism_optimizer$lastXpUpgrade || mekanism_optimizer$cachedResultItem.isEmpty()) {
            mekanism_optimizer$lastXpUpgrade = currentUpgrade;
            long xp = (long) (recipe.getExperience() * 100 * (1L << (currentUpgrade * 2)));
            mekanism_optimizer$lastXp = xp;
            mekanism_optimizer$cachedXpStack = xp <= 0 ? InfusionStack.EMPTY : AMEInfuseTypes.XP.getStack(xp);
            mekanism_optimizer$cachedResultItem = recipe.getResultItem(null);
            this.recipeOutput = new ItemInfuseOutput(mekanism_optimizer$cachedResultItem, mekanism_optimizer$cachedXpStack);
        }

        this.recipeInput = inputHandler.getRecipeInput(inputIngredient);
        if (this.recipeInput.isEmpty() || this.recipeOutput.itemStack().isEmpty()) {
            tracker.mismatchedRecipe();
            ci.cancel();
            return;
        }

        inputHandler.calculateOperationsCanSupport(tracker, this.recipeInput);
        outputHandler.calculateOperationsCanSupport(tracker, this.recipeOutput);
        ci.cancel();
    }
}
