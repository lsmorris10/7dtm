package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.TerritoryConfig;
import com.sevendaystominecraft.network.SyncTerritoryPayload;
import com.sevendaystominecraft.network.SyncTerritoryPayload.TerritoryEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class TerritoryBroadcaster {

    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 60;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel().dimension() != Level.OVERWORLD) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        ServerLevel level = (ServerLevel) event.getLevel();
        TerritoryData data = TerritoryData.getOrCreate(level);

        double syncRange = TerritoryConfig.INSTANCE.syncRangeBlocks.get();
        double entryRange = TerritoryConfig.INSTANCE.entryTriggerRangeBlocks.get();

        for (ServerPlayer player : level.players()) {
            BlockPos playerPos = player.blockPosition();
            List<TerritoryRecord> nearby = data.getNearby(playerPos, syncRange);

            List<TerritoryEntry> entries = new ArrayList<>(nearby.size());
            for (TerritoryRecord record : nearby) {
                checkEntryTrigger(level, player, playerPos, record, data, entryRange);

                entries.add(new TerritoryEntry(
                        record.getId(),
                        record.getOrigin().getX(),
                        record.getOrigin().getY(),
                        record.getOrigin().getZ(),
                        record.getTier().getTier(),
                        record.getLabel()
                ));
            }

            PacketDistributor.sendToPlayer(player, new SyncTerritoryPayload(entries));
        }
    }

    private static void checkEntryTrigger(ServerLevel level, ServerPlayer player,
                                           BlockPos playerPos, TerritoryRecord record,
                                           TerritoryData data, double entryRange) {
        if (record.isCleared()) return;
        if (record.getZombiesRemaining() > 0) return;

        BlockPos origin = record.getOrigin();
        double dx = playerPos.getX() - origin.getX();
        double dz = playerPos.getZ() - origin.getZ();
        double distSq = dx * dx + dz * dz;

        if (distSq > entryRange * entryRange) return;

        SevenDaysToMinecraft.LOGGER.info(
                "[BZHS Territory] Player {} entered territory #{} ({} {}) — spawning zombies",
                player.getName().getString(), record.getId(),
                record.getType().getDisplayName(), record.getTier().getStars());

        List<BlockPos> spawnPositions = TerritoryStructureBuilder
                .generateInteriorSpawnPositions(origin, record.getTier());
        TerritoryZombieSpawner.populate(level, record, spawnPositions);
        data.markDirtyRecord();
    }
}
