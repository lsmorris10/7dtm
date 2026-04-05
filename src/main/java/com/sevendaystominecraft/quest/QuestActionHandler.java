package com.sevendaystominecraft.quest;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.config.QuestConfig;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.network.QuestActionPayload;
import com.sevendaystominecraft.network.SyncTraderQuestsPayload;
import com.sevendaystominecraft.network.SyncTraderQuestsPayload.TraderQuestEntry;
import com.sevendaystominecraft.perk.LevelManager;
import com.sevendaystominecraft.trader.TraderData;
import com.sevendaystominecraft.trader.TraderRecord;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class QuestActionHandler {

    public static void handleAction(ServerPlayer player, QuestActionPayload payload) {
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        switch (payload.action()) {
            case QuestActionPayload.ACTION_ACCEPT -> handleAccept(player, stats, payload);
            case QuestActionPayload.ACTION_ABANDON -> handleAbandon(player, stats, payload);
            case QuestActionPayload.ACTION_TURN_IN -> handleTurnIn(player, stats, payload);
            case QuestActionPayload.ACTION_TRACK -> handleTrack(player, stats, payload);
        }
    }

    private static boolean isPlayerNearTrader(ServerPlayer player, TraderRecord record) {
        BlockPos traderPos = record.getOrigin();
        double distSq = player.blockPosition().distSqr(traderPos);
        return distSq <= 100;
    }

    private static void handleAccept(ServerPlayer player, SevenDaysPlayerStats stats, QuestActionPayload payload) {
        int maxQuests = QuestConfig.INSTANCE.maxActiveQuests.get();
        if (stats.getActiveQuests().size() >= maxQuests) return;

        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        TraderData traderData = TraderData.getOrCreate(serverLevel);
        TraderRecord record = traderData.getTraderById(payload.traderId());
        if (record == null) return;

        if (!isPlayerNearTrader(player, record)) return;

        List<QuestDefinition> available = record.getAvailableQuests(serverLevel);
        QuestDefinition target = null;
        for (QuestDefinition def : available) {
            if (generateQuestId(def).equals(payload.questId())) {
                target = def;
                break;
            }
        }

        if (target == null) return;

        String questGenId = generateQuestId(target);
        if (stats.isQuestConsumed(questGenId, record.getLastRestockDay())) return;

        for (QuestInstance existing : stats.getActiveQuests()) {
            if (existing.getDefinition().getObjectiveDescription().equals(target.getObjectiveDescription())
                    && existing.getDefinition().getTraderId() == target.getTraderId()) {
                return;
            }
        }

        stats.markQuestConsumed(questGenId, record.getLastRestockDay());

        if (target.getType() == QuestType.BURIED_TREASURE && target.getTargetLocation() != null) {
            BlockPos cachePos = QuestProgressHandler.getCachePos(serverLevel, target.getTargetLocation());
            target = new QuestDefinition(
                    target.getType(), target.getObjectiveDescription(), target.getTargetCount(),
                    target.getRewardXp(), target.getRewardTokens(), target.getTargetId(),
                    cachePos, target.getTraderId(), target.getTraderTier());
            QuestProgressHandler.placeSupplyCache(serverLevel, target.getTargetLocation());
        }

        QuestInstance quest = new QuestInstance(target);
        stats.addQuest(quest);
        if (stats.getTrackedQuestId().isEmpty()) {
            stats.setTrackedQuestId(quest.getQuestId());
        }

        QuestSyncHelper.syncQuests(player);
        syncTraderQuests(player, payload.traderId());
    }

    private static void handleAbandon(ServerPlayer player, SevenDaysPlayerStats stats, QuestActionPayload payload) {
        if (payload.questId().equals(stats.getTrackedQuestId())) {
            stats.setTrackedQuestId("");
        }
        stats.removeQuest(payload.questId());
        QuestSyncHelper.syncQuests(player);
    }

    private static void handleTrack(ServerPlayer player, SevenDaysPlayerStats stats, QuestActionPayload payload) {
        QuestInstance quest = stats.getQuestById(payload.questId());
        if (quest == null) return;
        stats.setTrackedQuestId(payload.questId());
        QuestSyncHelper.syncQuests(player);
    }

    private static void handleTurnIn(ServerPlayer player, SevenDaysPlayerStats stats, QuestActionPayload payload) {
        QuestInstance quest = stats.getQuestById(payload.questId());
        if (quest == null) return;
        if (quest.getState() != QuestInstance.State.READY_TO_TURN_IN) return;

        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        TraderData traderData = TraderData.getOrCreate(serverLevel);
        TraderRecord record = traderData.getTraderById(quest.getDefinition().getTraderId());
        if (record == null || !isPlayerNearTrader(player, record)) return;

        QuestDefinition def = quest.getDefinition();

        if (def.getType() == QuestType.FETCH_DELIVER) {
            ItemStack targetItem = QuestGenerator.getItemForFetchQuest(def.getTargetId());
            if (!targetItem.isEmpty()) {
                int needed = def.getTargetCount();
                int available = 0;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.getItem() == targetItem.getItem()) {
                        available += stack.getCount();
                    }
                }
                if (available < needed) return;

                int remaining = needed;
                for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.getItem() == targetItem.getItem()) {
                        int take = Math.min(remaining, stack.getCount());
                        stack.shrink(take);
                        if (stack.isEmpty()) {
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                        }
                        remaining -= take;
                    }
                }
            }
        }

        quest.markCompleted();

        com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                com.sevendaystominecraft.sound.ModSounds.QUEST_COMPLETE, player,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.5f, 1.0f);

        int rewardXp = def.getRewardXp();
        int tokens = def.getRewardTokens();

        int boldExplorerRank = stats.getPerkRank("bold_explorer");
        if (boldExplorerRank > 0) {
            float xpBonus = 1.0f + (0.10f * boldExplorerRank);
            rewardXp = Math.round(rewardXp * xpBonus);
            float tokenBonus = 1.0f + (0.10f * boldExplorerRank);
            tokens = Math.round(tokens * tokenBonus);
        }

        int treasureHunterRank = stats.getPerkRank("treasure_hunter");
        if (treasureHunterRank > 0 && def.getType() == QuestType.BURIED_TREASURE) {
            float lootBonus = 1.0f + (0.15f * treasureHunterRank);
            tokens = Math.round(tokens * lootBonus);
        }

        LevelManager.awardXp(player, rewardXp);

        if (tokens > 0) {
            ItemStack tokenStack = new ItemStack(ModItems.SURVIVORS_COIN.get(), tokens);
            if (!player.getInventory().add(tokenStack)) {
                player.drop(tokenStack, false);
            }
        }

        if (quest.getQuestId().equals(stats.getTrackedQuestId())) {
            stats.setTrackedQuestId("");
        }
        stats.removeQuest(quest.getQuestId());

        if (stats.getTrackedQuestId().isEmpty() && !stats.getActiveQuests().isEmpty()) {
            stats.setTrackedQuestId(stats.getActiveQuests().get(0).getQuestId());
        }

        SevenDaysToMinecraft.LOGGER.info("[BZHS] {} completed quest: {} — awarded {} XP and {} tokens",
                player.getName().getString(), def.getQuestName(), def.getRewardXp(), def.getRewardTokens());

        QuestSyncHelper.syncQuests(player);
    }

    public static void syncTraderQuests(ServerPlayer player, int traderId) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        TraderData traderData = TraderData.getOrCreate(serverLevel);
        TraderRecord record = traderData.getTraderById(traderId);
        if (record == null) return;

        List<QuestDefinition> available = record.getAvailableQuests(serverLevel);
        List<TraderQuestEntry> entries = new ArrayList<>();

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        for (QuestDefinition def : available) {
            String questGenId = generateQuestId(def);

            if (stats.isQuestConsumed(questGenId, record.getLastRestockDay())) continue;

            boolean alreadyAccepted = false;
            for (QuestInstance existing : stats.getActiveQuests()) {
                if (existing.getDefinition().getObjectiveDescription().equals(def.getObjectiveDescription())
                        && existing.getDefinition().getTraderId() == def.getTraderId()) {
                    alreadyAccepted = true;
                    break;
                }
            }
            if (alreadyAccepted) continue;

            BlockPos loc = def.getTargetLocation();
            boolean hasLoc = loc != null;

            entries.add(new TraderQuestEntry(
                    generateQuestId(def),
                    def.getType().name(),
                    def.getQuestName(),
                    def.getObjectiveDescription(),
                    def.getTargetCount(),
                    def.getRewardXp(),
                    def.getRewardTokens(),
                    hasLoc,
                    hasLoc ? loc.getX() : 0,
                    hasLoc ? loc.getY() : 0,
                    hasLoc ? loc.getZ() : 0
            ));
        }

        PacketDistributor.sendToPlayer(player, new SyncTraderQuestsPayload(traderId, entries));
    }

    public static void removeQuestsForDeadTrader(ServerLevel level, int traderId, String traderName) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            List<String> removedQuestIds = new ArrayList<>();
            for (QuestInstance quest : new ArrayList<>(stats.getActiveQuests())) {
                if (quest.getDefinition().getTraderId() == traderId) {
                    removedQuestIds.add(quest.getQuestId());
                }
            }
            if (!removedQuestIds.isEmpty()) {
                for (String questId : removedQuestIds) {
                    if (questId.equals(stats.getTrackedQuestId())) {
                        stats.setTrackedQuestId("");
                    }
                    stats.removeQuest(questId);
                }
                // Re-track another quest if needed
                if (stats.getTrackedQuestId().isEmpty() && !stats.getActiveQuests().isEmpty()) {
                    stats.setTrackedQuestId(stats.getActiveQuests().get(0).getQuestId());
                }
                QuestSyncHelper.syncQuests(player);
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(
                                "\u00a7c" + traderName + " has died! " + removedQuestIds.size() +
                                " quest(s) from this trader have been removed."));
                SevenDaysToMinecraft.LOGGER.info("[BZHS] Removed {} quests for dead trader {} (id={}) from player {}",
                        removedQuestIds.size(), traderName, traderId, player.getName().getString());
            }
        }
    }

    public static String generateQuestId(QuestDefinition def) {
        return Integer.toHexString((def.getObjectiveDescription() + def.getTraderId()).hashCode());
    }
}
