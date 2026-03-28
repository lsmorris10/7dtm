package com.sevendaystominecraft.item;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.PlayerStatsHandler;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public class ConsumableStatItem extends Item {

    private final float foodRestore;
    private final float waterRestore;
    private final String[] appliedDebuffs;
    private final String[] curedDebuffs;
    private final int regenTicks;
    private final float poisonChance;

    public ConsumableStatItem(Properties properties, float foodRestore, float waterRestore,
                              String[] appliedDebuffs, String[] curedDebuffs, int regenTicks) {
        this(properties, foodRestore, waterRestore, appliedDebuffs, curedDebuffs, regenTicks, 0f);
    }

    public ConsumableStatItem(Properties properties, float foodRestore, float waterRestore,
                              String[] appliedDebuffs, String[] curedDebuffs, int regenTicks,
                              float poisonChance) {
        super(properties);
        this.foodRestore = foodRestore;
        this.waterRestore = waterRestore;
        this.appliedDebuffs = appliedDebuffs;
        this.curedDebuffs = curedDebuffs;
        this.regenTicks = regenTicks;
        this.poisonChance = poisonChance;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        if (waterRestore > 0 && foodRestore == 0) {
            return ItemUseAnimation.DRINK;
        }
        return ItemUseAnimation.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        if (level.isClientSide() || !(entityLiving instanceof Player player)) {
            return stack;
        }

        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) {
            return stack;
        }

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        if (foodRestore != 0f) {
            stats.setFood(stats.getFood() + foodRestore);
        }
        if (waterRestore != 0f) {
            stats.setWater(stats.getWater() + waterRestore);
        }

        for (String debuff : appliedDebuffs) {
            stats.addDebuff(debuff, 72000);
        }

        for (String debuff : curedDebuffs) {
            if (stats.hasDebuff(debuff)) {
                stats.removeDebuff(debuff);
            }
        }

        if (regenTicks > 0 && player instanceof ServerPlayer) {
            int effectiveRegenTicks = regenTicks;
            int fieldMedicRank = stats.getPerkRank("field_medic");
            if (fieldMedicRank > 0) {
                effectiveRegenTicks = (int) (regenTicks * (1.0f + 0.25f * fieldMedicRank));
            }
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, effectiveRegenTicks, 0));
        }

        if (poisonChance > 0f) {
            float effectivePoisonChance = poisonChance;
            int ironGutRank = stats.getPerkRank("iron_gut");
            if (ironGutRank > 0) {
                effectivePoisonChance *= (1.0f - 0.33f * ironGutRank);
            }
            if (effectivePoisonChance > 0f && level.random.nextFloat() < effectivePoisonChance) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            }
        }

        if (waterRestore > 0 && foodRestore == 0) {
            com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                    com.sevendaystominecraft.sound.ModSounds.PLAYER_DRINK, player,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
        } else if (foodRestore > 0) {
            com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                    com.sevendaystominecraft.sound.ModSounds.PLAYER_EAT_COOKED, player,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        for (String debuff : appliedDebuffs) {
            if (!debuff.isEmpty()) {
                com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                        com.sevendaystominecraft.sound.ModSounds.PLAYER_DEBUFF_APPLY, player,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.8f);
                break;
            }
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PlayerStatsHandler.sendStatsToClient(serverPlayer, stats);
        }

        return stack;
    }
}
