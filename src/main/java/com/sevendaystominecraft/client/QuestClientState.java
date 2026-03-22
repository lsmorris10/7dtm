package com.sevendaystominecraft.client;

import com.sevendaystominecraft.network.SyncQuestPayload.QuestEntry;
import com.sevendaystominecraft.network.SyncTraderQuestsPayload.TraderQuestEntry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestClientState {

    private static final List<QuestEntry> activeQuests = new CopyOnWriteArrayList<>();
    private static final List<TraderQuestEntry> traderQuests = new CopyOnWriteArrayList<>();
    private static int currentTraderId = -1;
    private static String trackedQuestId = "";

    public static void updateActiveQuests(List<QuestEntry> entries, String tracked) {
        activeQuests.clear();
        activeQuests.addAll(entries);
        trackedQuestId = tracked != null ? tracked : "";
    }

    public static List<QuestEntry> getActiveQuests() {
        return Collections.unmodifiableList(activeQuests);
    }

    public static String getTrackedQuestId() {
        return trackedQuestId;
    }

    public static QuestEntry getTrackedQuest() {
        if (trackedQuestId.isEmpty()) return null;
        for (QuestEntry q : activeQuests) {
            if (q.questId().equals(trackedQuestId)) return q;
        }
        return null;
    }

    public static void updateTraderQuests(int traderId, List<TraderQuestEntry> entries) {
        currentTraderId = traderId;
        traderQuests.clear();
        traderQuests.addAll(entries);
    }

    public static List<TraderQuestEntry> getTraderQuests() {
        return Collections.unmodifiableList(traderQuests);
    }

    public static int getCurrentTraderId() {
        return currentTraderId;
    }

    public static void clear() {
        activeQuests.clear();
        traderQuests.clear();
        currentTraderId = -1;
        trackedQuestId = "";
    }

    public static void reset() {
        clear();
    }
}
