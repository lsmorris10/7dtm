package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class TraderInventory {

    public record TraderOffer(ItemStack item, int buyPrice, int sellPrice, int maxStock) {
        public TraderOffer copy() {
            return new TraderOffer(item.copy(), buyPrice, sellPrice, maxStock);
        }
    }

    public static List<TraderOffer> getOffersForTier(int tier) {
        List<TraderOffer> offers = new ArrayList<>();
        switch (tier) {
            case 1 -> addTier1(offers);
            case 2 -> addTier2(offers);
            default -> addTier3(offers);
        }
        return offers;
    }

    private static void addTier1(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(Items.STONE_SWORD), 50, 10, 5));
        offers.add(new TraderOffer(new ItemStack(Items.STONE_PICKAXE), 50, 10, 5));
        offers.add(new TraderOffer(new ItemStack(Items.STONE_AXE), 50, 10, 5));
        offers.add(new TraderOffer(new ItemStack(Items.STONE_SHOVEL), 30, 5, 5));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_HELMET), 40, 8, 3));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_CHESTPLATE), 60, 12, 3));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_LEGGINGS), 50, 10, 3));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_BOOTS), 40, 8, 3));
        offers.add(new TraderOffer(new ItemStack(Items.BREAD, 4), 20, 3, 10));
        offers.add(new TraderOffer(new ItemStack(Items.COOKED_BEEF, 4), 30, 5, 10));
        offers.add(new TraderOffer(new ItemStack(Items.TORCH, 16), 15, 1, 10));
        offers.add(new TraderOffer(new ItemStack(Items.ARROW, 16), 25, 2, 10));
        offers.add(new TraderOffer(new ItemStack(Items.BOW), 80, 15, 3));
        offers.add(new TraderOffer(new ItemStack(ModItems.BANDAGE.get(), 2), 30, 5, 8));
        offers.add(new TraderOffer(new ItemStack(ModItems.BOILED_WATER.get(), 2), 15, 2, 10));
    }

    private static void addTier2(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(Items.IRON_SWORD), 150, 30, 3));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_PICKAXE), 150, 30, 3));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_AXE), 150, 30, 3));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_HELMET), 120, 25, 2));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_CHESTPLATE), 200, 40, 2));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_LEGGINGS), 170, 35, 2));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_BOOTS), 120, 25, 2));
        offers.add(new TraderOffer(new ItemStack(Items.SHIELD), 100, 20, 3));
        offers.add(new TraderOffer(new ItemStack(ModItems.ANTIBIOTICS.get(), 2), 80, 15, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.FIRST_AID_KIT.get()), 100, 20, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.SPLINT.get(), 2), 40, 8, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.AMMO_9MM.get(), 30), 100, 15, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.FORGED_IRON.get(), 8), 60, 10, 8));
        offers.add(new TraderOffer(new ItemStack(ModItems.MECHANICAL_PARTS.get(), 4), 80, 15, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.NAIL.get(), 16), 40, 5, 8));
    }

    private static void addTier3(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_SWORD), 500, 100, 2));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_PICKAXE), 500, 100, 2));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_CHESTPLATE), 600, 120, 1));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_HELMET), 400, 80, 1));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_LEGGINGS), 500, 100, 1));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_BOOTS), 400, 80, 1));
        offers.add(new TraderOffer(new ItemStack(ModItems.AMMO_762.get(), 30), 200, 30, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.FORGED_STEEL.get(), 8), 200, 35, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.ELECTRICAL_PARTS.get(), 4), 250, 45, 3));
        offers.add(new TraderOffer(new ItemStack(ModItems.POLYMER.get(), 8), 150, 25, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.ACID.get(), 4), 120, 20, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.SPRING.get(), 8), 100, 15, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.DUCT_TAPE.get(), 4), 80, 12, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.FIRST_AID_KIT.get(), 3), 250, 50, 3));
        offers.add(new TraderOffer(new ItemStack(ModItems.GAS_CAN.get(), 2), 200, 35, 3));
    }

    public static int getSellValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.getItem() == Items.IRON_INGOT) return 5;
        if (stack.getItem() == Items.GOLD_INGOT) return 15;
        if (stack.getItem() == Items.DIAMOND) return 50;
        if (stack.getItem() == Items.EMERALD) return 30;
        if (stack.getItem() == Items.IRON_SWORD) return 20;
        if (stack.getItem() == Items.IRON_PICKAXE) return 20;
        if (stack.getItem() == Items.IRON_AXE) return 20;
        if (stack.getItem() == Items.DIAMOND_SWORD) return 80;
        if (stack.getItem() == Items.DIAMOND_PICKAXE) return 80;
        if (stack.getItem() == Items.ROTTEN_FLESH) return 1;
        if (stack.getItem() == Items.BONE) return 2;
        if (stack.getItem() == Items.STRING) return 2;
        if (stack.getItem() == Items.GUNPOWDER) return 5;
        if (stack.getItem() == Items.LEATHER) return 3;
        if (stack.getItem() == ModItems.IRON_SCRAP.get()) return 2;
        if (stack.getItem() == ModItems.LEAD.get()) return 3;
        if (stack.getItem() == ModItems.MECHANICAL_PARTS.get()) return 10;
        if (stack.getItem() == ModItems.ELECTRICAL_PARTS.get()) return 20;
        if (stack.getItem() == ModItems.FORGED_IRON.get()) return 5;
        if (stack.getItem() == ModItems.FORGED_STEEL.get()) return 15;
        if (stack.getItem() == ModItems.POLYMER.get()) return 10;
        return 1;
    }
}
