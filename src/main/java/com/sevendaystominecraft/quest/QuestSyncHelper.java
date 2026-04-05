package com.sevendaystominecraft.quest;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.network.SyncQuestPayload;
import com.sevendaystominecraft.network.SyncQuestPayload.QuestEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class QuestSyncHelper {

    public static void syncQuests(ServerPlayer player) {
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        List<QuestEntry> entries = new ArrayList<>();

        for (QuestInstance quest : stats.getActiveQuests()) {
            QuestDefinition def = quest.getDefinition();
            BlockPos loc = def.getTargetLocation();
            boolean hasLoc = loc != null;

            entries.add(new QuestEntry(
                    quest.getQuestId(),
                    def.getType().name(),
                    def.getQuestName(),
                    def.getObjectiveDescription(),
                    def.getTargetCount(),
                    quest.getProgress(),
                    quest.getState().name(),
                    def.getRewardXp(),
                    def.getRewardTokens(),
                    def.getTraderId(),
                    hasLoc,
                    hasLoc ? loc.getX() : 0,
                    hasLoc ? loc.getY() : 0,
                    hasLoc ? loc.getZ() : 0
            ));
        }

        PacketDistributor.sendToPlayer(player, new SyncQuestPayload(entries, stats.getTrackedQuestId()));
    }
}
