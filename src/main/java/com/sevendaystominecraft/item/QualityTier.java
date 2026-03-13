package com.sevendaystominecraft.item;

import net.minecraft.ChatFormatting;

public enum QualityTier {
    POOR(1, "Poor", ChatFormatting.GRAY, 0.7f, 1),
    GOOD(2, "Good", ChatFormatting.GOLD, 0.85f, 1),
    GREAT(3, "Great", ChatFormatting.YELLOW, 1.0f, 2),
    SUPERIOR(4, "Superior", ChatFormatting.GREEN, 1.15f, 2),
    EXCELLENT(5, "Excellent", ChatFormatting.BLUE, 1.3f, 3),
    LEGENDARY(6, "Legendary", ChatFormatting.LIGHT_PURPLE, 1.5f, 4);

    private final int level;
    private final String displayName;
    private final ChatFormatting color;
    private final float statMultiplier;
    private final int modSlots;

    QualityTier(int level, String displayName, ChatFormatting color, float statMultiplier, int modSlots) {
        this.level = level;
        this.displayName = displayName;
        this.color = color;
        this.statMultiplier = statMultiplier;
        this.modSlots = modSlots;
    }

    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    public ChatFormatting getColor() { return color; }
    public float getStatMultiplier() { return statMultiplier; }
    public int getModSlots() { return modSlots; }

    public static QualityTier fromLevel(int level) {
        for (QualityTier tier : values()) {
            if (tier.level == level) return tier;
        }
        return POOR;
    }

    public static QualityTier fromLootStage(int lootStage) {
        if (lootStage >= 100) return LEGENDARY;
        if (lootStage >= 51) return EXCELLENT;
        if (lootStage >= 26) return SUPERIOR;
        if (lootStage >= 11) return GREAT;
        if (lootStage >= 5) return GOOD;
        return POOR;
    }

    public static QualityTier randomForLootStage(int lootStage, java.util.Random random) {
        QualityTier max = fromLootStage(lootStage);
        QualityTier[] values = values();
        int maxIdx = max.ordinal();
        int minIdx = Math.max(0, maxIdx - 2);
        int idx = minIdx + random.nextInt(maxIdx - minIdx + 1);
        return values[idx];
    }
}
