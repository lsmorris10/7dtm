package com.sevendaystominecraft.quest;

import java.util.*;

/**
 * Static registry that defines all progression tree nodes and their connections.
 * Grid layout: X = stage column (0-4), Y = row within stage (0-6, by category).
 */
public class ProgressionTreeRegistry {

    private static final List<ProgressionNode> ALL_NODES = new ArrayList<>();
    private static final Map<String, ProgressionNode> BY_ID = new LinkedHashMap<>();
    private static final Map<ProgressionStage, List<ProgressionNode>> BY_STAGE = new LinkedHashMap<>();

    static {
        buildTree();
    }

    private static void buildTree() {
        // ===================================================================
        // Stage 1: SCAVENGER — "Learn to survive"
        // ===================================================================
        add(new ProgressionNode("s1_gather_wood", ProgressionStage.SCAVENGER, NodeCategory.SCAVENGE,
                "Gather Wood", "Collect 16 Wood Planks", "minecraft:oak_planks",
                50, 0, List.of(), 0, 0));

        add(new ProgressionNode("s1_gather_stone", ProgressionStage.SCAVENGER, NodeCategory.SCAVENGE,
                "Gather Stone", "Collect 8 Cobblestone", "minecraft:cobblestone",
                50, 0, List.of(), 0, 1));

        add(new ProgressionNode("s1_get_water", ProgressionStage.SCAVENGER, NodeCategory.WATER,
                "Find Water", "Collect Murky Water", "sevendaystominecraft:murky_water",
                75, 0, List.of(), 0, 2));

        add(new ProgressionNode("s1_hunt_meat", ProgressionStage.SCAVENGER, NodeCategory.FOOD,
                "Hunt for Meat", "Obtain Raw Meat", "minecraft:beef",
                75, 0, List.of(), 0, 3));

        add(new ProgressionNode("s1_stone_tools", ProgressionStage.SCAVENGER, NodeCategory.TOOL,
                "Stone Tools", "Craft a Stone Pickaxe", "minecraft:stone_pickaxe",
                100, 10, List.of("s1_gather_wood", "s1_gather_stone"), 0, 4));

        add(new ProgressionNode("s1_stone_club", ProgressionStage.SCAVENGER, NodeCategory.WEAPON,
                "Stone Club", "Craft a Stone Club", "sevendaystominecraft:stone_club",
                100, 10, List.of("s1_gather_wood", "s1_gather_stone"), 0, 5));

        // ===================================================================
        // Stage 2: SURVIVOR — "Establish your foothold"
        // ===================================================================
        add(new ProgressionNode("s2_campfire", ProgressionStage.SURVIVOR, NodeCategory.TOOL,
                "Build Campfire", "Place a Campfire", "minecraft:campfire",
                100, 0, List.of("s1_gather_wood"), 1, 0));

        add(new ProgressionNode("s2_boil_water", ProgressionStage.SURVIVOR, NodeCategory.WATER,
                "Boil Water", "Craft Boiled Water", "sevendaystominecraft:boiled_water",
                100, 15, List.of("s2_campfire", "s1_get_water"), 1, 1));

        add(new ProgressionNode("s2_cook_meat", ProgressionStage.SURVIVOR, NodeCategory.FOOD,
                "Cook Meat", "Cook any Raw Meat", "minecraft:cooked_beef",
                100, 0, List.of("s2_campfire", "s1_hunt_meat"), 1, 2));

        add(new ProgressionNode("s2_bandage", ProgressionStage.SURVIVOR, NodeCategory.MEDICAL,
                "Craft Bandage", "Craft a Bandage", "sevendaystominecraft:bandage",
                125, 15, List.of("s1_gather_wood"), 1, 3));

        add(new ProgressionNode("s2_wooden_shelter", ProgressionStage.SURVIVOR, NodeCategory.SHELTER,
                "Build Shelter", "Place 32 Planks", "minecraft:oak_planks",
                150, 20, List.of("s1_gather_wood"), 1, 4));

        add(new ProgressionNode("s2_baseball_bat", ProgressionStage.SURVIVOR, NodeCategory.WEAPON,
                "Baseball Bat", "Craft a Baseball Bat", "sevendaystominecraft:baseball_bat",
                150, 20, List.of("s1_stone_club"), 1, 5));

        // ===================================================================
        // Stage 3: ESTABLISHED — "The iron age begins"
        // ===================================================================
        add(new ProgressionNode("s3_workbench", ProgressionStage.ESTABLISHED, NodeCategory.TOOL,
                "Crafting Table", "Place a Crafting Table", "minecraft:crafting_table",
                200, 25, List.of("s1_stone_tools"), 2, 0));

        add(new ProgressionNode("s3_forge", ProgressionStage.ESTABLISHED, NodeCategory.TOOL,
                "Blast Furnace", "Place a Blast Furnace", "minecraft:blast_furnace",
                200, 25, List.of("s3_workbench"), 2, 1));

        add(new ProgressionNode("s3_forged_iron", ProgressionStage.ESTABLISHED, NodeCategory.SCAVENGE,
                "Forged Iron", "Smelt Forged Iron in a Blast Furnace", "sevendaystominecraft:forged_iron",
                200, 0, List.of("s3_forge"), 2, 2));

        add(new ProgressionNode("s3_iron_tools", ProgressionStage.ESTABLISHED, NodeCategory.TOOL,
                "Iron Tools", "Craft an Iron Pickaxe", "minecraft:iron_pickaxe",
                200, 25, List.of("s3_forged_iron"), 2, 3));

        add(new ProgressionNode("s3_scrap_armor", ProgressionStage.ESTABLISHED, NodeCategory.SHELTER,
                "Scrap Armor", "Craft any Scrap Iron Armor piece", "sevendaystominecraft:scrap_iron_chestplate",
                250, 30, List.of("s3_forged_iron"), 2, 4));

        add(new ProgressionNode("s3_pistol", ProgressionStage.ESTABLISHED, NodeCategory.WEAPON,
                "9mm Pistol", "Obtain a 9mm Pistol", "sevendaystominecraft:pistol_9mm",
                300, 50, List.of("s3_workbench"), 2, 5));

        add(new ProgressionNode("s3_goldenrod_tea", ProgressionStage.ESTABLISHED, NodeCategory.MEDICAL,
                "Goldenrod Tea", "Brew Goldenrod Tea", "sevendaystominecraft:goldenrod_tea",
                150, 20, List.of("s2_boil_water"), 2, 6));

        // ===================================================================
        // Stage 4: FORTIFIED — "Fortify and arm up"
        // ===================================================================
        add(new ProgressionNode("s4_adv_workbench", ProgressionStage.FORTIFIED, NodeCategory.TOOL,
                "Forged Steel", "Craft Forged Steel", "sevendaystominecraft:forged_steel",
                300, 40, List.of("s3_forged_iron"), 3, 0));

        add(new ProgressionNode("s4_forged_steel", ProgressionStage.FORTIFIED, NodeCategory.SCAVENGE,
                "Forged Steel", "Craft Forged Steel", "sevendaystominecraft:forged_steel",
                300, 40, List.of("s3_forge", "s3_forged_iron"), 3, 1));

        add(new ProgressionNode("s4_military_armor", ProgressionStage.FORTIFIED, NodeCategory.SHELTER,
                "Military Armor", "Craft any Military Armor piece", "sevendaystominecraft:military_chestplate",
                400, 60, List.of("s4_forged_steel", "s4_adv_workbench"), 3, 2));

        add(new ProgressionNode("s4_ak47", ProgressionStage.FORTIFIED, NodeCategory.WEAPON,
                "AK-47", "Obtain an AK-47", "sevendaystominecraft:ak47",
                400, 75, List.of("s3_pistol"), 3, 3));

        add(new ProgressionNode("s4_shotgun", ProgressionStage.FORTIFIED, NodeCategory.WEAPON,
                "Shotgun", "Obtain a Shotgun", "sevendaystominecraft:shotgun",
                400, 75, List.of("s3_pistol"), 3, 4));

        add(new ProgressionNode("s4_farming", ProgressionStage.FORTIFIED, NodeCategory.FOOD,
                "Start Farming", "Plant and Harvest a Crop", "sevendaystominecraft:corn",
                250, 30, List.of("s2_cook_meat"), 3, 5));

        add(new ProgressionNode("s4_first_aid", ProgressionStage.FORTIFIED, NodeCategory.MEDICAL,
                "First Aid Kit", "Craft a First Aid Kit", "sevendaystominecraft:first_aid_kit",
                300, 40, List.of("s2_bandage", "s3_goldenrod_tea"), 3, 6));

        // ===================================================================
        // Stage 5: SELF-SUFFICIENT — "Master of the wasteland"
        // ===================================================================
        add(new ProgressionNode("s5_chem_station", ProgressionStage.SELF_SUFFICIENT, NodeCategory.TOOL,
                "Gunpowder", "Craft Gunpowder from Nitrate", "minecraft:gunpowder",
                400, 50, List.of("s4_adv_workbench"), 4, 0));

        add(new ProgressionNode("s5_cement_mixer", ProgressionStage.SELF_SUFFICIENT, NodeCategory.TOOL,
                "Concrete", "Craft Concrete Mix", "sevendaystominecraft:concrete_mix",
                400, 50, List.of("s3_forge"), 4, 1));

        add(new ProgressionNode("s5_antibiotics", ProgressionStage.SELF_SUFFICIENT, NodeCategory.MEDICAL,
                "Antibiotics", "Craft Antibiotics", "sevendaystominecraft:antibiotics",
                400, 50, List.of("s5_chem_station", "s4_first_aid"), 4, 2));

        add(new ProgressionNode("s5_clear_territory", ProgressionStage.SELF_SUFFICIENT, NodeCategory.SCAVENGE,
                "Clear Territory", "Clear any Territory", "minecraft:iron_sword",
                600, 150, List.of("s4_ak47", "s4_military_armor"), 4, 6));

        // Build stage map
        for (ProgressionStage stage : ProgressionStage.values()) {
            BY_STAGE.put(stage, new ArrayList<>());
        }
        for (ProgressionNode node : ALL_NODES) {
            BY_STAGE.get(node.getStage()).add(node);
        }
    }

