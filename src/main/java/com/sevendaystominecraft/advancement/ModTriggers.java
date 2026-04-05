package com.sevendaystominecraft.advancement;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
            DeferredRegister.create(Registries.TRIGGER_TYPE, SevenDaysToMinecraft.MOD_ID);

    public static final Supplier<PerkRankTrigger> PERK_RANK =
            TRIGGER_TYPES.register("perk_rank", PerkRankTrigger::new);

    public static final Supplier<MagazineReadTrigger> MAGAZINE_READ =
            TRIGGER_TYPES.register("magazine_read", MagazineReadTrigger::new);
}
