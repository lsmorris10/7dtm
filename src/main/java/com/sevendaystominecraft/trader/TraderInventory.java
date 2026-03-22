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

    public static List<TraderOffer> getOffersForTrader(String traderName) {
        List<TraderOffer> offers = new ArrayList<>();
        addCommonStock(offers);
        switch (traderName) {
            case "Trader Joel" -> addJoelStock(offers);
            case "Trader Rekt" -> addRektStock(offers);
            case "Trader Jen" -> addJenStock(offers);
            case "Trader Bob" -> addBobStock(offers);
            case "Trader Hugh" -> addHughStock(offers);
            default -> addJoelStock(offers);
        }
        return offers;
    }

    public static List<TraderOffer> getSecretStash(String traderName) {
        List<TraderOffer> stash = new ArrayList<>();
        switch (traderName) {
            case "Trader Joel" -> addJoelSecretStash(stash);
            case "Trader Rekt" -> addRektSecretStash(stash);
            case "Trader Jen" -> addJenSecretStash(stash);
            case "Trader Bob" -> addBobSecretStash(stash);
            case "Trader Hugh" -> addHughSecretStash(stash);
            default -> addJoelSecretStash(stash);
        }
        return stash;
    }

    @Deprecated
    public static List<TraderOffer> getOffersForTier(int tier) {
        return switch (tier) {
            case 1 -> getOffersForTrader("Trader Joel");
            case 2 -> getOffersForTrader("Trader Rekt");
            default -> getOffersForTrader("Trader Hugh");
        };
    }

    private static void addCommonStock(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(Items.BREAD, 4), 20, 3, 10));
        offers.add(new TraderOffer(new ItemStack(Items.COOKED_BEEF, 4), 30, 5, 10));
        offers.add(new TraderOffer(new ItemStack(Items.TORCH, 16), 15, 1, 10));
        offers.add(new TraderOffer(new ItemStack(ModItems.BOILED_WATER.get(), 2), 15, 2, 10));
        offers.add(new TraderOffer(new ItemStack(ModItems.BANDAGE.get(), 2), 30, 5, 8));
    }

    private static void addJoelStock(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(Items.STONE_SWORD), 50, 10, 5));
        offers.add(new TraderOffer(new ItemStack(Items.STONE_PICKAXE), 50, 10, 5));
        offers.add(new TraderOffer(new ItemStack(Items.STONE_AXE), 50, 10, 5));
        offers.add(new TraderOffer(new ItemStack(Items.STONE_SHOVEL), 30, 5, 5));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_SWORD), 150, 30, 3));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_PICKAXE), 150, 30, 3));
        offers.add(new TraderOffer(new ItemStack(Items.BOW), 80, 15, 3));
        offers.add(new TraderOffer(new ItemStack(Items.ARROW, 16), 25, 2, 10));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_CHESTPLATE), 60, 12, 3));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_BOOTS), 40, 8, 3));
        offers.add(new TraderOffer(new ItemStack(ModItems.FORGED_IRON.get(), 8), 60, 10, 8));
        offers.add(new TraderOffer(new ItemStack(ModItems.NAIL.get(), 16), 40, 5, 8));
    }

    private static void addRektStock(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(Items.IRON_SWORD), 150, 30, 3));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_SWORD), 500, 100, 2));
        offers.add(new TraderOffer(new ItemStack(Items.BOW), 80, 15, 3));
        offers.add(new TraderOffer(new ItemStack(Items.CROSSBOW), 200, 40, 2));
        offers.add(new TraderOffer(new ItemStack(Items.ARROW, 32), 40, 3, 10));
        offers.add(new TraderOffer(new ItemStack(ModItems.AMMO_9MM.get(), 30), 100, 15, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.AMMO_762.get(), 30), 200, 30, 5));
        offers.add(new TraderOffer(new ItemStack(Items.SHIELD), 100, 20, 3));
        offers.add(new TraderOffer(new ItemStack(Items.TNT, 2), 150, 25, 3));
        offers.add(new TraderOffer(new ItemStack(ModItems.FORGED_STEEL.get(), 4), 120, 20, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.MECHANICAL_PARTS.get(), 4), 80, 15, 5));
    }

    private static void addJenStock(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(ModItems.ANTIBIOTICS.get(), 2), 80, 15, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.FIRST_AID_KIT.get()), 100, 20, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.SPLINT.get(), 2), 40, 8, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.BANDAGE.get(), 4), 50, 8, 8));
        offers.add(new TraderOffer(new ItemStack(Items.GOLDEN_APPLE), 200, 40, 2));
        offers.add(new TraderOffer(new ItemStack(Items.BOOK, 4), 30, 5, 8));
        offers.add(new TraderOffer(new ItemStack(Items.EXPERIENCE_BOTTLE, 4), 80, 15, 5));
        offers.add(new TraderOffer(new ItemStack(Items.GLASS_BOTTLE, 8), 20, 2, 10));
        offers.add(new TraderOffer(new ItemStack(ModItems.ACID.get(), 4), 120, 20, 5));
        offers.add(new TraderOffer(new ItemStack(Items.BREWING_STAND), 150, 30, 2));
    }

    private static void addBobStock(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(Items.IRON_PICKAXE), 150, 30, 3));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_AXE), 150, 30, 3));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_SHOVEL), 100, 20, 3));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_PICKAXE), 500, 100, 2));
        offers.add(new TraderOffer(new ItemStack(ModItems.FORGED_IRON.get(), 16), 100, 10, 8));
        offers.add(new TraderOffer(new ItemStack(ModItems.FORGED_STEEL.get(), 8), 200, 35, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.MECHANICAL_PARTS.get(), 8), 150, 15, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.ELECTRICAL_PARTS.get(), 4), 250, 45, 3));
        offers.add(new TraderOffer(new ItemStack(ModItems.NAIL.get(), 32), 60, 5, 8));
        offers.add(new TraderOffer(new ItemStack(ModItems.SPRING.get(), 8), 100, 15, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.DUCT_TAPE.get(), 4), 80, 12, 5));
    }

    private static void addHughStock(List<TraderOffer> offers) {
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_HELMET), 40, 8, 3));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_CHESTPLATE), 60, 12, 3));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_LEGGINGS), 50, 10, 3));
        offers.add(new TraderOffer(new ItemStack(Items.LEATHER_BOOTS), 40, 8, 3));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_HELMET), 120, 25, 2));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_CHESTPLATE), 200, 40, 2));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_LEGGINGS), 170, 35, 2));
        offers.add(new TraderOffer(new ItemStack(Items.IRON_BOOTS), 120, 25, 2));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_CHESTPLATE), 600, 120, 1));
        offers.add(new TraderOffer(new ItemStack(Items.DIAMOND_HELMET), 400, 80, 1));
        offers.add(new TraderOffer(new ItemStack(ModItems.POLYMER.get(), 8), 150, 25, 5));
        offers.add(new TraderOffer(new ItemStack(ModItems.GAS_CAN.get(), 2), 200, 35, 3));
    }

    private static void addJoelSecretStash(List<TraderOffer> stash) {
        stash.add(new TraderOffer(new ItemStack(Items.DIAMOND_SWORD), 500, 100, 1));
        stash.add(new TraderOffer(new ItemStack(Items.DIAMOND_PICKAXE), 500, 100, 1));
        stash.add(new TraderOffer(new ItemStack(ModItems.FIRST_AID_KIT.get(), 3), 250, 50, 2));
    }

    private static void addRektSecretStash(List<TraderOffer> stash) {
        stash.add(new TraderOffer(new ItemStack(ModItems.AMMO_762.get(), 64), 400, 60, 2));
        stash.add(new TraderOffer(new ItemStack(ModItems.AMMO_9MM.get(), 64), 250, 40, 2));
        stash.add(new TraderOffer(new ItemStack(Items.TNT, 8), 500, 80, 1));
    }

    private static void addJenSecretStash(List<TraderOffer> stash) {
        stash.add(new TraderOffer(new ItemStack(ModItems.FIRST_AID_KIT.get(), 5), 400, 80, 2));
        stash.add(new TraderOffer(new ItemStack(ModItems.ANTIBIOTICS.get(), 8), 300, 50, 2));
        stash.add(new TraderOffer(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 800, 150, 1));
    }

    private static void addBobSecretStash(List<TraderOffer> stash) {
        stash.add(new TraderOffer(new ItemStack(ModItems.ELECTRICAL_PARTS.get(), 16), 500, 90, 2));
        stash.add(new TraderOffer(new ItemStack(ModItems.FORGED_STEEL.get(), 32), 600, 100, 2));
        stash.add(new TraderOffer(new ItemStack(ModItems.MECHANICAL_PARTS.get(), 16), 350, 60, 2));
    }

    private static void addHughSecretStash(List<TraderOffer> stash) {
        stash.add(new TraderOffer(new ItemStack(Items.DIAMOND_CHESTPLATE), 600, 120, 1));
        stash.add(new TraderOffer(new ItemStack(Items.DIAMOND_LEGGINGS), 500, 100, 1));
        stash.add(new TraderOffer(new ItemStack(Items.DIAMOND_BOOTS), 400, 80, 1));
    }

    public static int getBuyPrice(int basePrice, int betterBarterRank, float difficultyMult) {
        float multiplier = 1.0f + (6 - betterBarterRank) * 0.1f;
        return Math.max(1, Math.round(basePrice * multiplier * difficultyMult));
    }

    public static int getSellValue(ItemStack stack) {
        return getSellValue(stack, 0);
    }

    public static int getSellValue(ItemStack stack, int betterBarterRank) {
        if (stack.isEmpty()) return 0;
        int baseValue = getBaseSellValue(stack);
        if (betterBarterRank > 0) {
            float bonus = 1.0f + betterBarterRank * 0.05f;
            return Math.max(1, Math.round(baseValue * bonus));
        }
        return baseValue;
    }

    private static int getBaseSellValue(ItemStack stack) {
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
