package com.sevendaystominecraft.block.workstation;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public record WorkstationRecipe(
        List<Ingredient> inputs,
        ItemStack output,
        int processingTicks
) {
    public record Ingredient(Item item, int count, Predicate<ItemStack> stackFilter) {
        public Ingredient(Item item, int count) {
            this(item, count, null);
        }

        public boolean testStack(ItemStack stack) {
            if (!stack.is(item)) return false;
            return stackFilter == null || stackFilter.test(stack);
        }
    }

    public boolean matches(java.util.function.Function<Ingredient, Integer> ingredientCounter) {
        for (Ingredient ing : inputs) {
            if (ingredientCounter.apply(ing) < ing.count()) return false;
        }
        return true;
    }

    public void consumeInputs(java.util.function.BiConsumer<Ingredient, Integer> ingredientConsumer) {
        for (Ingredient ing : inputs) {
            ingredientConsumer.accept(ing, ing.count());
        }
    }
}
