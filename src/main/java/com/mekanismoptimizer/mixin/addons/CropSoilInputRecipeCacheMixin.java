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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Pseudo
@Mixin(value = CropSoilInputRecipeCache.class, remap = false)
public abstract class CropSoilInputRecipeCacheMixin {

    @Shadow private List<CropSoilRecipe> allRecipes;
    @Shadow public abstract void initCacheIfNeeded(Level world);

    @Unique
    private static final class CropSoilFluidKey {
        final Item crop;
        final Item soil;
        final Fluid fluid;
        final int hash;

        CropSoilFluidKey(Item crop, Item soil, Fluid fluid) {
            this.crop = crop;
            this.soil = soil;
            this.fluid = fluid;
            int h = crop != null ? crop.hashCode() : 0;
            h = 31 * h + (soil != null ? soil.hashCode() : 0);
            h = 31 * h + (fluid != null ? fluid.hashCode() : 0);
            this.hash = h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CropSoilFluidKey other)) return false;
            return this.crop == other.crop && this.soil == other.soil && this.fluid == other.fluid;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    @Unique
    private static final class PairKey {
        final Object first;
        final Object second;
        final int hash;

        PairKey(Object first, Object second) {
            this.first = first;
            this.second = second;
            int h = first != null ? first.hashCode() : 0;
            this.hash = 31 * h + (second != null ? second.hashCode() : 0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PairKey other)) return false;
            return this.first == other.first && this.second == other.second;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    @Unique private final Set<Item> mekanism_optimizer$validCrops = new HashSet<>();
    @Unique private final Set<Item> mekanism_optimizer$validSoils = new HashSet<>();
    @Unique private final Set<Fluid> mekanism_optimizer$validFluids = new HashSet<>();

    @Unique private final Set<PairKey> mekanism_optimizer$cropSoilPairs = new HashSet<>();
    @Unique private final Set<PairKey> mekanism_optimizer$cropFluidPairs = new HashSet<>();
    @Unique private final Set<PairKey> mekanism_optimizer$soilFluidPairs = new HashSet<>();

    @Unique private final Map<CropSoilFluidKey, CropSoilRecipe> mekanism_optimizer$tripleRecipeMap = new ConcurrentHashMap<>();
    @Unique private boolean mekanism_optimizer$cacheBuilt = false;

