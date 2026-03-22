package com.sevendaystominecraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class StructuralIntegrityConfig {

    public static final ModConfigSpec SPEC;
    public static final StructuralIntegrityConfig INSTANCE;

    static {
        Pair<StructuralIntegrityConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(StructuralIntegrityConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue enabled;
    public final ModConfigSpec.IntValue collapseDelayTicks;

    StructuralIntegrityConfig(ModConfigSpec.Builder builder) {
        builder.comment("Brutal Zombie Horde Survival — Structural Integrity Configuration (spec §2.4)")
               .push("structuralIntegrity");

        enabled = builder
                .comment("Whether structural integrity checks are enabled")
                .define("enabled", true);

        collapseDelayTicks = builder
                .comment("Delay in ticks before unsupported blocks collapse (20 ticks = 1 second)")
                .defineInRange("collapseDelay", 10, 1, 100);

        builder.pop();
    }
}
