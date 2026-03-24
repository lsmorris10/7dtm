package com.sevendaystominecraft.crafting;

import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.item.VanillaGearMaterials;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ScrappingSystem {

    private static final Random RANDOM = new Random();

    private static final Set<Item> ELECTRONICS = Set.of(
            Items.REDSTONE, Items.REDSTONE_TORCH, Items.REPEATER, Items.COMPARATOR
    );

    private static final Set<Item> CANNED_FOOD = Set.of(
            Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.COOKED_MUTTON
    );

    public static List<ItemStack> scrapItem(ItemStack input, boolean atWorkbench) {
        List<ItemStack> results = new ArrayList<>();

        if (input.isEmpty()) return results;

        Item item = input.getItem();
        float yieldMultiplier = atWorkbench ? 1.0f : 0.5f;

        VanillaGearMaterials.MaterialTier materialTier = VanillaGearMaterials.getMaterialTier(item);
        if (materialTier != null) {
            List<VanillaGearMaterials.ScrapOutput> yields = VanillaGearMaterials.getScrapYields(materialTier);
            for (VanillaGearMaterials.ScrapOutput output : yields) {
                addScrapResult(results, output.item(), output.minCount(), output.maxCount(), yieldMultiplier);
            }
        } else if (ELECTRONICS.contains(item)) {
            addScrapResult(results, ModItems.ELECTRICAL_PARTS.get(), 1, 3, yieldMultiplier);
            if (RANDOM.nextFloat() < 0.5f) {
                addScrapResult(results, ModItems.POLYMER.get(), 1, 1, yieldMultiplier);
            }
        } else if (CANNED_FOOD.contains(item)) {
            if (RANDOM.nextFloat() < 0.5f) {
                addScrapResult(results, ModItems.IRON_SCRAP.get(), 1, 1, yieldMultiplier);
            }
        } else {
            addScrapResult(results, ModItems.IRON_SCRAP.get(), 1, 2, yieldMultiplier);
        }

        return results;
    }

    private static void addScrapResult(List<ItemStack> results, Item output, int min, int max, float multiplier) {
        int count = min + RANDOM.nextInt(Math.max(1, max - min + 1));
        count = Math.round(count * multiplier);
        if (count > 0) {
            results.add(new ItemStack(output, count));
        }
    }
}
