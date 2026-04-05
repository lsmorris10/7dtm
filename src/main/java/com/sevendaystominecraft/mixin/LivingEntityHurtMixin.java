package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.ChargedZombie;
import com.sevendaystominecraft.entity.zombie.CopZombie;
import com.sevendaystominecraft.entity.zombie.FeralWightZombie;

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
        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        SurvivalConfig cfg = SurvivalConfig.INSTANCE;

        boolean isZombie = (source.getEntity() instanceof BaseSevenDaysZombie)
                || (source.getEntity() instanceof Zombie);

        if (isZombie && source.getEntity() instanceof LivingEntity) {
            float bleedChance = cfg.bleedingChance.get().floatValue();
            if (player.getRandom().nextFloat() < bleedChance) {
                stats.addDebuff(SevenDaysPlayerStats.DEBUFF_BLEEDING, cfg.bleedingDuration.get());
            }

            float infectionChance = cfg.infectionBaseChance.get().floatValue();
            if (source.getEntity() instanceof FeralWightZombie) {
                infectionChance += 0.05f;
            }
            if (player.getRandom().nextFloat() < infectionChance) {
                if (!stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_1)
                        && !stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_2)) {
                    stats.addDebuff(SevenDaysPlayerStats.DEBUFF_INFECTION_1, cfg.infection1Duration.get());
                }
            }

            if (source.getEntity() instanceof ChargedZombie) {
                applyFreeze(stats, SevenDaysPlayerStats.DEBUFF_ELECTROCUTED, 30);
            }

            if (source.getEntity() instanceof CopZombie && source.getDirectEntity() != source.getEntity()) {
                applyFreeze(stats, SevenDaysPlayerStats.DEBUFF_STUNNED, 40);
            }
        }

        String damageType = source.type().msgId();
        boolean isFireDamage = damageType.equals("onFire")
                || damageType.equals("inFire")
                || damageType.equals("lava")
                || source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);
        if (isFireDamage) {
            stats.addDebuff(SevenDaysPlayerStats.DEBUFF_BURN, cfg.burnDuration.get());
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
