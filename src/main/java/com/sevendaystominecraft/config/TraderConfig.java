package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TraderConfig {

    public static final ModConfigSpec SPEC;
    public static final TraderConfig INSTANCE;

    static {
        Pair<TraderConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(TraderConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.IntValue guaranteeRadius;
    public final ModConfigSpec.IntValue minChunkSpacing;
    public final ModConfigSpec.IntValue spawnChanceDenominator;
    public final ModConfigSpec.IntValue protectionRadius;
    public final ModConfigSpec.IntValue restockIntervalDays;
    public final ModConfigSpec.IntValue syncRangeBlocks;
    public final ModConfigSpec.IntValue tier1MaxDistance;
    public final ModConfigSpec.IntValue tier2MaxDistance;
    public final ModConfigSpec.IntValue maxElevation;
    public final ModConfigSpec.IntValue maxSlopeVariance;

    TraderConfig(ModConfigSpec.Builder builder) {

        builder.comment("Brutal Zombie Horde Survival — Trader NPC Configuration")
               .push("trader");

        guaranteeRadius = builder
                .comment("Guaranteed radius in blocks from world spawn (0,0) within which",
                         "at least one Trader Outpost will generate. Default 150.")
                .defineInRange("guaranteeRadius", 150, 50, 500);

        minChunkSpacing = builder
                .comment("Minimum chunk distance between any two trader outposts.",
                         "55 chunks = roughly 880 blocks apart.")
                .defineInRange("minChunkSpacing", 55, 10, 128);

        spawnChanceDenominator = builder
                .comment("1-in-N chance per new chunk to attempt placing a trader outpost.",
                         "Lower = more traders. Default 200.")
                .defineInRange("spawnChanceDenominator", 200, 5, 1000);

        protectionRadius = builder
                .comment("Radius in blocks around each trader where zombie spawns and block breaking are suppressed.",
                         "Default 30.")
                .defineInRange("protectionRadius", 30, 10, 100);

        restockIntervalDays = builder
                .comment("Number of in-game days between trader inventory restocks.",
                         "Default 3.")
                .defineInRange("restockIntervalDays", 3, 1, 30);

        syncRangeBlocks = builder
                .comment("Radius in blocks within which traders are synced to the client for map display.",
                         "Default 512.")
                .defineInRange("syncRangeBlocks", 512, 64, 2048);

        tier1MaxDistance = builder
                .comment("Traders within this distance from spawn stock Tier 1 items.",
                         "Default 300.")
                .defineInRange("tier1MaxDistance", 300, 100, 2000);

        tier2MaxDistance = builder
                .comment("Traders within this distance from spawn stock Tier 2 items.",
                         "Beyond this distance, traders stock Tier 3+ items. Default 800.")
                .defineInRange("tier2MaxDistance", 800, 200, 5000);

        maxElevation = builder
                .comment("Maximum surface Y elevation at which a trader outpost can spawn.",
                         "Prevents traders from appearing on mountain peaks. Default 100.")
                .defineInRange("maxElevation", 100, 50, 256);

        maxSlopeVariance = builder
                .comment("Maximum allowed slope variance (difference between highest and lowest",
                         "sample points) for a trader outpost location. Default 6.")
                .defineInRange("maxSlopeVariance", 6, 2, 20);

        builder.pop();
    }
}
