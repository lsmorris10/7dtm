package com.sevendaystominecraft.advancement;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public final class RecipeUnlockManager {

    private static final String NS = "sevendaystominecraft:";

    private static final Map<String, Map<Integer, List<String>>> PERK_RECIPE_MAP = new LinkedHashMap<>();
    private static final Map<String, Map<Integer, List<String>>> MAGAZINE_ISSUE_RECIPE_MAP = new LinkedHashMap<>();
    private static final Map<String, List<String>> MAGAZINE_MASTERY_RECIPE_MAP = new LinkedHashMap<>();
    private static final Set<String> STARTER_RECIPES = new LinkedHashSet<>();

    static {
        registerStarterRecipes();
        registerCampfireCookRecipes();
        registerAdvancedEngineeringRecipes();
        registerPhysicianRecipes();
        registerGearheadRecipes();
        registerRoboticsInventorRecipes();
        registerPackMuleRecipes();
        registerMagazineRecipes();
    }

    private static String r(String path) {
        return NS + path;
    }

    private static void registerStarterRecipes() {
        STARTER_RECIPES.addAll(List.of(
                r("campfire/cook_beef"),
                r("campfire/cook_porkchop"),
                r("campfire/cook_chicken"),
                r("campfire/cook_mutton"),
                r("campfire/cook_cod"),
                r("campfire/cook_salmon"),
                r("campfire/bake_potato"),
                r("campfire/boil_water_bottle"),
                r("campfire/char_rabbit"),
                r("campfire/purify_murky_water"),
                r("campfire/corn_on_the_cob"),
                r("campfire/baked_potato_meal"),
                r("workbench/bandage"),
                r("workbench/splint"),
                r("workbench/stone_club"),
                r("forge/smelt_iron_scrap")
        ));
    }

    private static void registerCampfireCookRecipes() {
        Map<Integer, List<String>> map = new LinkedHashMap<>();
        map.put(1, List.of(
                r("campfire/goldenrod_tea"),
                r("campfire/red_tea"),
                r("campfire/coffee")
        ));
        map.put(2, List.of(
                r("grill/cook_beef"),
                r("grill/cook_porkchop"),
                r("grill/cook_chicken"),
                r("grill/cook_mutton"),
                r("grill/cook_cod"),
                r("grill/cook_salmon"),
                r("grill/bake_potato"),
                r("grill/cook_rabbit"),
                r("grill/corn_on_the_cob"),
                r("grill/baked_potato_meal"),
                r("grill/purify_murky_water"),
                r("grill/coffee")
        ));
        map.put(3, List.of(
                r("campfire/blueberry_pie"),
                r("campfire/vegetable_stew"),
                r("campfire/meat_stew"),
                r("grill/blueberry_pie"),
                r("grill/vegetable_stew"),
                r("grill/meat_stew")
        ));
        map.put(4, List.of(
                r("grill/hobo_stew"),
                r("grill/sham_chowder")
        ));
        PERK_RECIPE_MAP.put("campfire_cook", map);
    }

    private static void registerAdvancedEngineeringRecipes() {
        Map<Integer, List<String>> map = new LinkedHashMap<>();
        map.put(1, List.of(
                r("workbench/oak_door"),
                r("forge/forge_iron"),
                r("forge/forge_lead"),
                r("forge/make_nails")
        ));
        map.put(2, List.of(
                r("workbench/iron_pickaxe"),
                r("workbench/iron_axe"),
                r("workbench/iron_shovel"),
                r("workbench/baseball_bat"),
                r("forge/make_spring"),
                r("cement_mixer/concrete_mix"),
                r("cement_mixer/sandstone")
        ));
        map.put(3, List.of(
                r("workbench/iron_sledgehammer"),
                r("workbench/shield"),
                r("workbench/dew_collector"),
                r("cement_mixer/cement")
        ));
        map.put(4, List.of(
                r("workbench/generator_bank"),
                r("workbench/battery_bank"),
                r("workbench/solar_panel")
        ));
        map.put(5, List.of(
                r("advanced_workbench/forged_steel"),
                r("advanced_workbench/crossbow"),
                r("advanced_workbench/iron_chestplate"),
                r("advanced_workbench/spyglass"),
                r("advanced_workbench/iron_sword")
        ));
        PERK_RECIPE_MAP.put("advanced_engineering", map);
    }

    private static void registerPhysicianRecipes() {
        Map<Integer, List<String>> map = new LinkedHashMap<>();
        map.put(1, List.of(
                r("chemistry_station/painkiller"),
                r("chemistry_station/aloe_cream")
        ));
        map.put(2, List.of(
                r("chemistry_station/polymer"),
                r("chemistry_station/gunpowder"),
                r("chemistry_station/ammo_9mm")
        ));
        map.put(3, List.of(
                r("chemistry_station/antibiotics"),
                r("chemistry_station/acid"),
                r("chemistry_station/first_aid_kit"),
                r("chemistry_station/ammo_762")
        ));
        map.put(4, List.of(
                r("chemistry_station/gas_can"),
                r("chemistry_station/fertilizer")
        ));
        PERK_RECIPE_MAP.put("physician", map);
    }

    private static void registerGearheadRecipes() {
        Map<Integer, List<String>> map = new LinkedHashMap<>();
        map.put(3, List.of(
                r("advanced_workbench/pistol_9mm")
        ));
        map.put(5, List.of(
                r("advanced_workbench/ak47")
        ));
        PERK_RECIPE_MAP.put("gearhead", map);
    }

    private static void registerRoboticsInventorRecipes() {
        Map<Integer, List<String>> map = new LinkedHashMap<>();
        map.put(1, List.of(
                r("workbench/generator_bank")
        ));
        PERK_RECIPE_MAP.put("robotics_inventor", map);
    }

    private static void registerPackMuleRecipes() {
        Map<Integer, List<String>> map = new LinkedHashMap<>();
        map.put(3, List.of(
                r("workbench/coin_bag")
        ));
        PERK_RECIPE_MAP.put("pack_mule", map);
    }

    private static void registerMagazineRecipes() {
        Map<Integer, List<String>> steadySteve = new LinkedHashMap<>();
        steadySteve.put(1, List.of(r("chemistry_station/ammo_9mm")));
        steadySteve.put(4, List.of(r("advanced_workbench/pistol_9mm")));
        MAGAZINE_ISSUE_RECIPE_MAP.put("steady_steve", steadySteve);

        Map<Integer, List<String>> blockBrawler = new LinkedHashMap<>();
        blockBrawler.put(1, List.of(r("workbench/stone_club")));
        blockBrawler.put(3, List.of(r("workbench/baseball_bat")));
        blockBrawler.put(5, List.of(r("workbench/iron_sledgehammer")));
        MAGAZINE_ISSUE_RECIPE_MAP.put("block_brawler", blockBrawler);

        Map<Integer, List<String>> sharpshotSam = new LinkedHashMap<>();
        sharpshotSam.put(1, List.of(r("chemistry_station/ammo_762")));
        sharpshotSam.put(4, List.of(r("advanced_workbench/crossbow")));
        MAGAZINE_ISSUE_RECIPE_MAP.put("sharpshot_sam", sharpshotSam);

        Map<Integer, List<String>> tinkerer = new LinkedHashMap<>();
        tinkerer.put(1, List.of(r("workbench/bandage"), r("workbench/splint")));
        tinkerer.put(3, List.of(r("workbench/shield")));
        MAGAZINE_ISSUE_RECIPE_MAP.put("the_tinkerer", tinkerer);

        Map<Integer, List<String>> overworldChef = new LinkedHashMap<>();
        overworldChef.put(1, List.of(r("campfire/goldenrod_tea"), r("campfire/red_tea")));
        overworldChef.put(3, List.of(r("campfire/blueberry_pie"), r("campfire/vegetable_stew")));
        MAGAZINE_ISSUE_RECIPE_MAP.put("overworld_chef", overworldChef);

        Map<Integer, List<String>> dungeonTactician = new LinkedHashMap<>();
        dungeonTactician.put(1, List.of(r("chemistry_station/gunpowder")));
        dungeonTactician.put(4, List.of(r("advanced_workbench/ak47")));
        MAGAZINE_ISSUE_RECIPE_MAP.put("dungeon_tactician", dungeonTactician);

        MAGAZINE_MASTERY_RECIPE_MAP.put("steady_steve", List.of(r("advanced_workbench/pistol_9mm")));
        MAGAZINE_MASTERY_RECIPE_MAP.put("block_brawler", List.of(r("workbench/iron_sledgehammer")));
        MAGAZINE_MASTERY_RECIPE_MAP.put("sharpshot_sam", List.of(r("advanced_workbench/ak47")));
        MAGAZINE_MASTERY_RECIPE_MAP.put("the_tinkerer", List.of(r("workbench/dew_collector")));
        MAGAZINE_MASTERY_RECIPE_MAP.put("overworld_chef", List.of(r("grill/hobo_stew"), r("grill/sham_chowder")));
        MAGAZINE_MASTERY_RECIPE_MAP.put("dungeon_tactician", List.of(r("advanced_workbench/crossbow")));
    }

    public static void grantStarterRecipes(ServerPlayer player) {
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        int count = 0;
        for (String recipeId : STARTER_RECIPES) {
            if (stats.unlockRecipe(recipeId)) {
                count++;
            }
        }
        if (count > 0) {
            SevenDaysToMinecraft.LOGGER.debug("[BZHS] Granted {} starter recipes to {}", count, player.getName().getString());
        }
    }

    public static void onPerkRankGained(ServerPlayer player, String perkId, int newRank) {
        Map<Integer, List<String>> rankMap = PERK_RECIPE_MAP.get(perkId);
        if (rankMap == null) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        List<String> unlocked = new ArrayList<>();

        for (Map.Entry<Integer, List<String>> entry : rankMap.entrySet()) {
            if (newRank >= entry.getKey()) {
                for (String recipeId : entry.getValue()) {
                    if (stats.unlockRecipe(recipeId)) {
                        unlocked.add(recipeId);
                    }
                }
            }
        }

        if (!unlocked.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("§a[BZHS] §fNew recipes unlocked! §7(" + unlocked.size() + " recipes)"),
                    false);
            SevenDaysToMinecraft.LOGGER.debug("[BZHS] Perk {} rank {} unlocked {} recipes for {}",
                    perkId, newRank, unlocked.size(), player.getName().getString());
        }

        ModTriggers.PERK_RANK.get().trigger(player, perkId, newRank);
    }

    public static void onMagazineRead(ServerPlayer player, String seriesId, int issue, boolean mastery) {
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        List<String> unlocked = new ArrayList<>();

        Map<Integer, List<String>> issueMap = MAGAZINE_ISSUE_RECIPE_MAP.get(seriesId);
        if (issueMap != null) {
            for (Map.Entry<Integer, List<String>> entry : issueMap.entrySet()) {
                if (issue >= entry.getKey()) {
                    for (String recipeId : entry.getValue()) {
                        if (stats.unlockRecipe(recipeId)) {
                            unlocked.add(recipeId);
                        }
                    }
                }
            }
        }

        if (mastery) {
            List<String> masteryRecipes = MAGAZINE_MASTERY_RECIPE_MAP.get(seriesId);
            if (masteryRecipes != null) {
                for (String recipeId : masteryRecipes) {
                    if (stats.unlockRecipe(recipeId)) {
                        unlocked.add(recipeId);
                    }
                }
            }
        }

        if (!unlocked.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("§a[BZHS] §fNew recipes unlocked! §7(" + unlocked.size() + " recipes)"),
                    false);
            SevenDaysToMinecraft.LOGGER.debug("[BZHS] Magazine {} issue {} unlocked {} recipes for {}",
                    seriesId, issue, unlocked.size(), player.getName().getString());
        }

        ModTriggers.MAGAZINE_READ.get().trigger(player, seriesId, issue, mastery);
    }

    public static boolean isRecipeUnlocked(ServerPlayer player, String recipeId) {
        if (STARTER_RECIPES.contains(recipeId)) return true;
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        return stats.isRecipeUnlocked(recipeId);
    }

    public static boolean isRecipeUnlocked(SevenDaysPlayerStats stats, String recipeId) {
        if (STARTER_RECIPES.contains(recipeId)) return true;
        return stats.isRecipeUnlocked(recipeId);
    }

    public static boolean isStarterRecipe(String recipeId) {
        return STARTER_RECIPES.contains(recipeId);
    }

    public static Set<String> getStarterRecipeIds() {
        return Collections.unmodifiableSet(STARTER_RECIPES);
    }

    public static Map<String, Map<Integer, List<String>>> getPerkRecipeMap() {
        return Collections.unmodifiableMap(PERK_RECIPE_MAP);
    }
}
