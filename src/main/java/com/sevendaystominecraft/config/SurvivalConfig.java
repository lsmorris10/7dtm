package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Server-side configuration for survival mechanics.
 *
 * Generates {@code survival.toml} in the server config directory.
 * All drain/regen rates are per-second unless noted otherwise.
 *
 * Spec reference: §1.1 (stat drain/regen), §1.2 (debuff triggers)
 */
public class SurvivalConfig {

    /** The config spec registered with NeoForge. */
    public static final ModConfigSpec SPEC;
    /** The parsed config values. */
    public static final SurvivalConfig INSTANCE;

    static {
        Pair<SurvivalConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(SurvivalConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    // ── Food & Water Drain ──────────────────────────────────────────────

    /** Passive food drain per minute at rest (spec §1.1: 0.2/min) */
    public final ModConfigSpec.DoubleValue foodDrainPerMinute;

    /** Passive water drain per minute at rest (spec §1.1: 0.3/min) */
    public final ModConfigSpec.DoubleValue waterDrainPerMinute;

    /** Water drain multiplier in desert/hot biomes (spec §1.1: ×1.5) */
    public final ModConfigSpec.DoubleValue waterDrainDesertMultiplier;

    /** Food drain multiplier while sprinting */
    public final ModConfigSpec.DoubleValue foodDrainActivityMultiplier;

    // ── Stamina ─────────────────────────────────────────────────────────

    /** Stamina drain per second while sprinting (spec §1.1: 10/s) */
    public final ModConfigSpec.DoubleValue staminaDrainSprint;

    /** Stamina cost per melee swing (spec §1.1: 8–25, this is base) */
    public final ModConfigSpec.DoubleValue staminaDrainMelee;

    /** Stamina cost per mining swing (spec §1.1: 5/swing) */
    public final ModConfigSpec.DoubleValue staminaDrainMining;

    /** Stamina cost per jump (spec §1.1: 8) */
    public final ModConfigSpec.DoubleValue staminaDrainJump;

    /** Stamina regen per second at rest (spec §1.1: 8/s) */
    public final ModConfigSpec.DoubleValue staminaRegenRest;

    /** Stamina regen per second while walking (spec §1.1: 4/s) */
    public final ModConfigSpec.DoubleValue staminaRegenWalking;

    // ── Health Regen ────────────────────────────────────────────────────

    /** Health regen per second when above thresholds (spec §1.1: 0.5/s) */
    public final ModConfigSpec.DoubleValue healthRegenRate;

    /** Food must be above this % for health regen (spec §1.1: 50%) */
    public final ModConfigSpec.DoubleValue healthRegenFoodThreshold;

    /** Water must be above this % for health regen (spec §1.1: 50%) */
    public final ModConfigSpec.DoubleValue healthRegenWaterThreshold;

    // ── Starvation / Dehydration Cascade (§1.1) ─────────────────────────

    /** Below this food %, stamina regen is halved (spec §1.1: 30%) */
    public final ModConfigSpec.DoubleValue cascadeThreshold1;

    /** Below this food %, health drains slowly (spec §1.1: 10%) */
    public final ModConfigSpec.DoubleValue cascadeThreshold2;

    /** Health drain per second when food/water < threshold2 (spec §1.1: 0.5/s) */
    public final ModConfigSpec.DoubleValue cascadeHealthDrainSlow;

    /** Health drain per second when food/water = 0 (spec §1.1: 2.0/s) */
    public final ModConfigSpec.DoubleValue cascadeHealthDrainFast;

    /** Movement speed penalty when food/water = 0 (spec §1.1: 40% = 0.4) */
    public final ModConfigSpec.DoubleValue cascadeSpeedPenalty;

    // ── Debuff Chances ──────────────────────────────────────────────────

    /** Bleeding chance on zombie melee hit (spec §1.2: 30%) */
    public final ModConfigSpec.DoubleValue bleedingChance;

    /** Base infection chance on zombie hit (spec §1.2: 10%, +5% per feral) */
    public final ModConfigSpec.DoubleValue infectionBaseChance;

    // ── Temperature ─────────────────────────────────────────────────────

    /** Rate of core temp adjustment toward ambient (°F/sec, spec §1.1: 0.3) */
    public final ModConfigSpec.DoubleValue tempAdjustRate;

    public final ModConfigSpec.DoubleValue hypothermiaThreshold;
    public final ModConfigSpec.DoubleValue hyperthermiaThreshold;
    public final ModConfigSpec.DoubleValue hypothermiaClearThreshold;
    public final ModConfigSpec.DoubleValue hyperthermiaClearThreshold;
    public final ModConfigSpec.IntValue temperatureExposureTicks;
    public final ModConfigSpec.DoubleValue rainTempModifier;
    public final ModConfigSpec.DoubleValue snowTempModifier;
    public final ModConfigSpec.DoubleValue waterTempModifier;
    public final ModConfigSpec.DoubleValue fireTempBonus;
    public final ModConfigSpec.DoubleValue undergroundNormTemp;

    // ── Sync ────────────────────────────────────────────────────────────

    // ── Food/Water Restoration ─────────────────────────────────────────
    public final ModConfigSpec.DoubleValue foodRestorationMultiplier;
    public final ModConfigSpec.DoubleValue waterPerDrink;

    // ── XP ───────────────────────────────────────────────────────────────
    public final ModConfigSpec.IntValue vanillaMobXP;

    // ── Player Damage ────────────────────────────────────────────────────
    public final ModConfigSpec.DoubleValue playerDamageMultiplier;

    /** How often to sync stats to client (in ticks; 10 = every 0.5s) */
    public final ModConfigSpec.IntValue syncIntervalTicks;

    // =====================================================================
    // Constructor — builds the config spec
    // =====================================================================

    SurvivalConfig(ModConfigSpec.Builder builder) {

        builder.comment("Brutal Zombie Horde Survival — Survival Mechanics Configuration")
               .push("survival");

        // Food & Water
        builder.push("food_water");
        foodDrainPerMinute = builder
                .comment("Passive food drain per minute at rest")
                .defineInRange("foodDrainPerMinute", 0.2, 0.0, 10.0);
        waterDrainPerMinute = builder
                .comment("Passive water drain per minute at rest")
                .defineInRange("waterDrainPerMinute", 0.3, 0.0, 10.0);
        waterDrainDesertMultiplier = builder
                .comment("Water drain multiplier in desert/hot biomes")
                .defineInRange("waterDrainDesertMultiplier", 1.5, 1.0, 5.0);
        foodDrainActivityMultiplier = builder
                .comment("Food drain multiplier while sprinting/mining")
                .defineInRange("foodDrainActivityMultiplier", 2.0, 1.0, 10.0);
        builder.pop();

        // Stamina
        builder.push("stamina");
        staminaDrainSprint = builder
                .comment("Stamina drain per second while sprinting")
                .defineInRange("staminaDrainSprint", 10.0, 0.0, 50.0);
        staminaDrainMelee = builder
                .comment("Stamina cost per melee swing (base)")
                .defineInRange("staminaDrainMelee", 12.0, 0.0, 50.0);
        staminaDrainMining = builder
                .comment("Stamina cost per mining swing")
                .defineInRange("staminaDrainMining", 5.0, 0.0, 50.0);
        staminaDrainJump = builder
                .comment("Stamina cost per jump")
                .defineInRange("staminaDrainJump", 8.0, 0.0, 50.0);
        staminaRegenRest = builder
                .comment("Stamina regen per second at rest")
                .defineInRange("staminaRegenRest", 8.0, 0.0, 50.0);
        staminaRegenWalking = builder
                .comment("Stamina regen per second while walking")
                .defineInRange("staminaRegenWalking", 4.0, 0.0, 50.0);
        builder.pop();

        // Health
        builder.push("health");
        healthRegenRate = builder
                .comment("Health regen per second when above food/water thresholds")
                .defineInRange("healthRegenRate", 0.1, 0.0, 10.0);
        healthRegenFoodThreshold = builder
                .comment("Food must be above this % of max for health regen")
                .defineInRange("healthRegenFoodThreshold", 50.0, 0.0, 100.0);
        healthRegenWaterThreshold = builder
                .comment("Water must be above this % of max for health regen")
                .defineInRange("healthRegenWaterThreshold", 50.0, 0.0, 100.0);
        builder.pop();

        // Starvation cascade
        builder.push("cascade");
        cascadeThreshold1 = builder
                .comment("Below this food/water %, stamina regen halved")
                .defineInRange("cascadeThreshold1", 30.0, 0.0, 100.0);
        cascadeThreshold2 = builder
                .comment("Below this food/water %, health drains")
                .defineInRange("cascadeThreshold2", 10.0, 0.0, 100.0);
        cascadeHealthDrainSlow = builder
                .comment("Health drain/sec when food/water < threshold2")
                .defineInRange("cascadeHealthDrainSlow", 0.1, 0.0, 10.0);
        cascadeHealthDrainFast = builder
                .comment("Health drain/sec when food/water = 0")
                .defineInRange("cascadeHealthDrainFast", 0.4, 0.0, 20.0);
        cascadeSpeedPenalty = builder
                .comment("Movement speed penalty when food/water = 0 (0.4 = 40%)")
                .defineInRange("cascadeSpeedPenalty", 0.4, 0.0, 1.0);
        builder.pop();

        // Debuffs
        builder.push("debuffs");
        bleedingChance = builder
                .comment("Bleeding chance on zombie melee hit (0.3 = 30%)")
                .defineInRange("bleedingChance", 0.3, 0.0, 1.0);
        infectionBaseChance = builder
                .comment("Base infection chance on zombie hit (0.1 = 10%)")
                .defineInRange("infectionBaseChance", 0.1, 0.0, 1.0);
        builder.pop();

        // Temperature
        builder.push("temperature");
        tempAdjustRate = builder
                .comment("Core temp adjustment rate toward ambient (°F/sec)")
                .defineInRange("tempAdjustRate", 0.3, 0.01, 10.0);
        hypothermiaThreshold = builder
                .comment("Core temp below which cold exposure timer starts (°F)")
                .defineInRange("hypothermiaThreshold", 32.0, -50.0, 100.0);
        hyperthermiaThreshold = builder
                .comment("Core temp above which heat exposure timer starts (°F)")
                .defineInRange("hyperthermiaThreshold", 110.0, 80.0, 200.0);
        hypothermiaClearThreshold = builder
                .comment("Core temp above which hypothermia debuff clears (°F)")
                .defineInRange("hypothermiaClearThreshold", 50.0, -50.0, 120.0);
        hyperthermiaClearThreshold = builder
                .comment("Core temp below which hyperthermia debuff clears (°F)")
                .defineInRange("hyperthermiaClearThreshold", 100.0, 60.0, 200.0);
        temperatureExposureTicks = builder
                .comment("Ticks of exposure before temperature debuff triggers (2400 = 120 sec)")
                .defineInRange("temperatureExposureTicks", 2400, 20, 72000);
        rainTempModifier = builder
                .comment("Temperature modifier when raining at player position (°F)")
                .defineInRange("rainTempModifier", -15.0, -50.0, 0.0);
        snowTempModifier = builder
                .comment("Temperature modifier when snowing at player position (°F)")
                .defineInRange("snowTempModifier", -20.0, -50.0, 0.0);
        waterTempModifier = builder
                .comment("Temperature modifier when player is in water (°F)")
                .defineInRange("waterTempModifier", -25.0, -50.0, 0.0);
        fireTempBonus = builder
                .comment("Temperature bonus when near fire/campfire/furnace within 5 blocks (°F)")
                .defineInRange("fireTempBonus", 15.0, 0.0, 50.0);
        undergroundNormTemp = builder
                .comment("Temperature that underground areas (below Y=40) trend toward (°F)")
                .defineInRange("undergroundNormTemp", 55.0, 0.0, 120.0);
        builder.pop();

        // Food/Water Restoration
        builder.push("restoration");
        foodRestorationMultiplier = builder
                .comment("Multiplier applied to vanilla food nutrition to get BZHS food restoration (e.g. steak nutrition=8 * 5.0 = 40 food restored)")
                .defineInRange("foodRestorationMultiplier", 5.0, 0.1, 50.0);
        waterPerDrink = builder
                .comment("Water restored per drink (water bottle, potion, milk bucket)")
                .defineInRange("waterPerDrink", 25.0, 1.0, 100.0);
        builder.pop();

        // XP
        builder.push("xp");
        vanillaMobXP = builder
                .comment("Mod XP awarded for killing vanilla hostile mobs (not BZHS zombies)")
                .defineInRange("vanillaMobXP", 50, 0, 1000);
        builder.pop();

        // Player Damage
        builder.push("combat");
        playerDamageMultiplier = builder
                .comment("Multiplier for player damage dealt to BZHS zombie entities (2.0 = double damage)")
                .defineInRange("playerDamageMultiplier", 1.0, 0.1, 10.0);
        builder.pop();

        // Sync
        builder.push("sync");
        syncIntervalTicks = builder
                .comment("How often to sync stats to client (in ticks, 20 = 1 sec)")
                .defineInRange("syncIntervalTicks", 10, 1, 100);
        builder.pop();

        builder.pop(); // survival
    }
}
