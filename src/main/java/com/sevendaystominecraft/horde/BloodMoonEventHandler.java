package com.sevendaystominecraft.horde;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HordeConfig;
import com.sevendaystominecraft.network.BloodMoonSyncPayload;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.sevendaystominecraft.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class BloodMoonEventHandler {

    private static final int WARNING_TIME = 14000;
    private static final int SKY_RED_TIME = 12000;
    private static final int SIREN_TIME = 12500;
    private static final int HORDE_START_TIME = 16000;
    private static final int FINAL_WAVE_TIME = 22000;
    private static final int DAWN_BURN_TIME = 23500;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel().dimension() != Level.OVERWORLD) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        BloodMoonTracker tracker = BloodMoonTracker.getOrCreate(level);

        int currentDay = BloodMoonTracker.calculateGameDay(level);
        int timeOfDay = BloodMoonTracker.getTimeOfDay(level);

        if (currentDay != tracker.getLastKnownDay()) {
            tracker.setGameDay(currentDay);
            tracker.setLastKnownDay(currentDay);
            tracker.resetDayFlags();
            if (tracker.getPhase() != BloodMoonTracker.Phase.NONE) {
                tracker.endBloodMoon();
                syncBloodMoonState(level, false, 0, HordeConfig.INSTANCE.waveCount.get(), currentDay);
            }
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Day {} has begun", currentDay);
        }

        int cycle = HordeConfig.INSTANCE.hordeCycleLength.get();
        boolean isTomorrowBloodMoon = currentDay > 0 && (currentDay + 1) % cycle == 0;
        boolean isTodayBloodMoon = currentDay > 0 && currentDay % cycle == 0;

        if (isTomorrowBloodMoon && !tracker.hasSentWarning() && timeOfDay >= WARNING_TIME) {
            tracker.setSentWarning(true);
            broadcastMessage(level, Component.literal("§c§l[BZHS] §eHorde Night Tomorrow! Prepare your defenses!"));
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Horde Night warning sent — blood moon tomorrow (day {})", currentDay + 1);
        }

        if (isTodayBloodMoon) {
            handleBloodMoonDay(level, tracker, currentDay, timeOfDay);
        }

        if (tracker.isHordeSpawning()) {
            handleActiveHorde(level, tracker, currentDay, timeOfDay);
        }

        if (tracker.getPhase() == BloodMoonTracker.Phase.POST) {
            handlePostHorde(level, tracker, currentDay, timeOfDay);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        ServerLevel level = serverPlayer.serverLevel();
        if (level.dimension() != Level.OVERWORLD) return;

        BloodMoonTracker tracker = BloodMoonTracker.getOrCreate(level);
        if (tracker.isBloodMoonActive()) {
            int totalWaves = HordeConfig.INSTANCE.waveCount.get();
            BloodMoonSyncPayload payload = new BloodMoonSyncPayload(
                    true, tracker.getCurrentWave(), totalWaves, tracker.getGameDay());
            PacketDistributor.sendToPlayer(serverPlayer, payload);
            SevenDaysToMinecraft.LOGGER.debug("[BZHS] Synced blood moon state to {} on login", 
                    serverPlayer.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerTrySleep(CanPlayerSleepEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != Level.OVERWORLD) return;

        BloodMoonTracker tracker = BloodMoonTracker.getOrCreate(serverLevel);
        if (tracker.isBloodMoonActive()) {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
            if (event.getEntity() instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.literal("§cYou cannot sleep during a Blood Moon!"), true);
            }
        }
    }

    private static void handleBloodMoonDay(ServerLevel level, BloodMoonTracker tracker,
                                            int currentDay, int timeOfDay) {
        if (!tracker.hasSentSkyRed() && timeOfDay >= SKY_RED_TIME) {
            tracker.setSentSkyRed(true);
            tracker.setPhase(BloodMoonTracker.Phase.PREP);
            broadcastMessage(level, Component.literal("§4§l[BZHS] §cBlood Moon Rising! The sky turns red..."));
            syncBloodMoonState(level, true, 0, HordeConfig.INSTANCE.waveCount.get(), currentDay);
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Blood Moon sky effect activated — day {}", currentDay);
        }

        if (!tracker.hasSentSiren() && timeOfDay >= SIREN_TIME) {
            tracker.setSentSiren(true);
            for (ServerPlayer player : level.players()) {
                player.playNotifySound(ModSounds.BLOOD_MOON_SIREN.get(), SoundSource.HOSTILE, 1.5f, 0.5f);
            }
            broadcastMessage(level, Component.literal("§4§l[BZHS] §c⚠ WARNING: Blood Moon siren!"));
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Blood Moon siren played");
        }

        if (!tracker.hasStartedHorde() && timeOfDay >= HORDE_START_TIME) {
            tracker.setStartedHorde(true);
            tracker.startBloodMoon();
            tracker.setCurrentWave(0);

            broadcastMessage(level, Component.literal("§4§l[BZHS] §c§lHORDE NIGHT BEGINS! §fWave 1 incoming!"));
            HordeSpawner.spawnWave(level, 0, currentDay);
            tracker.setCurrentWave(1);

            int intervalTicks = HordeConfig.INSTANCE.waveIntervalSec.get() * 20;
            tracker.setTicksUntilNextWave(intervalTicks);

            syncBloodMoonState(level, true, 1, HordeConfig.INSTANCE.waveCount.get(), currentDay);
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Horde Night started! Wave 1 spawned on day {}", currentDay);
        }
    }

    private static void handleActiveHorde(ServerLevel level, BloodMoonTracker tracker,
                                           int currentDay, int timeOfDay) {
        int totalWaves = HordeConfig.INSTANCE.waveCount.get();
        int currentWave = tracker.getCurrentWave();

        if (timeOfDay >= FINAL_WAVE_TIME) {
            if (!tracker.hasSpawnedFinalWave()) {
                tracker.setSpawnedFinalWave(true);
                broadcastMessage(level, Component.literal("§4§l[BZHS] §c§lFINAL WAVE! Hold the line!"));
                HordeSpawner.spawnWave(level, totalWaves - 1, currentDay);
                SevenDaysToMinecraft.LOGGER.info("[BZHS] Final wave spawned (wave {})", totalWaves);
            }

            tracker.setPhase(BloodMoonTracker.Phase.POST);
            broadcastMessage(level, Component.literal("§e§l[BZHS] §fThe horde thins... dawn approaches."));
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Horde spawning ended, entering post phase on day {}", currentDay);
            return;
        }

        if (currentWave < totalWaves) {
            tracker.decrementWaveTimer();

            if (tracker.getTicksUntilNextWave() <= 0) {
                broadcastMessage(level, Component.literal(
                        "§4§l[BZHS] §cWave " + (currentWave + 1) + " incoming!"));
                HordeSpawner.spawnWave(level, currentWave, currentDay);
                tracker.setCurrentWave(currentWave + 1);

                int intervalTicks = HordeConfig.INSTANCE.waveIntervalSec.get() * 20;
                tracker.setTicksUntilNextWave(intervalTicks);

                syncBloodMoonState(level, true, currentWave + 1, totalWaves, currentDay);
                SevenDaysToMinecraft.LOGGER.info("[BZHS] Wave {} spawned", currentWave + 1);
            }
        }
    }

    private static void handlePostHorde(ServerLevel level, BloodMoonTracker tracker,
                                         int currentDay, int timeOfDay) {
        if (timeOfDay >= DAWN_BURN_TIME) {
            if (!tracker.hasBurnedAtDawn() && HordeConfig.INSTANCE.burnAtDawn.get()) {
                tracker.setBurnedAtDawn(true);
                burnSurvivingZombies(level);
            }

            tracker.endBloodMoon();
            broadcastMessage(level, Component.literal("§a§l[BZHS] §fThe Blood Moon fades... You survived!"));
            syncBloodMoonState(level, false, 0, HordeConfig.INSTANCE.waveCount.get(), currentDay);
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Blood Moon ended on day {}", currentDay);
        }
    }

    private static void burnSurvivingZombies(ServerLevel level) {
        int burned = 0;
        for (ServerPlayer player : level.players()) {
            AABB area = new AABB(
                    player.getX() - 128, player.getY() - 64, player.getZ() - 128,
                    player.getX() + 128, player.getY() + 64, player.getZ() + 128
            );
            List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, area);
            for (Zombie zombie : zombies) {
                zombie.igniteForSeconds(10);
                burned++;
            }
        }
        if (burned > 0) {
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Dawn cleanup: {} horde zombies set on fire", burned);
        }
    }

    private static void broadcastMessage(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(message);
        }
    }

    private static void syncBloodMoonState(ServerLevel level, boolean active, int wave,
                                            int totalWaves, int dayNumber) {
        BloodMoonSyncPayload payload = new BloodMoonSyncPayload(active, wave, totalWaves, dayNumber);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}
