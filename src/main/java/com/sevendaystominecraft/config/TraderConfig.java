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
    public final ModConfigSpec.IntValue compoundProtectionRadius;
    public final ModConfigSpec.IntValue restockIntervalDays;
    public final ModConfigSpec.IntValue syncRangeBlocks;
    public final ModConfigSpec.IntValue tier1MaxDistance;
    public final ModConfigSpec.IntValue tier2MaxDistance;
    public final ModConfigSpec.IntValue maxElevation;
    public final ModConfigSpec.IntValue maxSlopeVariance;
    public final ModConfigSpec.IntValue maxTradersInRadius;
    public final ModConfigSpec.IntValue maxTradersCheckRadius;

    TraderConfig(ModConfigSpec.Builder builder) {

        builder.comment("Brutal Zombie Horde Survival — Trader NPC Configuration")
               .push("trader");

        guaranteeRadius = builder
                .comment("Guaranteed radius in blocks from world spawn (0,0) within which",
                         "at least one Trader Outpost will generate. Default 150.")
                .defineInRange("guaranteeRadius", 150, 50, 500);

        minChunkSpacing = builder
                .comment("Minimum chunk distance between any two trader outposts.",
                         "80 chunks = roughly 1280 blocks apart.")
                .defineInRange("minChunkSpacing", 80, 10, 200);

        spawnChanceDenominator = builder
                .comment("1-in-N chance per new chunk to attempt placing a trader outpost.",
                         "Lower = more traders. Default 400.")
                .defineInRange("spawnChanceDenominator", 400, 5, 2000);

        protectionRadius = builder
                .comment("Radius in blocks around each trader where zombie spawns and block breaking are suppressed.",
                         "Default 30.")
                .defineInRange("protectionRadius", 30, 10, 100);

        compoundProtectionRadius = builder
                .comment("Radius in blocks around the compound center where natural zombie spawning is suppressed.",
                         "Covers the full trader outpost building cluster. Default 80.")
                .defineInRange("compoundProtectionRadius", 80, 30, 200);

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

        maxTradersInRadius = builder
                .comment("Maximum number of trader outposts allowed within the check radius.",
                         "Prevents clustering of traders in one area. Default 1.")
                .defineInRange("maxTradersInRadius", 1, 1, 10);

        maxTradersCheckRadius = builder
                .comment("Radius in chunks to check for the max trader limit.",
                         "100 chunks = roughly 1600 blocks. Default 100.")
                .defineInRange("maxTradersCheckRadius", 100, 20, 500);

        builder.pop();
    }
}
