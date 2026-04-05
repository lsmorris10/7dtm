package com.sevendaystominecraft.horde;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class DayCycleHandler {

    private static int tickCounter = 0;
    private static boolean initialTimeSet = false;
    private static boolean sleepHandled = false;

    private static final long MORNING_START_TIME = 1000L;

    @SubscribeEvent
    public static void onLevelTickPre(LevelTickEvent.Pre event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel().dimension() != Level.OVERWORLD) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        List<ServerPlayer> players = level.players();

        if (players.isEmpty()) {
            sleepHandled = false;
            return;
        }

        boolean anyoneSleeping = players.stream().anyMatch(ServerPlayer::isSleeping);
        if (!anyoneSleeping) {
            sleepHandled = false;
            return;
        }

        if (sleepHandled) return;

        boolean allSleepingLongEnough = players.stream().allMatch(p ->
                !p.isSleeping() || p.isSleepingLongEnough());
        if (!allSleepingLongEnough) return;

        long currentTime = level.getDayTime();
        long timeOfDay = currentTime % SevenDaysConstants.DAY_LENGTH;

        if (timeOfDay >= SevenDaysConstants.NIGHT_END) {
            sleepHandled = true;
            // Wake all players up even if already morning
            for (ServerPlayer sp : players) {
                if (sp.isSleeping()) {
                    sp.stopSleeping();
                }
            }
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Sleep completed — already at morning (timeOfDay={}), no time advance needed", timeOfDay);
            return;
        }

        long dayBase = currentTime - timeOfDay;
        long targetTime = dayBase + SevenDaysConstants.NIGHT_END;
        level.setDayTime(targetTime);
        sleepHandled = true;

        // Wake all players up after advancing time
        for (ServerPlayer sp : players) {
            if (sp.isSleeping()) {
                sp.stopSleeping();
            }
        }

        // Reset weather on sleep like vanilla does
        level.setWeatherParameters(0, 0, false, false);

        SevenDaysToMinecraft.LOGGER.info("[BZHS] Sleep completed — advanced time from {} to {} (morning of day {})",
                currentTime, targetTime, BloodMoonTracker.calculateGameDay(level));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel().dimension() != Level.OVERWORLD) return;

        ServerLevel level = (ServerLevel) event.getLevel();

        if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Disabled vanilla daylight cycle — using TIME_SCALE={} slower-tick day cycle", SevenDaysConstants.TIME_SCALE);
        }

        if (!initialTimeSet && level.getDayTime() == 0) {
            level.setDayTime(MORNING_START_TIME);
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Set initial world time to {} (clear morning) to avoid sunrise red sky", MORNING_START_TIME);
        }
        initialTimeSet = true;

        tickCounter++;
        if (tickCounter >= SevenDaysConstants.TIME_SCALE) {
            tickCounter = 0;
            level.setDayTime(level.getDayTime() + 1);
        }
    }

    public static void reset() {
        initialTimeSet = false;
        tickCounter = 0;
        sleepHandled = false;
    }
}
