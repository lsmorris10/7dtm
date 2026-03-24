package com.sevendaystominecraft.block.workstation.recipe;

import com.mojang.serialization.MapCodec;
import com.sevendaystominecraft.capability.WaterBottleConversionHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

public class WaterBottleIngredient implements ICustomIngredient {

    public static final WaterBottleIngredient INSTANCE = new WaterBottleIngredient();

    public static final MapCodec<WaterBottleIngredient> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(ItemStack stack) {
        return WaterBottleConversionHandler.isVanillaWaterBottle(stack);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Stream.of(Items.POTION.builtInRegistryHolder());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return ModIngredientTypes.WATER_BOTTLE.get();
    }
}
