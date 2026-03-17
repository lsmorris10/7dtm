package com.sevendaystominecraft.block.workstation;

import com.sevendaystominecraft.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public final class WorkstationRecipes {

    private static final Map<WorkstationType, List<WorkstationRecipe>> RECIPES = new EnumMap<>(WorkstationType.class);

    static {
        registerCampfireRecipes();
        registerGrillRecipes();
        registerForgeRecipes();
        registerCementMixerRecipes();
        registerWorkbenchRecipes();
        registerChemistryStationRecipes();
        registerAdvancedWorkbenchRecipes();
    }

    private static void registerCampfireRecipes() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        recipes.add(recipe(List.of(ing(Items.BEEF, 1)), new ItemStack(Items.COOKED_BEEF), 200));
        recipes.add(recipe(List.of(ing(Items.PORKCHOP, 1)), new ItemStack(Items.COOKED_PORKCHOP), 200));
        recipes.add(recipe(List.of(ing(Items.CHICKEN, 1)), new ItemStack(Items.COOKED_CHICKEN), 200));
        recipes.add(recipe(List.of(ing(Items.MUTTON, 1)), new ItemStack(Items.COOKED_MUTTON), 200));
        recipes.add(recipe(List.of(ing(Items.COD, 1)), new ItemStack(Items.COOKED_COD), 200));
        recipes.add(recipe(List.of(ing(Items.SALMON, 1)), new ItemStack(Items.COOKED_SALMON), 200));
        recipes.add(recipe(List.of(ing(Items.POTATO, 1)), new ItemStack(Items.BAKED_POTATO), 200));
        RECIPES.put(WorkstationType.CAMPFIRE, recipes);
    }

    private static void registerGrillRecipes() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        recipes.add(recipe(List.of(ing(Items.BEEF, 1)), new ItemStack(Items.COOKED_BEEF), 150));
        recipes.add(recipe(List.of(ing(Items.PORKCHOP, 1)), new ItemStack(Items.COOKED_PORKCHOP), 150));
        recipes.add(recipe(List.of(ing(Items.CHICKEN, 1)), new ItemStack(Items.COOKED_CHICKEN), 150));
        recipes.add(recipe(List.of(ing(Items.MUTTON, 1)), new ItemStack(Items.COOKED_MUTTON), 150));
        recipes.add(recipe(List.of(ing(Items.COD, 1)), new ItemStack(Items.COOKED_COD), 150));
        recipes.add(recipe(List.of(ing(Items.SALMON, 1)), new ItemStack(Items.COOKED_SALMON), 150));
        recipes.add(recipe(List.of(ing(Items.POTATO, 1)), new ItemStack(Items.BAKED_POTATO), 150));
        recipes.add(recipe(List.of(ing(Items.RABBIT, 1)), new ItemStack(Items.COOKED_RABBIT), 150));
        RECIPES.put(WorkstationType.GRILL, recipes);
    }

    private static void registerForgeRecipes() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        recipes.add(recipe(List.of(ing(ModItems.IRON_SCRAP.get(), 1)), new ItemStack(Items.IRON_INGOT), 200));
        recipes.add(recipe(List.of(ing(Items.IRON_INGOT, 1)), new ItemStack(ModItems.FORGED_IRON.get()), 300));
        recipes.add(recipe(List.of(ing(ModItems.LEAD.get(), 1)), new ItemStack(ModItems.FORGED_LEAD.get()), 250));
        recipes.add(recipe(List.of(ing(Items.SAND, 2), ing(Items.CLAY_BALL, 1)), new ItemStack(ModItems.GLASS_JAR.get(), 3), 200));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_IRON.get(), 2)), new ItemStack(ModItems.NAIL.get(), 4), 100));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_IRON.get(), 1)), new ItemStack(ModItems.SPRING.get(), 1), 150));
        RECIPES.put(WorkstationType.FORGE, recipes);
    }

    private static void registerCementMixerRecipes() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        recipes.add(recipe(List.of(ing(Items.SAND, 2), ing(Items.COBBLESTONE, 2)), new ItemStack(ModItems.CONCRETE_MIX.get(), 4), 300));
        recipes.add(recipe(List.of(ing(ModItems.CONCRETE_MIX.get(), 4)), new ItemStack(ModItems.CEMENT.get(), 2), 400));
        recipes.add(recipe(List.of(ing(Items.SAND, 4)), new ItemStack(Items.SANDSTONE, 2), 200));
        RECIPES.put(WorkstationType.CEMENT_MIXER, recipes);
    }

    private static void registerWorkbenchRecipes() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        recipes.add(recipe(List.of(ing(Items.OAK_PLANKS, 4), ing(ModItems.NAIL.get(), 2)), new ItemStack(Items.OAK_DOOR, 1), 0));
        recipes.add(recipe(List.of(ing(Items.STICK, 3), ing(ModItems.FORGED_IRON.get(), 2)), new ItemStack(Items.IRON_PICKAXE, 1), 0));
        recipes.add(recipe(List.of(ing(Items.STICK, 2), ing(ModItems.FORGED_IRON.get(), 3)), new ItemStack(Items.IRON_AXE, 1), 0));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_IRON.get(), 1), ing(Items.STICK, 1)), new ItemStack(Items.IRON_SHOVEL, 1), 0));
        recipes.add(recipe(List.of(ing(ModItems.MECHANICAL_PARTS.get(), 2), ing(ModItems.FORGED_IRON.get(), 2), ing(ModItems.DUCT_TAPE.get(), 1)), new ItemStack(Items.SHIELD, 1), 0));
        recipes.add(recipe(List.of(ing(Items.COBBLESTONE, 3), ing(Items.STICK, 2)), new ItemStack(ModItems.STONE_CLUB.get(), 1), 0));
        recipes.add(recipe(List.of(ing(Items.OAK_PLANKS, 3), ing(ModItems.NAIL.get(), 2), ing(ModItems.DUCT_TAPE.get(), 1)), new ItemStack(ModItems.BASEBALL_BAT.get(), 1), 0));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_IRON.get(), 5), ing(Items.STICK, 2)), new ItemStack(ModItems.IRON_SLEDGEHAMMER.get(), 1), 0));
        RECIPES.put(WorkstationType.WORKBENCH, recipes);
    }

    private static void registerChemistryStationRecipes() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        recipes.add(recipe(List.of(ing(ModItems.OIL_SHALE.get(), 2)), new ItemStack(ModItems.POLYMER.get(), 1), 250));
        recipes.add(recipe(List.of(ing(ModItems.NITRATE.get(), 2), ing(Items.COAL, 1)), new ItemStack(Items.GUNPOWDER, 2), 200));
        recipes.add(recipe(List.of(ing(ModItems.OIL_SHALE.get(), 3)), new ItemStack(ModItems.GAS_CAN.get(), 1), 400));
        recipes.add(recipe(List.of(ing(ModItems.NITRATE.get(), 1), ing(ModItems.GLASS_JAR.get(), 1)), new ItemStack(ModItems.ACID.get(), 1), 300));
        recipes.add(recipe(List.of(ing(ModItems.NITRATE.get(), 3), ing(ModItems.GLASS_JAR.get(), 1), ing(ModItems.POLYMER.get(), 1)), new ItemStack(ModItems.ANTIBIOTICS.get(), 1), 400));
        recipes.add(recipe(List.of(ing(Items.GUNPOWDER, 1), ing(ModItems.FORGED_LEAD.get(), 1)), new ItemStack(ModItems.AMMO_9MM.get(), 8), 150));
        recipes.add(recipe(List.of(ing(Items.GUNPOWDER, 2), ing(ModItems.FORGED_LEAD.get(), 1), ing(ModItems.FORGED_IRON.get(), 1)), new ItemStack(ModItems.AMMO_762.get(), 8), 200));
        RECIPES.put(WorkstationType.CHEMISTRY_STATION, recipes);
    }

    private static void registerAdvancedWorkbenchRecipes() {
        List<WorkstationRecipe> recipes = new ArrayList<>();
        recipes.add(recipe(List.of(ing(ModItems.FORGED_IRON.get(), 5), ing(ModItems.MECHANICAL_PARTS.get(), 3)), new ItemStack(ModItems.FORGED_STEEL.get(), 2), 0));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_STEEL.get(), 2), ing(ModItems.SPRING.get(), 1), ing(ModItems.MECHANICAL_PARTS.get(), 1)), new ItemStack(Items.CROSSBOW, 1), 0));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_STEEL.get(), 3), ing(ModItems.MECHANICAL_PARTS.get(), 2), ing(ModItems.DUCT_TAPE.get(), 1)), new ItemStack(Items.IRON_CHESTPLATE, 1), 0));
        recipes.add(recipe(List.of(ing(ModItems.ELECTRICAL_PARTS.get(), 3), ing(ModItems.FORGED_IRON.get(), 2), ing(ModItems.POLYMER.get(), 1)), new ItemStack(Items.SPYGLASS, 1), 0));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_STEEL.get(), 1), ing(Items.STICK, 2)), new ItemStack(Items.IRON_SWORD, 1), 0));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_IRON.get(), 3), ing(ModItems.MECHANICAL_PARTS.get(), 2), ing(ModItems.SPRING.get(), 1)), new ItemStack(ModItems.PISTOL_9MM.get(), 1), 0));
        recipes.add(recipe(List.of(ing(ModItems.FORGED_STEEL.get(), 5), ing(ModItems.MECHANICAL_PARTS.get(), 3), ing(ModItems.SPRING.get(), 2)), new ItemStack(ModItems.AK47.get(), 1), 0));
        RECIPES.put(WorkstationType.ADVANCED_WORKBENCH, recipes);
    }

    public static List<WorkstationRecipe> getRecipes(WorkstationType type) {
        return RECIPES.getOrDefault(type, List.of());
    }

    public static WorkstationRecipe findMatch(WorkstationType type,
                                               java.util.function.Function<net.minecraft.world.item.Item, Integer> itemCounter) {
        for (WorkstationRecipe recipe : getRecipes(type)) {
            if (recipe.matches(itemCounter)) return recipe;
        }
        return null;
    }

    private static WorkstationRecipe.Ingredient ing(net.minecraft.world.item.Item item, int count) {
        return new WorkstationRecipe.Ingredient(item, count);
    }

    private static WorkstationRecipe recipe(List<WorkstationRecipe.Ingredient> inputs, ItemStack output, int ticks) {
        return new WorkstationRecipe(inputs, output, ticks);
    }
}
