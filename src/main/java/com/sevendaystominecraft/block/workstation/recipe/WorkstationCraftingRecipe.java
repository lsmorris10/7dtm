package com.sevendaystominecraft.block.workstation.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

public class WorkstationCraftingRecipe implements Recipe<WorkstationRecipeInput> {

    private final List<SizedIngredient> ingredients;
    private final ItemStack result;
    private final int processingTicks;
    private final RecipeType<WorkstationCraftingRecipe> type;
    private final RecipeSerializer<WorkstationCraftingRecipe> serializer;

    public WorkstationCraftingRecipe(List<SizedIngredient> ingredients, ItemStack result, int processingTicks,
                                     RecipeType<WorkstationCraftingRecipe> type,
                                     RecipeSerializer<WorkstationCraftingRecipe> serializer) {
        this.ingredients = ingredients;
        this.result = result;
        this.processingTicks = processingTicks;
        this.type = type;
        this.serializer = serializer;
    }

    public List<SizedIngredient> getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getProcessingTicks() {
        return processingTicks;
    }

    @Override
    public boolean matches(WorkstationRecipeInput input, Level level) {
        for (SizedIngredient required : ingredients) {
            int count = 0;
            for (int i = 0; i < input.size(); i++) {
                ItemStack stack = input.getItem(i);
                if (required.ingredient().test(stack)) {
                    count += stack.getCount();
                }
            }
            if (count < required.count()) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(WorkstationRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.WORKSTATION.get();
    }

    @Override
    public RecipeSerializer<WorkstationCraftingRecipe> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<WorkstationCraftingRecipe> getType() {
        return type;
    }

    public static class Serializer implements RecipeSerializer<WorkstationCraftingRecipe> {

        private final RecipeType<WorkstationCraftingRecipe> recipeType;
        private final MapCodec<WorkstationCraftingRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, WorkstationCraftingRecipe> streamCodec;

        public Serializer(RecipeType<WorkstationCraftingRecipe> recipeType) {
            this.recipeType = recipeType;
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SizedIngredient.NESTED_CODEC.listOf().fieldOf("ingredients")
                            .forGetter(WorkstationCraftingRecipe::getIngredients),
                    ItemStack.STRICT_CODEC.fieldOf("result")
                            .forGetter(WorkstationCraftingRecipe::getResult),
                    Codec.INT.optionalFieldOf("processing_ticks", 0)
                            .forGetter(WorkstationCraftingRecipe::getProcessingTicks)
            ).apply(instance, (ings, res, ticks) -> new WorkstationCraftingRecipe(ings, res, ticks, recipeType, this)));

            this.streamCodec = StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    WorkstationCraftingRecipe::getIngredients,
                    ItemStack.STREAM_CODEC,
                    WorkstationCraftingRecipe::getResult,
                    ByteBufCodecs.INT,
                    WorkstationCraftingRecipe::getProcessingTicks,
                    (ings, res, ticks) -> new WorkstationCraftingRecipe(ings, res, ticks, recipeType, this)
            );
        }

        @Override
        public MapCodec<WorkstationCraftingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WorkstationCraftingRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
