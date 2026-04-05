package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class QuestConfig {

    public static final ModConfigSpec SPEC;
    public static final QuestConfig INSTANCE;

    static {
        Pair<QuestConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(QuestConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.IntValue maxActiveQuests;
    public final ModConfigSpec.DoubleValue xpRewardMultiplier;
    public final ModConfigSpec.DoubleValue tokenRewardMultiplier;
    public final ModConfigSpec.IntValue questRefreshIntervalDays;
    public final ModConfigSpec.IntValue questsPerTrader;
    public final ModConfigSpec.IntValue buriedTreasureMaxRange;

    QuestConfig(ModConfigSpec.Builder builder) {

        builder.comment("Brutal Zombie Horde Survival — Quest System Configuration")
               .push("quest");

        maxActiveQuests = builder
                .comment("Maximum number of quests a player can have active at once.",
                         "Default 3.")
                .defineInRange("maxActiveQuests", 3, 1, 10);

        xpRewardMultiplier = builder
                .comment("Multiplier applied to quest XP rewards.",
                         "Default 1.0.")
                .defineInRange("xpRewardMultiplier", 1.0, 0.1, 10.0);

        tokenRewardMultiplier = builder
                .comment("Multiplier applied to quest token rewards.",
                         "Default 1.0.")
                .defineInRange("tokenRewardMultiplier", 1.0, 0.1, 10.0);

        questRefreshIntervalDays = builder
                .comment("Legacy config — quests now refresh automatically when the trader restocks.",
                         "This value is no longer used. Kept for config compatibility.")
                .defineInRange("questRefreshIntervalDays", 3, 1, 30);

        questsPerTrader = builder
                .comment("Number of quests each trader offers at a time.",
                         "Default 4.")
                .defineInRange("questsPerTrader", 4, 3, 8);

        buriedTreasureMaxRange = builder
                .comment("Maximum distance in blocks from trader for buried treasure quests.",
                         "Default 200.")
                .defineInRange("buriedTreasureMaxRange", 200, 50, 500);

        builder.pop();
    }
}
