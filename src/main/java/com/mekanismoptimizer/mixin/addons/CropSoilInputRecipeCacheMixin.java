package com.mekanismoptimizer.mixin.addons;

import astral_mekanism.generalrecipe.lookup.cache.recipe.CropSoilInputRecipeCache;
import astral_mekanism.generalrecipe.recipe.CropSoilRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Pseudo
@Mixin(value = CropSoilInputRecipeCache.class, remap = false)
public abstract class CropSoilInputRecipeCacheMixin {

    @Shadow private List<CropSoilRecipe> allRecipes;
    @Shadow public abstract void initCacheIfNeeded(Level world);

    @Unique private final Set<Item> mekanism_optimizer$validCrops = new HashSet<>();
    @Unique private final Set<Item> mekanism_optimizer$validSoils = new HashSet<>();
    @Unique private final Set<Fluid> mekanism_optimizer$validFluids = new HashSet<>();
    @Unique private boolean mekanism_optimizer$cacheBuilt = false;

    @Unique
    private void mekanism_optimizer$buildFastCache(Level world) {
        initCacheIfNeeded(world);
        if (!mekanism_optimizer$cacheBuilt && allRecipes != null && !allRecipes.isEmpty()) {
            mekanism_optimizer$validCrops.clear();
            mekanism_optimizer$validSoils.clear();
            mekanism_optimizer$validFluids.clear();
            for (CropSoilRecipe recipe : allRecipes) {
                if (recipe.getCrop() != null && recipe.getCrop().getRepresentations() != null) {
                    for (ItemStack stack : recipe.getCrop().getRepresentations()) {
                        if (stack != null && !stack.isEmpty()) {
                            mekanism_optimizer$validCrops.add(stack.getItem());
                        }
                    }
                }
                if (recipe.getSoil() != null && recipe.getSoil().getRepresentations() != null) {
                    for (ItemStack stack : recipe.getSoil().getRepresentations()) {
                        if (stack != null && !stack.isEmpty()) {
                            mekanism_optimizer$validSoils.add(stack.getItem());
                        }
                    }
                }
                if (recipe.getWater() != null && recipe.getWater().getRepresentations() != null) {
                    for (FluidStack stack : recipe.getWater().getRepresentations()) {
                        if (stack != null && !stack.isEmpty()) {
                            mekanism_optimizer$validFluids.add(stack.getFluid());
                        }
                    }
                }
            }
            mekanism_optimizer$cacheBuilt = true;
        }
    }

    @Inject(method = "containsRecipeCrop", at = @At("HEAD"), cancellable = true)
    private void onContainsRecipeCrop(Level world, ItemStack cropStack, CallbackInfoReturnable<Boolean> cir) {
        if (cropStack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        mekanism_optimizer$buildFastCache(world);
        if (mekanism_optimizer$cacheBuilt) {
            cir.setReturnValue(mekanism_optimizer$validCrops.contains(cropStack.getItem()));
        }
    }

    @Inject(method = "containsRecipeSoil", at = @At("HEAD"), cancellable = true)
    private void onContainsRecipeSoil(Level world, ItemStack soilStack, CallbackInfoReturnable<Boolean> cir) {
        if (soilStack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        mekanism_optimizer$buildFastCache(world);
        if (mekanism_optimizer$cacheBuilt) {
            cir.setReturnValue(mekanism_optimizer$validSoils.contains(soilStack.getItem()));
        }
    }

    @Inject(method = "containsRecipeFluid", at = @At("HEAD"), cancellable = true)
    private void onContainsRecipeFluid(Level world, FluidStack fluidStack, CallbackInfoReturnable<Boolean> cir) {
        if (fluidStack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        mekanism_optimizer$buildFastCache(world);
        if (mekanism_optimizer$cacheBuilt) {
            cir.setReturnValue(mekanism_optimizer$validFluids.contains(fluidStack.getFluid()));
        }
    }

    @Inject(method = "containsFluidOther", at = @At("HEAD"), cancellable = true)
    private void onContainsFluidOther(Level world, ItemStack cropStack, ItemStack soilStack, FluidStack fluidStack, CallbackInfoReturnable<Boolean> cir) {
        if (fluidStack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        mekanism_optimizer$buildFastCache(world);
        if (mekanism_optimizer$cacheBuilt) {
            // Instant O(1) reject if the fluid is not used in ANY greenhouse recipe
            if (!mekanism_optimizer$validFluids.contains(fluidStack.getFluid())) {
                cir.setReturnValue(false);
                return;
            }
            // If crop and soil are empty, fluid validity alone is sufficient
            if (cropStack.isEmpty() && soilStack.isEmpty()) {
                cir.setReturnValue(true);
                return;
            }
            if (!cropStack.isEmpty() && !mekanism_optimizer$validCrops.contains(cropStack.getItem())) {
                cir.setReturnValue(false);
                return;
            }
            if (!soilStack.isEmpty() && !mekanism_optimizer$validSoils.contains(soilStack.getItem())) {
                cir.setReturnValue(false);
                return;
            }
        }
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void onClear(CallbackInfo ci) {
        mekanism_optimizer$validCrops.clear();
        mekanism_optimizer$validSoils.clear();
        mekanism_optimizer$validFluids.clear();
        mekanism_optimizer$cacheBuilt = false;
    }
}
