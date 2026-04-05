package com.sevendaystominecraft.heatmap;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HeatmapConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class HeatEventHandler {

    private static final Map<UUID, Integer> sprintTickCounters = new HashMap<>();

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!HeatmapConfig.INSTANCE.enabled.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);

        HeatmapManager.addHeat(level, chunkPos, 0.5f, 3, 2.0f);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!HeatmapConfig.INSTANCE.enabled.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Player)) return;

        boolean isTorch = event.getPlacedBlock().getBlock() instanceof TorchBlock
                || event.getPlacedBlock().getBlock() instanceof WallTorchBlock;

        if (isTorch) {
            BlockPos pos = event.getPos();
            ChunkPos chunkPos = new ChunkPos(pos);
            HeatmapManager.addHeat(level, chunkPos, 2.0f, 1, 1.0f);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!HeatmapConfig.INSTANCE.enabled.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        var explosion = event.getExplosion();
        double x = explosion.center().x;
        double z = explosion.center().z;
        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) x, 0, (int) z));

        HeatmapManager.addHeat(level, chunkPos, 25.0f, 6, 2.0f);
    }

    public static void onPlayerSprint(ServerLevel level, Player player) {
        if (!HeatmapConfig.INSTANCE.enabled.get()) return;

        UUID playerId = player.getUUID();
        int ticks = sprintTickCounters.getOrDefault(playerId, 0) + 1;

        if (ticks >= 20) {
            ChunkPos chunkPos = new ChunkPos(player.blockPosition());
            HeatmapManager.addHeat(level, chunkPos, 0.2f, 2, 3.0f);
            ticks = 0;
        }

        sprintTickCounters.put(playerId, ticks);
    }

    public static void clearSprintCounters() {
        sprintTickCounters.clear();
    }
}
