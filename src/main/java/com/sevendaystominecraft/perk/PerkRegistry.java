package com.sevendaystominecraft.perk;

import java.util.*;

public class PerkRegistry {

    private static final Map<String, PerkDefinition> PERKS = new LinkedHashMap<>();

    static {
        registerStrengthPerks();
        registerPerceptionPerks();
        registerFortitudePerks();
        registerAgilityPerks();
        registerIntellectPerks();
        registerMasteries();
    }

    private static void registerStrengthPerks() {
        register(new PerkDefinition("brawler", "Brawler", Attribute.STRENGTH,
                5, new int[]{1, 2, 3, 5, 7},
                "+10% fist/knuckle damage per rank", false));
        register(new PerkDefinition("iron_fists", "Iron Fists", Attribute.STRENGTH,
                5, new int[]{1, 2, 4, 6, 8},
                "+10% club damage per rank, rank 5: 25% knockdown chance", false));
        register(new PerkDefinition("skull_crusher", "Skull Crusher", Attribute.STRENGTH,
                5, new int[]{1, 3, 5, 7, 9},
                "+10% sledgehammer damage, +1 block damage per rank", false));
        register(new PerkDefinition("unstoppable_force", "Unstoppable Force", Attribute.STRENGTH,
                4, new int[]{2, 4, 6, 8},
                "-15% stamina cost on power attacks per rank", false));
        register(new PerkDefinition("campfire_cook", "Campfire Cook", Attribute.STRENGTH,
                4, new int[]{1, 3, 5, 7},
                "Unlock cooking recipes per rank", false));
        register(new PerkDefinition("pack_mule", "Pack Mule", Attribute.STRENGTH,
                4, new int[]{1, 3, 5, 7},
                "+10 slots carry capacity per rank", false));
        register(new PerkDefinition("deep_striker", "Deep Striker", Attribute.STRENGTH,
                5, new int[]{1, 2, 4, 6, 8},
                "+15% mining speed, +1 block damage per rank", false));
        register(new PerkDefinition("deep_veins", "Deep Veins", Attribute.STRENGTH,
                3, new int[]{3, 5, 7},
                "+20% ore yield per rank", false));
    }

    private static void registerPerceptionPerks() {
        register(new PerkDefinition("archery", "Archery", Attribute.PERCEPTION,
                5, new int[]{1, 2, 3, 5, 7},
                "+10% bow/crossbow damage per rank", false));
        register(new PerkDefinition("gunslinger", "Gunslinger", Attribute.PERCEPTION,
                5, new int[]{1, 2, 4, 6, 8},
                "+10% pistol damage, +5% reload speed per rank", false));
        register(new PerkDefinition("rifle_guy", "Rifle Guy", Attribute.PERCEPTION,
                5, new int[]{2, 3, 5, 7, 9},
                "+10% rifle/sniper/machine gun damage per rank", false));
        register(new PerkDefinition("boomstick", "Boomstick", Attribute.PERCEPTION,
                5, new int[]{1, 2, 4, 6, 8},
                "+10% shotgun damage, +5% reload speed per rank", false));
        register(new PerkDefinition("demolitions_expert", "Demolitions Expert", Attribute.PERCEPTION,
                5, new int[]{1, 3, 5, 7, 9},
                "+20% explosive damage, +1 blast radius at rank 5", false));
        register(new PerkDefinition("lock_picking", "Lock Picking", Attribute.PERCEPTION,
                3, new int[]{3, 5, 7},
                "Pick locks faster, higher tier locks per rank", false));
        register(new PerkDefinition("keen_scavenger", "Keen Scavenger", Attribute.PERCEPTION,
                5, new int[]{1, 2, 4, 6, 8},
                "+10% loot quality bonus per rank", false));
        register(new PerkDefinition("treasure_hunter", "Treasure Hunter", Attribute.PERCEPTION,
                3, new int[]{3, 5, 7},
                "Buried supply quests reward bonus loot", false));
        register(new PerkDefinition("spear_master", "Spear Master", Attribute.PERCEPTION,
                5, new int[]{1, 2, 3, 5, 7},
                "+10% spear damage, +range at rank 5", false));
    }

    private static void registerFortitudePerks() {
        register(new PerkDefinition("healing_factor", "Healing Factor", Attribute.FORTITUDE,
                5, new int[]{1, 2, 3, 5, 7},
                "+20% natural health regen per rank", false));
        register(new PerkDefinition("iron_gut", "Iron Gut", Attribute.FORTITUDE,
                3, new int[]{3, 5, 7},
                "Reduce food poisoning chance by 33% per rank", false));
        register(new PerkDefinition("rule1_cardio", "Rule 1: Cardio", Attribute.FORTITUDE,
                3, new int[]{1, 3, 5},
                "+10% stamina regen, +5% sprint speed per rank", false));
        register(new PerkDefinition("green_thumb", "Green Thumb", Attribute.FORTITUDE,
                3, new int[]{1, 3, 5},
                "+1 crop yield per rank from farming", false));
        register(new PerkDefinition("pain_tolerance", "Pain Tolerance", Attribute.FORTITUDE,
                5, new int[]{1, 2, 4, 6, 8},
                "-10% damage taken per rank", false));
        register(new PerkDefinition("heavy_armor", "Heavy Armor", Attribute.FORTITUDE,
                5, new int[]{1, 2, 4, 6, 8},
                "-15% heavy armor movement penalty per rank", false));
        register(new PerkDefinition("medium_armor", "Medium Armor", Attribute.FORTITUDE,
                5, new int[]{1, 2, 4, 6, 8},
                "+5% medium armor stamina regen bonus per rank", false));
        register(new PerkDefinition("well_insulated", "Well Insulated", Attribute.FORTITUDE,
                3, new int[]{3, 5, 7},
                "+-10F comfort zone expansion per rank", false));
        register(new PerkDefinition("field_medic", "Field Medic", Attribute.FORTITUDE,
                4, new int[]{2, 4, 6, 8},
                "Medical items +25% effectiveness per rank", false));
    }

