package com.sevendaystominecraft.crafting;

import com.sevendaystominecraft.item.ModItems;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ScrappingSystem {

    private static final Random RANDOM = new Random();

    private static final Set<Item> TOOLS = Set.of(
            Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_SWORD,
            Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE, Items.STONE_SWORD,
            Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_SWORD,
            Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.WOODEN_SWORD,
            Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.NETHERITE_SWORD
    );

    private static final Set<Item> ARMOR = Set.of(
            Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS
    );

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

        if (TOOLS.contains(item)) {
            addScrapResult(results, ModItems.IRON_SCRAP.get(), 2, 6, yieldMultiplier);
            if (RANDOM.nextFloat() < 0.3f) {
                addScrapResult(results, ModItems.MECHANICAL_PARTS.get(), 1, 1, yieldMultiplier);
            }
        } else if (ARMOR.contains(item)) {
            addScrapResult(results, Items.LEATHER, 1, 2, yieldMultiplier);
            addScrapResult(results, Items.STRING, 3, 5, yieldMultiplier);
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
