package com.sevendaystominecraft.block.workstation.recipe;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<RecipeType<WorkstationCraftingRecipe>> CAMPFIRE =
            RECIPE_TYPES.register("campfire_workstation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "campfire_workstation")));

    public static final Supplier<RecipeType<WorkstationCraftingRecipe>> GRILL =
            RECIPE_TYPES.register("grill_workstation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "grill_workstation")));

    public static final Supplier<RecipeType<WorkstationCraftingRecipe>> FORGE =
            RECIPE_TYPES.register("forge_workstation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "forge_workstation")));

    public static final Supplier<RecipeType<WorkstationCraftingRecipe>> CEMENT_MIXER =
            RECIPE_TYPES.register("cement_mixer_workstation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "cement_mixer_workstation")));

    public static final Supplier<RecipeType<WorkstationCraftingRecipe>> WORKBENCH =
            RECIPE_TYPES.register("workbench_workstation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "workbench_workstation")));

    public static final Supplier<RecipeType<WorkstationCraftingRecipe>> CHEMISTRY_STATION =
            RECIPE_TYPES.register("chemistry_station_workstation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "chemistry_station_workstation")));

    public static final Supplier<RecipeType<WorkstationCraftingRecipe>> ADVANCED_WORKBENCH =
            RECIPE_TYPES.register("advanced_workbench_workstation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "advanced_workbench_workstation")));
}
