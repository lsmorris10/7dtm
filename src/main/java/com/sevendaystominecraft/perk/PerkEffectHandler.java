package com.sevendaystominecraft.perk;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.PlayerStatsHandler;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class PerkEffectHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerTakeDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        float dmgReduction = PlayerStatsHandler.getDamageReductionMultiplier(stats);
        if (dmgReduction < 1.0f) {
            event.setNewDamage(event.getNewDamage() * dmgReduction);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerFatalDamage(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        if (stats.getPerkRank("unkillable") > 0) {
            long currentTime = player.level().getGameTime();
            long cooldownEnd = stats.getUnkillableCooldownEnd();

            if (currentTime >= cooldownEnd) {
                event.setCanceled(true);
                player.setHealth(1.0f);
                stats.setUnkillableCooldownEnd(currentTime + 72000L);

                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§d[BZHS] Unkillable activated! 10s invulnerability."), true);
                player.invulnerableTime = 200;

                SevenDaysToMinecraft.LOGGER.info("[BZHS] Unkillable proc for {} — 60 min cooldown started",
                        player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        float mult = PlayerStatsHandler.getMiningSpeedMultiplier(stats);
        if (mult != 1.0f) {
            event.setNewSpeed(event.getNewSpeed() * mult);
        }
    }

    @SubscribeEvent
    public static void onZombieKilledForGhost(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof BaseSevenDaysZombie)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        if (stats.getPerkRank("ghost") <= 0) return;

        if (player.isShiftKeyDown()) {
            if (player.level() instanceof ServerLevel serverLevel) {
                ChunkPos chunkPos = new ChunkPos(event.getEntity().blockPosition());
                SevenDaysToMinecraft.LOGGER.debug("[BZHS] Ghost perk: stealth kill, zero heatmap noise at ({}, {})",
                        chunkPos.x, chunkPos.z);
            }
        }
    }
}