    private static void add(ProgressionNode node) {
        ALL_NODES.add(node);
        BY_ID.put(node.getId(), node);
    }

    public static List<ProgressionNode> getAllNodes() {
        return Collections.unmodifiableList(ALL_NODES);
    }

    public static ProgressionNode getNode(String id) {
        return BY_ID.get(id);
    }

    public static List<ProgressionNode> getNodesForStage(ProgressionStage stage) {
        return Collections.unmodifiableList(BY_STAGE.getOrDefault(stage, Collections.emptyList()));
    }

    public static int getTotalNodeCount() {
        return ALL_NODES.size();
    }

    public static int getNodeCountForStage(ProgressionStage stage) {
        return BY_STAGE.getOrDefault(stage, Collections.emptyList()).size();
    }

    /**
     * Returns true if all prerequisites for a node are in the given completed set.
     */
    public static boolean arePrerequisitesMet(ProgressionNode node, Set<String> completedIds) {
        for (String prereq : node.getPrerequisiteIds()) {
            if (!completedIds.contains(prereq)) return false;
        }
        return true;
    }

    /**
     * Returns true if all nodes in the given stage are completed.
     */
    public static boolean isStageComplete(ProgressionStage stage, Set<String> completedIds) {
        for (ProgressionNode node : getNodesForStage(stage)) {
            if (!completedIds.contains(node.getId())) return false;
        }
        return true;
    }

    /**
     * Returns the highest completed stage number (0 if none fully complete).
     */
    public static int getHighestCompletedStage(Set<String> completedIds) {
        int highest = 0;
        for (ProgressionStage stage : ProgressionStage.values()) {
            if (isStageComplete(stage, completedIds)) {
                highest = stage.getNumber();
            }
        }
        return highest;
    }
}
