package com.sevendaystominecraft.block.workstation.recipe;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModIngredientTypes {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<IngredientType<WaterBottleIngredient>> WATER_BOTTLE =
            INGREDIENT_TYPES.register("water_bottle",
                    () -> new IngredientType<>(WaterBottleIngredient.CODEC));
}
