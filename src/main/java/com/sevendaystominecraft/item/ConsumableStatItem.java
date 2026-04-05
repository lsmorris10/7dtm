package com.sevendaystominecraft.item;

import com.sevendaystominecraft.capability.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
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

        // Ensure player stats attachment is present — force-init if missing
        // (e.g. player used /give before the attachment was initialized)
        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) {
            player.setData(ModAttachments.PLAYER_STATS.get(), new SevenDaysPlayerStats());
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
            // Use vanilla drink sound — reliable and correct
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.GENERIC_DRINK.value(),
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
        if (foodRestore > 0 && waterRestore > 0) {
            tooltip.add(Component.literal("§aRestores " + fmt(foodRestore) + " Food, " + fmt(waterRestore) + " Water"));
        } else if (foodRestore > 0) {
            tooltip.add(Component.literal("§aRestores " + fmt(foodRestore) + " Food"));
        } else if (waterRestore > 0) {
            tooltip.add(Component.literal("§bRestores " + fmt(waterRestore) + " Water"));
        }

        if (regenTicks > 0) {
            tooltip.add(Component.literal("§dGrants Regeneration (" + (regenTicks / 20) + "s)"));
        }

        for (String debuff : curedDebuffs) {
            if (!debuff.isEmpty()) {
                tooltip.add(Component.literal("§aCures " + formatDebuffName(debuff)));
            }
        }

        for (String debuff : appliedDebuffs) {
            if (!debuff.isEmpty()) {
                tooltip.add(Component.literal("§cRisk: " + formatDebuffName(debuff)));
            }
        }

        if (poisonChance > 0f) {
            int pct = Math.round(poisonChance * 100);
            tooltip.add(Component.literal("§c" + pct + "% chance of food poisoning"));
        }
    }

    private static String fmt(float value) {
        if (value == (int) value) return String.valueOf((int) value);
        return String.format("%.1f", value);
    }

    private static String formatDebuffName(String debuffId) {
        return switch (debuffId) {
            case "bleeding" -> "Bleeding";
            case "infection_1" -> "Infection I";
            case "infection_2" -> "Infection II";
            case "dysentery" -> "Dysentery";
            case "sprain" -> "Sprain";
            case "fracture" -> "Fracture";
            case "concussion" -> "Concussion";
            case "burn" -> "Burn";
            case "hypothermia" -> "Hypothermia";
            case "hyperthermia" -> "Hyperthermia";
            default -> debuffId.substring(0, 1).toUpperCase() + debuffId.substring(1).replace('_', ' ');
        };
    }
}
