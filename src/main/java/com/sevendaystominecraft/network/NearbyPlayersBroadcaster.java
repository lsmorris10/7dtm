package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.group.GroupData;
import com.sevendaystominecraft.heatmap.HeatmapData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class NearbyPlayersBroadcaster {

    private static final double NEARBY_RANGE = 200.0;
    private static final double NEARBY_RANGE_SQ = NEARBY_RANGE * NEARBY_RANGE;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel().dimension() != Level.OVERWORLD) return;

        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        ServerLevel overworldLevel = ((ServerLevel) event.getLevel()).getServer().getLevel(Level.OVERWORLD);
        if (overworldLevel == null) return;
        GroupData groupData = GroupData.getOrCreate(overworldLevel);

        List<ServerPlayer> allPlayers = overworldLevel.getServer().getPlayerList().getPlayers();

        for (ServerPlayer recipient : allPlayers) {
            List<SyncNearbyPlayersPayload.NearbyPlayerEntry> entries = new ArrayList<>();
            double rx = recipient.getX();
            double rz = recipient.getZ();

            Set<UUID> groupMemberUUIDs = groupData.getGroupMemberUUIDs(recipient.getUUID());
            for (ServerPlayer other : allPlayers) {
                if (other == recipient) continue;
                UUID otherUUID = other.getUUID();

                boolean sameDimension = other.level().dimension() == recipient.level().dimension();
                boolean isGroupMember = groupMemberUUIDs.contains(otherUUID);

                if (sameDimension) {
                    double dx = other.getX() - rx;
                    double dz = other.getZ() - rz;
                    boolean isNearby = dx * dx + dz * dz <= NEARBY_RANGE_SQ;

                    if (isNearby || isGroupMember) {
                        entries.add(new SyncNearbyPlayersPayload.NearbyPlayerEntry(
                                other.getName().getString(), other.getX(), other.getZ(), isGroupMember));
                    }
                } else if (isGroupMember) {
                    entries.add(new SyncNearbyPlayersPayload.NearbyPlayerEntry(
                            other.getName().getString(), other.getX(), other.getZ(), true));
                }
            }

            PacketDistributor.sendToPlayer(recipient, new SyncNearbyPlayersPayload(entries));

            ServerLevel recipientLevel = (ServerLevel) recipient.level();
            ChunkPos chunkPos = new ChunkPos(recipient.blockPosition());
            HeatmapData data = HeatmapData.getOrCreate(recipientLevel);
            float heat = data.getHeat(chunkPos);
            PacketDistributor.sendToPlayer(recipient, new SyncChunkHeatPayload(heat));
        }
    }
}
