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

    TerritoryConfig(ModConfigSpec.Builder builder) {

        builder.comment("Brutal Zombie Horde Survival — Territory Configuration",
                       "Controls how Territory POIs (Points of Interest) generate and behave (spec §2.2)")
               .push("territory");

        spawnChanceDenominator = builder
                .comment("1-in-N chance per new chunk to generate a territory.",
                         "Lower = more territories. Default 40 gives approx 1 territory per 40 new chunks.")
                .defineInRange("spawnChanceDenominator", 40, 5, 200);

        minChunkSpacing = builder
                .comment("Minimum chunk distance between any two territory centers.",
                         "16 = territories must be at least 256 blocks apart.")
                .defineInRange("minChunkSpacing", 16, 4, 64);

        syncRangeBlocks = builder
                .comment("Radius in blocks within which territories are synced to the client compass.",
                         "Higher values show more territory markers on the compass.")
                .defineInRange("syncRangeBlocks", 512, 64, 2048);

        entryTriggerRangeBlocks = builder
                .comment("Radius in blocks within which approaching a territory triggers zombie population.",
                         "Zombies are only spawned when a player first enters this range.")
                .defineInRange("entryTriggerRangeBlocks", 64, 16, 256);

        builder.pop();
    }
}
