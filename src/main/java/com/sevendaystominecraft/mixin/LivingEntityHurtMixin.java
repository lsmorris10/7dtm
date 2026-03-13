package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.ChargedZombie;
import com.sevendaystominecraft.entity.zombie.CopZombie;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHurtMixin {

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void sevendaystominecraft$rollDebuffsOnHurt(
            ServerLevel level, DamageSource source, float amount, CallbackInfo ci
    ) {
        if (!((Object) this instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (source.getEntity() == null || !(source.getEntity() instanceof LivingEntity attacker)) return;
        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) return;

        boolean isZombie = attacker instanceof BaseSevenDaysZombie || attacker instanceof Zombie;
        if (!isZombie) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        SurvivalConfig cfg = SurvivalConfig.INSTANCE;

        float bleedChance = cfg.bleedingChance.get().floatValue();
        if (player.getRandom().nextFloat() < bleedChance) {
            int currentStacks = stats.getBleedingStacks();
            if (currentStacks < SevenDaysPlayerStats.MAX_BLEEDING_STACKS) {
                stats.setBleedingStacks(currentStacks + 1);
            }
            stats.addDebuff(SevenDaysPlayerStats.DEBUFF_BLEEDING, 72000);
        }

        float infectionChance = cfg.infectionBaseChance.get().floatValue();
        if (player.getRandom().nextFloat() < infectionChance) {
            if (!stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_1)
                    && !stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_2)) {
                stats.addDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_1, (int) SevenDaysConstants.DAY_LENGTH);
            }
        }

        if (attacker instanceof ChargedZombie) {
            applyFreeze(stats, SevenDaysPlayerStats.DEBUFF_ELECTROCUTED, 30);
        }

        if (attacker instanceof CopZombie && source.getDirectEntity() != source.getEntity()) {
            applyFreeze(stats, SevenDaysPlayerStats.DEBUFF_STUNNED, 40);
        }
    }

    private static void applyFreeze(SevenDaysPlayerStats stats, String debuffId, int duration) {
        int electrocutedRemaining = stats.getDebuffs().getOrDefault(SevenDaysPlayerStats.DEBUFF_ELECTROCUTED, 0);
        int stunnedRemaining = stats.getDebuffs().getOrDefault(SevenDaysPlayerStats.DEBUFF_STUNNED, 0);
        int currentMax = Math.max(electrocutedRemaining, stunnedRemaining);

        if (duration > currentMax) {
            if (electrocutedRemaining > 0) stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_ELECTROCUTED);
            if (stunnedRemaining > 0) stats.removeDebuff(SevenDaysPlayerStats.DEBUFF_STUNNED);
            stats.addDebuff(debuffId, duration);
        }
    }
}
