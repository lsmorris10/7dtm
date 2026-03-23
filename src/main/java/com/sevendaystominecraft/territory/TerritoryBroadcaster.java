package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.TerritoryConfig;
import com.sevendaystominecraft.network.SyncTerritoryPayload;
import com.sevendaystominecraft.network.SyncTerritoryPayload.BuildingEntry;
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
    private static final double BUILDING_TRIGGER_RANGE = 12.0;

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
                checkSleeperAwakening(level, player, playerPos, record, entryRange);

                String label = record.getLabel();
                if (record.isCleared()) {
                    label = label + " [Cleared]";
                }

                List<BuildingEntry> buildingEntries = new ArrayList<>();
                List<BlockPos> centers = record.getBuildingCenters();
                List<String> typeNames = record.getBuildingTypeNames();
                for (int i = 0; i < centers.size(); i++) {
                    BlockPos pos = centers.get(i);
                    String displayName = (i < typeNames.size()) ? typeNames.get(i) : "";
                    buildingEntries.add(new BuildingEntry(pos.getX(), pos.getY(), pos.getZ(), displayName));
                }

                entries.add(new TerritoryEntry(
                        record.getId(),
                        record.getOrigin().getX(),
                        record.getOrigin().getY(),
                        record.getOrigin().getZ(),
                        record.getTier().getTier(),
                        label,
                        record.getType().getDisplayName(),
                        buildingEntries
                ));
            }

            PacketDistributor.sendToPlayer(player, new SyncTerritoryPayload(entries));
        }
    }

    private static void checkSleeperAwakening(ServerLevel level, ServerPlayer player,
                                               BlockPos playerPos, TerritoryRecord record,
                                               double entryRange) {
        if (record.isCleared()) return;
        if (record.getZombiesRemaining() <= 0) return;

        BlockPos origin = record.getOrigin();
        double dx = playerPos.getX() - origin.getX();
        double dz = playerPos.getZ() - origin.getZ();
        double distSq = dx * dx + dz * dz;

        if (distSq > entryRange * entryRange) return;

        List<BlockPos> buildingCenters = record.getBuildingCenters();
        if (buildingCenters.isEmpty()) {
            if (!record.isAwakened()) {
                SevenDaysToMinecraft.LOGGER.info(
                        "[BZHS Territory] Player {} entered territory #{} ({} {}) — awakening all sleeper zombies (legacy)",
                        player.getName().getString(), record.getId(),
                        record.getType().getDisplayName(), record.getTier().getStars());

                com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                        com.sevendaystominecraft.sound.ModSounds.TERRITORY_ENTER, player,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);

                SleeperZombieManager.awakenSleepers(level, record);
                record.setAwakened(true);
                TerritoryData.getOrCreate(level).markDirtyRecord();
            }
            return;
        }

        boolean anyAwakened = false;
        for (int i = 0; i < buildingCenters.size(); i++) {
            if (record.isBuildingAwakened(i)) continue;

            BlockPos buildingCenter = buildingCenters.get(i);
            double bdx = playerPos.getX() - buildingCenter.getX();
            double bdz = playerPos.getZ() - buildingCenter.getZ();
            double bDistSq = bdx * bdx + bdz * bdz;

            if (bDistSq <= BUILDING_TRIGGER_RANGE * BUILDING_TRIGGER_RANGE) {
                SevenDaysToMinecraft.LOGGER.info(
                        "[BZHS Territory] Player {} entered building #{} of territory #{} — awakening sleepers",
                        player.getName().getString(), i, record.getId());

                if (!anyAwakened) {
                    com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                            com.sevendaystominecraft.sound.ModSounds.TERRITORY_ENTER, player,
                            net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
                }

                SleeperZombieManager.awakenSleepersForBuilding(level, record, i);
                record.setBuildingAwakened(i);
                anyAwakened = true;
            }
        }

        if (anyAwakened) {
            TerritoryData.getOrCreate(level).markDirtyRecord();
        }
    }
}
