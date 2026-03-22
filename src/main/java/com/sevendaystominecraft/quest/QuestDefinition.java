package com.sevendaystominecraft.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class QuestDefinition {

    private final QuestType type;
    private final String objectiveDescription;
    private final int targetCount;
    private final int rewardXp;
    private final int rewardTokens;
    private final String targetId;
    private final BlockPos targetLocation;
    private final int traderId;
    private final int traderTier;

    public QuestDefinition(QuestType type, String objectiveDescription, int targetCount,
                           int rewardXp, int rewardTokens, String targetId,
                           BlockPos targetLocation, int traderId, int traderTier) {
        this.type = type;
        this.objectiveDescription = objectiveDescription;
        this.targetCount = targetCount;
        this.rewardXp = rewardXp;
        this.rewardTokens = rewardTokens;
        this.targetId = targetId;
        this.targetLocation = targetLocation;
        this.traderId = traderId;
        this.traderTier = traderTier;
    }

    public QuestType getType() { return type; }
    public String getObjectiveDescription() { return objectiveDescription; }
    public int getTargetCount() { return targetCount; }
    public int getRewardXp() { return rewardXp; }
    public int getRewardTokens() { return rewardTokens; }
    public String getTargetId() { return targetId; }
    public BlockPos getTargetLocation() { return targetLocation; }
    public int getTraderId() { return traderId; }
    public int getTraderTier() { return traderTier; }

    public String getQuestName() {
        return switch (type) {
            case CLEAR_TERRITORY -> "Clear Territory";
            case FETCH_DELIVER -> "Deliver " + targetCount + " " + targetId;
            case KILL_COUNT -> "Kill " + targetCount + " " + targetId;
            case BURIED_TREASURE -> "Buried Treasure";
        };
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.name());
        tag.putString("objective", objectiveDescription);
        tag.putInt("targetCount", targetCount);
        tag.putInt("rewardXp", rewardXp);
        tag.putInt("rewardTokens", rewardTokens);
        tag.putString("targetId", targetId != null ? targetId : "");
        if (targetLocation != null) {
            tag.putInt("locX", targetLocation.getX());
            tag.putInt("locY", targetLocation.getY());
            tag.putInt("locZ", targetLocation.getZ());
            tag.putBoolean("hasLocation", true);
        } else {
            tag.putBoolean("hasLocation", false);
        }
        tag.putInt("traderId", traderId);
        tag.putInt("traderTier", traderTier);
        return tag;
    }

    public static QuestDefinition load(CompoundTag tag) {
        QuestType type;
        try {
            type = QuestType.valueOf(tag.getString("type"));
        } catch (IllegalArgumentException e) {
            type = QuestType.KILL_COUNT;
        }
        String objective = tag.getString("objective");
        int targetCount = tag.getInt("targetCount");
        int rewardXp = tag.getInt("rewardXp");
        int rewardTokens = tag.getInt("rewardTokens");
        String targetId = tag.getString("targetId");
        if (targetId.isEmpty()) targetId = null;
        BlockPos location = null;
        if (tag.getBoolean("hasLocation")) {
            location = new BlockPos(tag.getInt("locX"), tag.getInt("locY"), tag.getInt("locZ"));
        }
        int traderId = tag.getInt("traderId");
        int traderTier = tag.getInt("traderTier");
        return new QuestDefinition(type, objective, targetCount, rewardXp, rewardTokens,
                targetId, location, traderId, traderTier);
    }
}
