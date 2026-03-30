package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TerritoryConfig {

    public static final ModConfigSpec SPEC;
    public static final TerritoryConfig INSTANCE;

    static {
        Pair<TerritoryConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(TerritoryConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.IntValue spawnChanceDenominator;
    public final ModConfigSpec.IntValue minChunkSpacing;
    public final ModConfigSpec.IntValue syncRangeBlocks;
    public final ModConfigSpec.IntValue entryTriggerRangeBlocks;
    public final ModConfigSpec.IntValue vanillaStructureExclusionRadius;

    public final ModConfigSpec.IntValue safeZoneRadius;
    public final ModConfigSpec.IntValue midRangeRadius;
    public final ModConfigSpec.IntValue farRangeRadius;
    public final ModConfigSpec.IntValue safeZoneMaxTier;
    public final ModConfigSpec.IntValue midRangeMaxTier;
    public final ModConfigSpec.IntValue farRangeMaxTier;
    public final ModConfigSpec.BooleanValue enforceMinSpawnDay;

    TerritoryConfig(ModConfigSpec.Builder builder) {

        builder.comment("Brutal Zombie Horde Survival — Territory Configuration",
                       "Controls how Territory POIs (Points of Interest) generate and behave (spec §2.2)")
               .push("territory");

        spawnChanceDenominator = builder
                .comment("1-in-N chance per new chunk to generate a territory.",
                         "Lower = more territories. Default 12 gives approx 1 territory per 12 new chunks.")
                .defineInRange("spawnChanceDenominator", 12, 5, 200);

        minChunkSpacing = builder
                .comment("Minimum chunk distance between any two territory centers.",
                         "8 = territories must be at least 128 blocks apart.")
                .defineInRange("minChunkSpacing", 8, 4, 64);

        syncRangeBlocks = builder
                .comment("Radius in blocks within which territories are synced to the client compass.",
                         "Higher values show more territory markers on the compass.")
                .defineInRange("syncRangeBlocks", 512, 64, 2048);

        entryTriggerRangeBlocks = builder
                .comment("Radius in blocks within which approaching a territory triggers zombie population.",
                         "Zombies are only spawned when a player first enters this range.")
                .defineInRange("entryTriggerRangeBlocks", 64, 16, 256);

        vanillaStructureExclusionRadius = builder
                .comment("Radius in blocks around vanilla structures (villages, outposts, etc.)",
                         "where BZHS territories will not generate. Set to 0 to disable.",
                         "Default 80 prevents territory/village overlap.")
                .defineInRange("vanillaStructureExclusionRadius", 80, 0, 256);

        builder.pop();

        builder.comment("Spawn Protection — Distance-based difficulty scaling around world spawn (0,0)",
                       "Controls the difficulty curve so early-game areas near spawn are survivable.")
               .push("spawnProtection");

        safeZoneRadius = builder
                .comment("Radius in blocks around world spawn (0,0) where only easy territories generate.",
                         "Default 200 = ~12 chunks. Only tiers up to safeZoneMaxTier spawn here.")
                .defineInRange("safeZoneRadius", 200, 0, 2000);

        midRangeRadius = builder
                .comment("Radius in blocks for the mid-range zone (between safe zone and this value).",
                         "Territories up to midRangeMaxTier can generate in this band.")
                .defineInRange("midRangeRadius", 500, 0, 5000);

        farRangeRadius = builder
                .comment("Radius in blocks for the far-range zone (between mid-range and this value).",
                         "Territories up to farRangeMaxTier can generate. Beyond this, all tiers are allowed.")
                .defineInRange("farRangeRadius", 1000, 0, 10000);

        safeZoneMaxTier = builder
                .comment("Maximum territory tier allowed in the safe zone. Default 1 (Walkers/Crawlers only).")
                .defineInRange("safeZoneMaxTier", 1, 1, 5);

        midRangeMaxTier = builder
                .comment("Maximum territory tier allowed in the mid-range zone. Default 2.")
                .defineInRange("midRangeMaxTier", 2, 1, 5);

        farRangeMaxTier = builder
                .comment("Maximum territory tier allowed in the far-range zone. Default 3.")
                .defineInRange("farRangeMaxTier", 3, 1, 5);

        enforceMinSpawnDay = builder
                .comment("If true, territory zombie spawner checks each zombie type's minSpawnDay.",
                         "Zombies that require a later day are replaced with easier types.")
                .define("enforceMinSpawnDay", true);

        builder.pop();
    }
}
