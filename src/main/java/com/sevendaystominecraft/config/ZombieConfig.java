package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ZombieConfig {

    public static final ModConfigSpec SPEC;
    public static final ZombieConfig INSTANCE;

    static {
        Pair<ZombieConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(ZombieConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.IntValue maxZombiesBase;
    public final ModConfigSpec.IntValue maxZombiesCap;
    public final ModConfigSpec.DoubleValue nightSpeedBonus;
    public final ModConfigSpec.DoubleValue darknessSpeedBonus;
    public final ModConfigSpec.IntValue darknessLightThreshold;
    public final ModConfigSpec.BooleanValue replaceVanillaHostiles;

    public final ModConfigSpec.DoubleValue walkerHP;
    public final ModConfigSpec.DoubleValue walkerDamage;
    public final ModConfigSpec.DoubleValue walkerSpeed;

    public final ModConfigSpec.DoubleValue crawlerHP;
    public final ModConfigSpec.DoubleValue crawlerDamage;
    public final ModConfigSpec.DoubleValue crawlerSpeed;

    public final ModConfigSpec.DoubleValue frozenLumberjackHP;
    public final ModConfigSpec.DoubleValue frozenLumberjackDamage;
    public final ModConfigSpec.DoubleValue frozenLumberjackSpeed;

    public final ModConfigSpec.DoubleValue bloatedWalkerHP;
    public final ModConfigSpec.DoubleValue bloatedWalkerDamage;
    public final ModConfigSpec.DoubleValue bloatedWalkerSpeed;
    public final ModConfigSpec.IntValue bloatedExplosionRadius;

    public final ModConfigSpec.DoubleValue spiderZombieHP;
    public final ModConfigSpec.DoubleValue spiderZombieDamage;
    public final ModConfigSpec.DoubleValue spiderZombieSpeed;

    public final ModConfigSpec.DoubleValue feralWightHP;
    public final ModConfigSpec.DoubleValue feralWightDamage;
    public final ModConfigSpec.DoubleValue feralWightSpeed;

    public final ModConfigSpec.DoubleValue copHP;
    public final ModConfigSpec.DoubleValue copDamage;
    public final ModConfigSpec.DoubleValue copSpeed;
    public final ModConfigSpec.DoubleValue copBileDamage;
    public final ModConfigSpec.IntValue copBileRange;

    public final ModConfigSpec.DoubleValue screamerHP;
    public final ModConfigSpec.DoubleValue screamerDamage;
    public final ModConfigSpec.DoubleValue screamerSpeed;
    public final ModConfigSpec.IntValue screamerSpawnMin;
    public final ModConfigSpec.IntValue screamerSpawnMax;

    public final ModConfigSpec.DoubleValue zombieDogHP;
    public final ModConfigSpec.DoubleValue zombieDogDamage;
    public final ModConfigSpec.DoubleValue zombieDogSpeed;

    public final ModConfigSpec.DoubleValue vultureHP;
    public final ModConfigSpec.DoubleValue vultureDamage;
    public final ModConfigSpec.DoubleValue vultureSpeed;

    public final ModConfigSpec.DoubleValue demolisherHP;
    public final ModConfigSpec.DoubleValue demolisherDamage;
    public final ModConfigSpec.DoubleValue demolisherSpeed;
    public final ModConfigSpec.IntValue demolisherExplosionRadius;

    public final ModConfigSpec.DoubleValue mutatedChuckHP;
    public final ModConfigSpec.DoubleValue mutatedChuckDamage;
    public final ModConfigSpec.DoubleValue mutatedChuckSpeed;
    public final ModConfigSpec.IntValue mutatedChuckVomitRange;

    public final ModConfigSpec.DoubleValue zombieBearHP;
    public final ModConfigSpec.DoubleValue zombieBearDamage;
    public final ModConfigSpec.DoubleValue zombieBearSpeed;

    public final ModConfigSpec.DoubleValue nurseHP;
    public final ModConfigSpec.DoubleValue nurseDamage;
    public final ModConfigSpec.DoubleValue nurseSpeed;
    public final ModConfigSpec.DoubleValue nurseHealRate;
    public final ModConfigSpec.IntValue nurseHealRadius;

    public final ModConfigSpec.DoubleValue soldierHP;
    public final ModConfigSpec.DoubleValue soldierDamage;
    public final ModConfigSpec.DoubleValue soldierSpeed;

    public final ModConfigSpec.DoubleValue behemothHP;
    public final ModConfigSpec.DoubleValue behemothDamage;
    public final ModConfigSpec.DoubleValue behemothSpeed;
    public final ModConfigSpec.IntValue behemothGroundPoundRadius;

    public final ModConfigSpec.DoubleValue radiatedHPMult;
    public final ModConfigSpec.DoubleValue radiatedDamageMult;
    public final ModConfigSpec.DoubleValue radiatedSpeedMult;
    public final ModConfigSpec.DoubleValue radiatedRegenPerSec;

    public final ModConfigSpec.DoubleValue chargedHPMult;
    public final ModConfigSpec.DoubleValue chargedDamageMult;
    public final ModConfigSpec.DoubleValue chargedSpeedMult;
    public final ModConfigSpec.IntValue chargedChainTargets;
    public final ModConfigSpec.DoubleValue chargedChainDamage;

    public final ModConfigSpec.DoubleValue infernalHPMult;
    public final ModConfigSpec.DoubleValue infernalDamageMult;
    public final ModConfigSpec.DoubleValue infernalSpeedMult;
    public final ModConfigSpec.IntValue infernalFireTrailInterval;

    public final ModConfigSpec.BooleanValue blockBreakEnabled;
    public final ModConfigSpec.DoubleValue blockBreakSpeedMultiplier;
    public final ModConfigSpec.IntValue investigateRange;
    public final ModConfigSpec.IntValue hordePathRange;
    public final ModConfigSpec.IntValue hordePathRangeDay21;
    public final ModConfigSpec.DoubleValue blockHPMultiplier;

    ZombieConfig(ModConfigSpec.Builder builder) {
        builder.comment("Brutal Zombie Horde Survival — Zombie Configuration",
                       "Per-variant HP/damage/speed overrides (spec §3.1)")
               .push("zombies");

        builder.push("general");
        maxZombiesBase = builder
                .comment("Base max zombies (formula: base + dayNumber * 2, capped at cap)")
                .defineInRange("maxZombiesBase", 32, 8, 128);
        maxZombiesCap = builder
                .comment("Hard cap on max zombies per player")
                .defineInRange("maxZombiesCap", 64, 16, 256);
        nightSpeedBonus = builder
                .comment("Speed multiplier bonus for sprint-capable zombies at night (spec §3.2: 1.25 = +125%, i.e. 2.25x base speed)")
                .defineInRange("nightSpeedBonus", 1.25, 0.0, 3.0);
        darknessSpeedBonus = builder
                .comment("Speed multiplier bonus for zombies in dark areas (block light and sky light both <= darknessLightThreshold). Defaults to same value as nightSpeedBonus.")
                .defineInRange("darknessSpeedBonus", 1.25, 0.0, 3.0);
        darknessLightThreshold = builder
                .comment("Light level threshold for darkness speed bonus. Zombies get the bonus when both block light and sky light are at or below this value.")
                .defineInRange("darknessLightThreshold", 7, 0, 15);
        replaceVanillaHostiles = builder
                .comment("Replace all vanilla hostile mob spawns with BZHS zombie variants")
                .define("replaceVanillaHostiles", true);
        blockBreakEnabled = builder
                .comment("Whether zombies can break blocks to reach players")
                .define("blockBreakEnabled", true);
        blockBreakSpeedMultiplier = builder
                .comment("Multiplier for zombie block breaking speed (1.0 = normal)")
                .defineInRange("blockBreakSpeedMultiplier", 1.0, 0.1, 10.0);
        investigateRange = builder
                .comment("Range in blocks for zombie heatmap investigation behavior")
                .defineInRange("investigateRange", 32, 8, 128);
        hordePathRange = builder
                .comment("Range in blocks for horde pathfinding to players during Blood Moon")
                .defineInRange("hordePathRange", 64, 16, 256);
        hordePathRangeDay21 = builder
                .comment("Range in blocks for horde pathfinding on day 21+")
                .defineInRange("hordePathRangeDay21", 128, 32, 512);
        blockHPMultiplier = builder
                .comment("Global multiplier for block HP values (higher = blocks take longer to break)")
                .defineInRange("blockHPMultiplier", 1.0, 0.1, 10.0);
        builder.pop();

        builder.push("walker");
        walkerHP = builder.defineInRange("hp", 20.0, 1.0, 1000.0);
        walkerDamage = builder.defineInRange("damage", 3.0, 0.1, 100.0);
        walkerSpeed = builder.defineInRange("speed", 1.0, 0.1, 5.0);
        builder.pop();

        builder.push("crawler");
        crawlerHP = builder.defineInRange("hp", 12.0, 1.0, 1000.0);
        crawlerDamage = builder.defineInRange("damage", 3.8, 0.1, 100.0);
        crawlerSpeed = builder.defineInRange("speed", 0.8, 0.1, 5.0);
        builder.pop();

        builder.push("frozen_lumberjack");
        frozenLumberjackHP = builder.defineInRange("hp", 30.0, 1.0, 1000.0);
        frozenLumberjackDamage = builder.defineInRange("damage", 4.5, 0.1, 100.0);
        frozenLumberjackSpeed = builder.defineInRange("speed", 0.9, 0.1, 5.0);
        builder.pop();

        builder.push("bloated_walker");
        bloatedWalkerHP = builder.defineInRange("hp", 40.0, 1.0, 1000.0);
        bloatedWalkerDamage = builder.defineInRange("damage", 3.8, 0.1, 100.0);
        bloatedWalkerSpeed = builder.defineInRange("speed", 0.7, 0.1, 5.0);
        bloatedExplosionRadius = builder
                .comment("Explosion radius on death (in blocks)")
                .defineInRange("explosionRadius", 2, 1, 8);
        builder.pop();

        builder.push("spider_zombie");
        spiderZombieHP = builder.defineInRange("hp", 24.0, 1.0, 1000.0);
        spiderZombieDamage = builder.defineInRange("damage", 5.3, 0.1, 100.0);
        spiderZombieSpeed = builder.defineInRange("speed", 1.8, 0.1, 5.0);
        builder.pop();

        builder.push("feral_wight");
        feralWightHP = builder.defineInRange("hp", 60.0, 1.0, 2000.0);
        feralWightDamage = builder.defineInRange("damage", 7.5, 0.1, 200.0);
        feralWightSpeed = builder.defineInRange("speed", 2.5, 0.1, 5.0);
        builder.pop();

        builder.push("cop");
        copHP = builder.defineInRange("hp", 70.0, 1.0, 2000.0);
        copDamage = builder.defineInRange("damage", 5.6, 0.1, 200.0);
        copSpeed = builder.defineInRange("speed", 1.2, 0.1, 5.0);
        copBileDamage = builder.defineInRange("bileDamage", 5.0, 0.1, 100.0);
        copBileRange = builder.defineInRange("bileRange", 10, 4, 20);
        builder.pop();

        builder.push("screamer");
        screamerHP = builder.defineInRange("hp", 16.0, 1.0, 1000.0);
        screamerDamage = builder.defineInRange("damage", 1.9, 0.1, 100.0);
        screamerSpeed = builder.defineInRange("speed", 1.5, 0.1, 5.0);
        screamerSpawnMin = builder.defineInRange("spawnMin", 4, 1, 16);
        screamerSpawnMax = builder.defineInRange("spawnMax", 8, 2, 32);
        builder.pop();

        builder.push("zombie_dog");
        zombieDogHP = builder.defineInRange("hp", 16.0, 1.0, 1000.0);
        zombieDogDamage = builder.defineInRange("damage", 6.8, 0.1, 100.0);
        zombieDogSpeed = builder.defineInRange("speed", 3.5, 0.1, 8.0);
        builder.pop();

        builder.push("vulture");
        vultureHP = builder.defineInRange("hp", 12.0, 1.0, 1000.0);
        vultureDamage = builder.defineInRange("damage", 4.5, 0.1, 100.0);
        vultureSpeed = builder.defineInRange("speed", 4.0, 0.1, 8.0);
        builder.pop();

        builder.push("demolisher");
        demolisherHP = builder.defineInRange("hp", 160.0, 10.0, 5000.0);
        demolisherDamage = builder.defineInRange("damage", 11.3, 0.1, 200.0);
        demolisherSpeed = builder.defineInRange("speed", 1.0, 0.1, 5.0);
        demolisherExplosionRadius = builder
                .comment("Explosion radius when hit in chest")
                .defineInRange("explosionRadius", 8, 2, 16);
        builder.pop();

        builder.push("mutated_chuck");
        mutatedChuckHP = builder.defineInRange("hp", 50.0, 1.0, 2000.0);
        mutatedChuckDamage = builder.defineInRange("damage", 6.8, 0.1, 200.0);
        mutatedChuckSpeed = builder.defineInRange("speed", 1.3, 0.1, 5.0);
        mutatedChuckVomitRange = builder
                .comment("Range of vomit attack in blocks (2.6: 11)")
                .defineInRange("vomitRange", 11, 4, 20);
        builder.pop();

        builder.push("zombie_bear");
        zombieBearHP = builder.defineInRange("hp", 120.0, 10.0, 5000.0);
        zombieBearDamage = builder.defineInRange("damage", 13.1, 0.1, 200.0);
        zombieBearSpeed = builder.defineInRange("speed", 2.0, 0.1, 5.0);
        builder.pop();

        builder.push("nurse");
        nurseHP = builder.defineInRange("hp", 24.0, 1.0, 1000.0);
        nurseDamage = builder.defineInRange("damage", 3.8, 0.1, 100.0);
        nurseSpeed = builder.defineInRange("speed", 1.0, 0.1, 5.0);
        nurseHealRate = builder
                .comment("HP healed per second to nearby zombies")
                .defineInRange("healRate", 1.0, 0.1, 50.0);
        nurseHealRadius = builder
                .comment("Radius in blocks for healing aura")
                .defineInRange("healRadius", 5, 2, 16);
        builder.pop();

        builder.push("soldier");
        soldierHP = builder.defineInRange("hp", 80.0, 1.0, 2000.0);
        soldierDamage = builder.defineInRange("damage", 9.4, 0.1, 200.0);
        soldierSpeed = builder.defineInRange("speed", 1.5, 0.1, 5.0);
        builder.pop();

        builder.push("behemoth");
        behemothHP = builder.defineInRange("hp", 400.0, 50.0, 10000.0);
        behemothDamage = builder.defineInRange("damage", 18.8, 0.1, 500.0);
        behemothSpeed = builder.defineInRange("speed", 0.8, 0.1, 5.0);
        behemothGroundPoundRadius = builder
                .comment("Ground pound AoE radius in blocks")
                .defineInRange("groundPoundRadius", 6, 2, 16);
        builder.pop();

        builder.push("modifiers");

        builder.push("radiated");
        radiatedHPMult = builder.defineInRange("hpMultiplier", 2.0, 1.0, 5.0);
        radiatedDamageMult = builder.defineInRange("damageMultiplier", 1.5, 1.0, 5.0);
        radiatedSpeedMult = builder.defineInRange("speedMultiplier", 1.3, 1.0, 3.0);
        radiatedRegenPerSec = builder.defineInRange("regenPerSecond", 0.4, 0.0, 20.0);
        builder.pop();

        builder.push("charged");
        chargedHPMult = builder.defineInRange("hpMultiplier", 1.8, 1.0, 5.0);
        chargedDamageMult = builder.defineInRange("damageMultiplier", 1.3, 1.0, 5.0);
        chargedSpeedMult = builder.defineInRange("speedMultiplier", 1.2, 1.0, 3.0);
        chargedChainTargets = builder.defineInRange("chainTargets", 3, 1, 8);
        chargedChainDamage = builder.defineInRange("chainDamage", 5.0, 0.1, 50.0);
        builder.pop();

        builder.push("infernal");
        infernalHPMult = builder.defineInRange("hpMultiplier", 1.8, 1.0, 5.0);
        infernalDamageMult = builder.defineInRange("damageMultiplier", 1.4, 1.0, 5.0);
        infernalSpeedMult = builder.defineInRange("speedMultiplier", 1.1, 1.0, 3.0);
        infernalFireTrailInterval = builder
                .comment("Ticks between fire trail block placement")
                .defineInRange("fireTrailInterval", 20, 5, 100);
        builder.pop();

        builder.pop(); // modifiers
        builder.pop(); // zombies
    }
}
