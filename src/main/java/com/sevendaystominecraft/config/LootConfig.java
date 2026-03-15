package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class LootConfig {

    public static final ModConfigSpec SPEC;
    public static final LootConfig INSTANCE;

    static {
        Pair<LootConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(LootConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.IntValue respawnDays;
    public final ModConfigSpec.DoubleValue abundanceMultiplier;
    public final ModConfigSpec.BooleanValue qualityScaling;

    LootConfig(ModConfigSpec.Builder builder) {
        builder.comment("Brutal Zombie Horde Survival — Loot & Crafting Configuration",
                       "Controls loot container respawn, abundance, and quality scaling (spec §8)")
               .push("loot");

        respawnDays = builder
                .comment("Number of in-game days before looted containers respawn their loot",
                         "0 = containers never respawn, default = 5")
                .defineInRange("respawnDays", 5, 0, 30);

        abundanceMultiplier = builder
                .comment("Multiplier for the amount of loot found in containers",
                         "0.25 = very scarce, 1.0 = normal, 4.0 = very abundant")
                .defineInRange("abundanceMultiplier", 1.0, 0.25, 4.0);

        qualityScaling = builder
                .comment("Whether loot quality scales with the player's loot stage",
                         "If false, all loot is random quality regardless of player progression")
                .define("qualityScaling", true);

        builder.pop();
    }
}
