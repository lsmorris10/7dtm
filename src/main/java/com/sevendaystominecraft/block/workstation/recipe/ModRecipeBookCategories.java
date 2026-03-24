package com.sevendaystominecraft.block.workstation.recipe;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRecipeBookCategories {

    public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES =
            DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<RecipeBookCategory> WORKSTATION =
            RECIPE_BOOK_CATEGORIES.register("workstation", RecipeBookCategory::new);
}