    @Unique
    private void mekanism_optimizer$buildFastCache(Level world) {
        initCacheIfNeeded(world);
        if (!mekanism_optimizer$cacheBuilt && allRecipes != null && !allRecipes.isEmpty()) {
            mekanism_optimizer$validCrops.clear();
            mekanism_optimizer$validSoils.clear();
            mekanism_optimizer$validFluids.clear();
            mekanism_optimizer$cropSoilPairs.clear();
            mekanism_optimizer$cropFluidPairs.clear();
            mekanism_optimizer$soilFluidPairs.clear();
            mekanism_optimizer$tripleRecipeMap.clear();

            for (CropSoilRecipe recipe : allRecipes) {
                List<ItemStack> cropReps = (recipe.getCrop() != null && recipe.getCrop().getRepresentations() != null)
                        ? recipe.getCrop().getRepresentations() : List.of();
                List<ItemStack> soilReps = (recipe.getSoil() != null && recipe.getSoil().getRepresentations() != null)
                        ? recipe.getSoil().getRepresentations() : List.of();
                List<FluidStack> fluidReps = (recipe.getWater() != null && recipe.getWater().getRepresentations() != null)
                        ? recipe.getWater().getRepresentations() : List.of();

                Set<Item> recipeCrops = new HashSet<>();
                for (ItemStack cs : cropReps) {
                    if (cs != null && !cs.isEmpty()) {
                        Item item = cs.getItem();
                        mekanism_optimizer$validCrops.add(item);
                        recipeCrops.add(item);
                    }
                }

                Set<Item> recipeSoils = new HashSet<>();
                for (ItemStack ss : soilReps) {
                    if (ss != null && !ss.isEmpty()) {
                        Item item = ss.getItem();
                        mekanism_optimizer$validSoils.add(item);
                        recipeSoils.add(item);
                    }
                }

                Set<Fluid> recipeFluids = new HashSet<>();
                for (FluidStack fs : fluidReps) {
                    if (fs != null && !fs.isEmpty()) {
                        Fluid fluid = fs.getFluid();
                        mekanism_optimizer$validFluids.add(fluid);
                        recipeFluids.add(fluid);
                    }
                }

                for (Item crop : recipeCrops) {
                    for (Item soil : recipeSoils) {
                        mekanism_optimizer$cropSoilPairs.add(new PairKey(crop, soil));
                        for (Fluid fluid : recipeFluids) {
                            mekanism_optimizer$tripleRecipeMap.put(new CropSoilFluidKey(crop, soil, fluid), recipe);
                        }
                    }
                    for (Fluid fluid : recipeFluids) {
                        mekanism_optimizer$cropFluidPairs.add(new PairKey(crop, fluid));
                    }
                }
                for (Item soil : recipeSoils) {
                    for (Fluid fluid : recipeFluids) {
                        mekanism_optimizer$soilFluidPairs.add(new PairKey(soil, fluid));
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

    @Inject(method = "containsCropOther", at = @At("HEAD"), cancellable = true)
    private void onContainsCropOther(Level world, ItemStack cropStack, ItemStack soilStack, FluidStack fluidStack, CallbackInfoReturnable<Boolean> cir) {
        if (cropStack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        mekanism_optimizer$buildFastCache(world);
        if (mekanism_optimizer$cacheBuilt) {
            Item cropItem = cropStack.getItem();
            if (!mekanism_optimizer$validCrops.contains(cropItem)) {
                cir.setReturnValue(false);
                return;
            }
            boolean hasSoil = !soilStack.isEmpty();
            boolean hasFluid = !fluidStack.isEmpty();

            if (!hasSoil && !hasFluid) {
                cir.setReturnValue(true);
                return;
            }
            if (hasSoil && !hasFluid) {
                cir.setReturnValue(mekanism_optimizer$cropSoilPairs.contains(new PairKey(cropItem, soilStack.getItem())));
                return;
            }
            if (!hasSoil && hasFluid) {
                cir.setReturnValue(mekanism_optimizer$cropFluidPairs.contains(new PairKey(cropItem, fluidStack.getFluid())));
                return;
            }
            cir.setReturnValue(mekanism_optimizer$tripleRecipeMap.containsKey(new CropSoilFluidKey(cropItem, soilStack.getItem(), fluidStack.getFluid())));
        }
    }

    @Inject(method = "containsSoilOther", at = @At("HEAD"), cancellable = true)
    private void onContainsSoilOther(Level world, ItemStack cropStack, ItemStack soilStack, FluidStack fluidStack, CallbackInfoReturnable<Boolean> cir) {
        if (soilStack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        mekanism_optimizer$buildFastCache(world);
        if (mekanism_optimizer$cacheBuilt) {
            Item soilItem = soilStack.getItem();
            if (!mekanism_optimizer$validSoils.contains(soilItem)) {
                cir.setReturnValue(false);
                return;
            }
            boolean hasCrop = !cropStack.isEmpty();
            boolean hasFluid = !fluidStack.isEmpty();

            if (!hasCrop && !hasFluid) {
                cir.setReturnValue(true);
                return;
            }
            if (hasCrop && !hasFluid) {
                cir.setReturnValue(mekanism_optimizer$cropSoilPairs.contains(new PairKey(cropStack.getItem(), soilItem)));
                return;
            }
            if (!hasCrop && hasFluid) {
                cir.setReturnValue(mekanism_optimizer$soilFluidPairs.contains(new PairKey(soilItem, fluidStack.getFluid())));
                return;
            }
            cir.setReturnValue(mekanism_optimizer$tripleRecipeMap.containsKey(new CropSoilFluidKey(cropStack.getItem(), soilItem, fluidStack.getFluid())));
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
            Fluid fluid = fluidStack.getFluid();
            if (!mekanism_optimizer$validFluids.contains(fluid)) {
                cir.setReturnValue(false);
                return;
            }
            boolean hasCrop = !cropStack.isEmpty();
            boolean hasSoil = !soilStack.isEmpty();

            if (!hasCrop && !hasSoil) {
                cir.setReturnValue(true);
                return;
            }
            if (hasCrop && !hasSoil) {
                cir.setReturnValue(mekanism_optimizer$cropFluidPairs.contains(new PairKey(cropStack.getItem(), fluid)));
                return;
            }
            if (!hasCrop && hasSoil) {
                cir.setReturnValue(mekanism_optimizer$soilFluidPairs.contains(new PairKey(soilStack.getItem(), fluid)));
                return;
            }
            // All 3 present: instant O(1) hash check! No fallthrough!
            cir.setReturnValue(mekanism_optimizer$tripleRecipeMap.containsKey(new CropSoilFluidKey(cropStack.getItem(), soilStack.getItem(), fluid)));
        }
    }

    @Inject(method = "findFirstRecipe", at = @At("HEAD"), cancellable = true)
    private void onFindFirstRecipe(Level world, ItemStack cropStack, ItemStack soilStack, FluidStack fluidStack, CallbackInfoReturnable<CropSoilRecipe> cir) {
        if (cropStack.isEmpty() || soilStack.isEmpty() || fluidStack.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }
        mekanism_optimizer$buildFastCache(world);
        if (mekanism_optimizer$cacheBuilt) {
            CropSoilRecipe recipe = mekanism_optimizer$tripleRecipeMap.get(new CropSoilFluidKey(cropStack.getItem(), soilStack.getItem(), fluidStack.getFluid()));
            cir.setReturnValue(recipe);
        }
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void onClear(CallbackInfo ci) {
        mekanism_optimizer$validCrops.clear();
        mekanism_optimizer$validSoils.clear();
        mekanism_optimizer$validFluids.clear();
        mekanism_optimizer$cropSoilPairs.clear();
        mekanism_optimizer$cropFluidPairs.clear();
        mekanism_optimizer$soilFluidPairs.clear();
        mekanism_optimizer$tripleRecipeMap.clear();
        mekanism_optimizer$cacheBuilt = false;
    }
}

