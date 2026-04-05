package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HordeConfig {

    public static final ModConfigSpec SPEC;
    public static final HordeConfig INSTANCE;

    static {
        Pair<HordeConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(HordeConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.IntValue hordeCycleLength;
    public final ModConfigSpec.IntValue baseCount;
    public final ModConfigSpec.DoubleValue difficultyMultiplier;
    public final ModConfigSpec.IntValue maxPerWave;
    public final ModConfigSpec.IntValue waveCount;
    public final ModConfigSpec.IntValue waveIntervalSec;
    public final ModConfigSpec.BooleanValue burnAtDawn;
    public final ModConfigSpec.IntValue feralDay;
    public final ModConfigSpec.IntValue demolisherDay;
    public final ModConfigSpec.IntValue chargedDay;
    public final ModConfigSpec.IntValue infernalDay;

    HordeConfig(ModConfigSpec.Builder builder) {

        builder.comment("Brutal Zombie Horde Survival — Horde Night / Blood Moon Configuration",
                       "Spec reference: §4.1 (blood moon mechanics), §4.2 (scaling formula)")
               .push("horde");

        hordeCycleLength = builder
                .comment("Blood moon occurs every N days (spec §4.1: default 7)")
                .defineInRange("hordeCycleLength", 7, 1, 30);

        baseCount = builder
                .comment("Base zombie count for wave 1 on the first blood moon (spec §4.2: 8)")
                .defineInRange("baseCount", 8, 4, 64);

        difficultyMultiplier = builder
                .comment("Scales the horde growth curve (spec §4.2: 1.0)")
                .defineInRange("difficultyMultiplier", 1.0, 0.1, 5.0);

        maxPerWave = builder
                .comment("Hard cap on zombies per wave (spec §4.2: 64)")
                .defineInRange("maxPerWave", 64, 16, 256);

        waveCount = builder
                .comment("Number of waves per blood moon night (spec §4.2: 4)")
                .defineInRange("waveCount", 4, 1, 12);

        waveIntervalSec = builder
                .comment("Seconds between waves (spec §4.2: 600 = 10 minutes)")
                .defineInRange("waveIntervalSec", 600, 120, 1800);

        burnAtDawn = builder
                .comment("Whether surviving horde zombies burn at sunrise (spec §4.1)")
                .define("burnAtDawn", true);

        builder.comment("Day thresholds for when special zombie types begin appearing in hordes")
               .push("composition");

        feralDay = builder
                .comment("First day Ferals appear in horde waves (spec §4.2: 14)")
                .defineInRange("feralDay", 14, 1, 100);

        demolisherDay = builder
                .comment("First day Demolishers appear in horde waves (spec §4.2: 21)")
                .defineInRange("demolisherDay", 21, 7, 100);

        chargedDay = builder
                .comment("First day Charged zombies appear in horde waves (spec §4.2, 2.6 NEW: 21)")
                .defineInRange("chargedDay", 21, 7, 100);

        infernalDay = builder
                .comment("First day Infernal zombies appear in horde waves (spec §4.2, 2.6 NEW: 21)")
                .defineInRange("infernalDay", 21, 7, 100);

        builder.pop(); // composition
        builder.pop(); // horde
    }
}