    private static void registerAgilityPerks() {
        register(new PerkDefinition("light_armor", "Light Armor", Attribute.AGILITY,
                5, new int[]{1, 2, 4, 6, 8},
                "+10% light armor effectiveness per rank", false));
        register(new PerkDefinition("parkour", "Parkour", Attribute.AGILITY,
                4, new int[]{1, 3, 5, 7},
                "-25% fall damage, +0.5 jump height at rank 4", false));
        register(new PerkDefinition("shadow_strike", "Shadow Strike", Attribute.AGILITY,
                5, new int[]{1, 2, 4, 6, 8},
                "+20% sneak attack multiplier per rank", false));
        register(new PerkDefinition("nightstalker", "Nightstalker", Attribute.AGILITY,
                3, new int[]{3, 5, 7},
                "Reduce detection range by 20% per rank", false));
        register(new PerkDefinition("deep_cuts", "Deep Cuts", Attribute.AGILITY,
                5, new int[]{1, 2, 3, 5, 7},
                "+10% knife/blade damage per rank", false));
        register(new PerkDefinition("run_and_gun", "Run and Gun", Attribute.AGILITY,
                3, new int[]{3, 5, 7},
                "-20% accuracy penalty while moving per rank", false));
        register(new PerkDefinition("flurry_of_blows", "Flurry of Blows", Attribute.AGILITY,
                5, new int[]{1, 2, 4, 6, 8},
                "+8% attack speed per rank", false));
        register(new PerkDefinition("gunslinger_agility", "Gunslinger (Agility)", Attribute.AGILITY,
                3, new int[]{3, 5, 7},
                "Dual-wield pistol accuracy bonus per rank", false));
    }

    private static void registerIntellectPerks() {
        register(new PerkDefinition("advanced_engineering", "Advanced Engineering", Attribute.INTELLECT,
                5, new int[]{1, 2, 4, 6, 8},
                "Unlock workstation tiers + craft quality bonus per rank", false));
        register(new PerkDefinition("gearhead", "Gearhead", Attribute.INTELLECT,
                5, new int[]{1, 3, 5, 7, 9},
                "Unlock vehicle tiers per rank", false));
        register(new PerkDefinition("better_barter", "Better Barter", Attribute.INTELLECT,
                5, new int[]{1, 2, 3, 5, 7},
                "-5% trader prices per rank, secret stock at rank 5", false));
        register(new PerkDefinition("bold_explorer", "Bold Explorer", Attribute.INTELLECT,
                4, new int[]{1, 3, 5, 7},
                "Better quest rewards, +1 quest choice at rank 4", false));
        register(new PerkDefinition("physician", "Physician", Attribute.INTELLECT,
                4, new int[]{2, 4, 6, 8},
                "Craft advanced medical items per rank", false));
        register(new PerkDefinition("electrocutioner", "Electrocutioner", Attribute.INTELLECT,
                5, new int[]{1, 3, 5, 7, 9},
                "+15% stun baton damage, +20% turret damage per rank", false));
        register(new PerkDefinition("robotics_inventor", "Robotics Inventor", Attribute.INTELLECT,
                5, new int[]{3, 5, 7, 8, 10},
                "Junk turret/auto turret unlock + damage per rank", false));
        register(new PerkDefinition("charismatic_nature", "Charismatic Nature", Attribute.INTELLECT,
                3, new int[]{1, 3, 5},
                "+10% XP share range in multiplayer per rank", false));
    }

    private static void registerMasteries() {
        register(new PerkDefinition("titan", "Titan", Attribute.STRENGTH,
                1, new int[]{10},
                "All melee attacks have 15% chance to stagger any zombie", true));
        register(new PerkDefinition("eagle_eye", "Eagle Eye", Attribute.PERCEPTION,
                1, new int[]{10},
                "Headshots deal x4.0 damage (up from x2.5)", true));
        register(new PerkDefinition("unkillable", "Unkillable", Attribute.FORTITUDE,
                1, new int[]{10},
                "Upon fatal damage, survive with 1 HP + 10 sec invulnerability (60 min cooldown)", true));
        register(new PerkDefinition("ghost", "Ghost", Attribute.AGILITY,
                1, new int[]{10},
                "Stealth kills are completely silent (0 heatmap noise) + x5.0 sneak damage", true));
        register(new PerkDefinition("mastermind", "Mastermind", Attribute.INTELLECT,
                1, new int[]{10},
                "All crafted items have 20% chance to roll +1 quality tier; turrets gain auto-repair", true));
    }

    private static void register(PerkDefinition perk) {
        PERKS.put(perk.getId(), perk);
    }

    public static PerkDefinition get(String id) {
        return PERKS.get(id);
    }

    public static Collection<PerkDefinition> getAll() {
        return Collections.unmodifiableCollection(PERKS.values());
    }

    public static List<PerkDefinition> getByAttribute(Attribute attribute) {
        List<PerkDefinition> result = new ArrayList<>();
        for (PerkDefinition perk : PERKS.values()) {
            if (perk.getAttribute() == attribute) {
                result.add(perk);
            }
        }
        return result;
    }

    public static int count() {
        return PERKS.size();
    }
}
