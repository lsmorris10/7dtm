package com.sevendaystominecraft.perk;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.config.SurvivalConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class LevelManager {

    public static int xpToNextLevel(int currentLevel) {
        return (int) Math.floor(1000.0 * Math.pow(currentLevel, 1.05));
    }

    public static void awardXp(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        stats.addXp(amount);

        int charismaRank = stats.getPerkRank("charismatic_nature");
        if (charismaRank > 0 && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            double shareRange = 10.0 + (10.0 * charismaRank);
            int sharedXp = Math.max(1, (int) (amount * 0.10f * charismaRank));
            for (ServerPlayer nearby : serverLevel.getPlayers(
                    p -> p != player && p.distanceTo(player) <= shareRange)) {
                awardXpDirect(nearby, sharedXp);
            }
        }

        int xpNeeded = xpToNextLevel(stats.getLevel());
        while (stats.getXp() >= xpNeeded) {
            stats.setXp(stats.getXp() - xpNeeded);
            int newLevel = stats.getLevel() + 1;
            stats.setLevel(newLevel);
            stats.addPerkPoints(1);

            if (newLevel % 10 == 0) {
                stats.addAttributePoints(1);
                SevenDaysToMinecraft.LOGGER.info("[BZHS] {} reached level {} — earned 1 perk point + 1 bonus attribute point",
                        player.getName().getString(), newLevel);
            } else {
                SevenDaysToMinecraft.LOGGER.info("[BZHS] {} reached level {} — earned 1 perk point",
                        player.getName().getString(), newLevel);
            }

            xpNeeded = xpToNextLevel(stats.getLevel());
        }
    }

    private static void awardXpDirect(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        stats.addXp(amount);

        int xpNeeded = xpToNextLevel(stats.getLevel());
        while (stats.getXp() >= xpNeeded) {
            stats.setXp(stats.getXp() - xpNeeded);
            int newLevel = stats.getLevel() + 1;
            stats.setLevel(newLevel);
            stats.addPerkPoints(1);

            if (newLevel % 10 == 0) {
                stats.addAttributePoints(1);
            }

            xpNeeded = xpToNextLevel(stats.getLevel());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        var source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        if (event.getEntity() instanceof BaseSevenDaysZombie zombie) {
            int xp = zombie.getVariant().getXpReward();
            if (zombie.getModifier() != null) {
                xp += zombie.getModifier().getXpReward();
            }
            awardXp(player, xp);
        } else if (event.getEntity() instanceof Monster) {
            int xp = SurvivalConfig.INSTANCE.vanillaMobXP.get();
            if (xp > 0) {
                awardXp(player, xp);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        float hardness = event.getState().getDestroySpeed(event.getLevel(), event.getPos());
        int xp;
        if (hardness <= 0.5f) {
            xp = 1;
        } else if (hardness <= 2.0f) {
            xp = 2;
        } else if (hardness <= 5.0f) {
            xp = 3;
        } else if (hardness <= 10.0f) {
            xp = 4;
        } else {
            xp = 5;
        }
        awardXp(player, xp);
    }
}
