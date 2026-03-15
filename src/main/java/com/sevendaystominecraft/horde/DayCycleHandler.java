package com.sevendaystominecraft.horde;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class DayCycleHandler {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel().dimension() != Level.OVERWORLD) return;

        ServerLevel level = (ServerLevel) event.getLevel();

        if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Disabled vanilla daylight cycle — using TIME_SCALE={} slower-tick day cycle", SevenDaysConstants.TIME_SCALE);
        }

        tickCounter++;
        if (tickCounter >= SevenDaysConstants.TIME_SCALE) {
            tickCounter = 0;
            level.setDayTime(level.getDayTime() + 1);
        }
    }
}
