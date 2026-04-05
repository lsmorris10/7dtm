package com.sevendaystominecraft.heatmap;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HeatmapConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class HeatmapManager {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel().dimension() != Level.OVERWORLD) return;
        if (!HeatmapConfig.INSTANCE.enabled.get()) return;

        ServerLevel level = (ServerLevel) event.getLevel();

        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;

            HeatmapData data = HeatmapData.getOrCreate(level);
            data.tickDecay();

            HeatmapSpawner.tick(level, data);
        }
    }

    public static void addHeat(ServerLevel level, ChunkPos center, float amount, int radiusChunks, float decayPerMinute) {
        if (!HeatmapConfig.INSTANCE.enabled.get()) return;

        HeatmapData data = HeatmapData.getOrCreate(level);
        data.addHeatWithRadius(center, amount, radiusChunks, decayPerMinute);

        float totalHeat = data.getHeat(center);
        if (totalHeat >= 25) {
            SevenDaysToMinecraft.LOGGER.debug("[BZHS Heatmap] Chunk ({}, {}) heat: {}",
                    center.x, center.z, String.format("%.1f", totalHeat));
        }
    }
}
