package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HeatmapConfig {

    public static final ModConfigSpec SPEC;
    public static final HeatmapConfig INSTANCE;

    static {
        Pair<HeatmapConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(HeatmapConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue enabled;
    public final ModConfigSpec.DoubleValue decayMultiplier;
    public final ModConfigSpec.DoubleValue spawnThresholdMultiplier;

    HeatmapConfig(ModConfigSpec.Builder builder) {

        builder.comment("Brutal Zombie Horde Survival — Heatmap Configuration",
                       "Per-chunk heat system that drives ambient zombie spawns (spec §1.3)",
                       "Higher heat = more zombie activity near noisy players")
               .push("heatmap");

        enabled = builder
                .comment("Enable/disable the heatmap system entirely")
                .define("enabled", true);

        decayMultiplier = builder
                .comment("Multiplier for heat decay rates (higher = faster decay, lower = slower)",
                         "1.0 = spec default, 0.5 = heat lingers twice as long, 2.0 = decays twice as fast")
                .defineInRange("decayMultiplier", 1.0, 0.1, 5.0);

        spawnThresholdMultiplier = builder
                .comment("Multiplier for spawn threshold values (higher = harder to trigger spawns)",
                         "1.0 = spec default (scouts at 25, screamer at 50, mini-horde at 75, waves at 100)",
                         "0.5 = thresholds halved (easier spawns), 2.0 = thresholds doubled")
                .defineInRange("spawnThresholdMultiplier", 1.0, 0.5, 3.0);

        builder.pop();
    }
}
