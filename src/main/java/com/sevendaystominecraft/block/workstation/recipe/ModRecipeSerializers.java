package com.sevendaystominecraft.block.workstation.recipe;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<RecipeSerializer<WorkstationCraftingRecipe>> CAMPFIRE =
            RECIPE_SERIALIZERS.register("campfire_workstation",
                    () -> new WorkstationCraftingRecipe.Serializer(ModRecipeTypes.CAMPFIRE.get()));

    public static final Supplier<RecipeSerializer<WorkstationCraftingRecipe>> GRILL =
            RECIPE_SERIALIZERS.register("grill_workstation",
                    () -> new WorkstationCraftingRecipe.Serializer(ModRecipeTypes.GRILL.get()));

    public static final Supplier<RecipeSerializer<WorkstationCraftingRecipe>> FORGE =
            RECIPE_SERIALIZERS.register("forge_workstation",
                    () -> new WorkstationCraftingRecipe.Serializer(ModRecipeTypes.FORGE.get()));

    public static final Supplier<RecipeSerializer<WorkstationCraftingRecipe>> CEMENT_MIXER =
            RECIPE_SERIALIZERS.register("cement_mixer_workstation",
                    () -> new WorkstationCraftingRecipe.Serializer(ModRecipeTypes.CEMENT_MIXER.get()));

    public static final Supplier<RecipeSerializer<WorkstationCraftingRecipe>> WORKBENCH =
            RECIPE_SERIALIZERS.register("workbench_workstation",
                    () -> new WorkstationCraftingRecipe.Serializer(ModRecipeTypes.WORKBENCH.get()));

    public static final Supplier<RecipeSerializer<WorkstationCraftingRecipe>> CHEMISTRY_STATION =
            RECIPE_SERIALIZERS.register("chemistry_station_workstation",
                    () -> new WorkstationCraftingRecipe.Serializer(ModRecipeTypes.CHEMISTRY_STATION.get()));

    public static final Supplier<RecipeSerializer<WorkstationCraftingRecipe>> ADVANCED_WORKBENCH =
            RECIPE_SERIALIZERS.register("advanced_workbench_workstation",
                    () -> new WorkstationCraftingRecipe.Serializer(ModRecipeTypes.ADVANCED_WORKBENCH.get()));
}
