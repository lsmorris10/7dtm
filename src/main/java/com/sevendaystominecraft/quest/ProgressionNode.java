package com.sevendaystominecraft.quest;

import java.util.Collections;
import java.util.List;

public class ProgressionNode {

    private final String id;
    private final ProgressionStage stage;
    private final NodeCategory category;
    private final String displayName;
    private final String description;
    private final String iconItemId;
    private final int rewardXp;
    private final int rewardTokens;
    private final List<String> prerequisiteIds;

    // Layout position for rendering (grid coordinates, not pixels)
    private final int gridX;
    private final int gridY;

    public ProgressionNode(String id, ProgressionStage stage, NodeCategory category,
                           String displayName, String description, String iconItemId,
                           int rewardXp, int rewardTokens, List<String> prerequisiteIds,
                           int gridX, int gridY) {
        this.id = id;
        this.stage = stage;
        this.category = category;
        this.displayName = displayName;
        this.description = description;
        this.iconItemId = iconItemId;
        this.rewardXp = rewardXp;
        this.rewardTokens = rewardTokens;
        this.prerequisiteIds = prerequisiteIds != null ? List.copyOf(prerequisiteIds) : Collections.emptyList();
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public String getId() { return id; }
    public ProgressionStage getStage() { return stage; }
    public NodeCategory getCategory() { return category; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIconItemId() { return iconItemId; }
    public int getRewardXp() { return rewardXp; }
    public int getRewardTokens() { return rewardTokens; }
    public List<String> getPrerequisiteIds() { return prerequisiteIds; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }

    public String getRewardText() {
        StringBuilder sb = new StringBuilder();
        if (rewardXp > 0) sb.append("+").append(rewardXp).append(" XP");
        if (rewardTokens > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("+").append(rewardTokens).append(" Coins");
        }
        return sb.toString();
    }
}
