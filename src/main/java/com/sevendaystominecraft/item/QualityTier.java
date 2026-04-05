package com.sevendaystominecraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public enum QualityTier {
    POOR(1, "Poor", ChatFormatting.GRAY, 0.7f, 1, false, false, "", ""),
    GOOD(2, "Good", ChatFormatting.GOLD, 0.85f, 1, false, false, "", ""),
    GREAT(3, "Great", ChatFormatting.YELLOW, 1.0f, 2, true, false, "", ""),
    SUPERIOR(4, "Superior", ChatFormatting.GREEN, 1.15f, 2, true, true, "", ""),
    EXCELLENT(5, "Excellent", ChatFormatting.BLUE, 1.3f, 3, true, true, "", ""),
    LEGENDARY(6, "Legendary", ChatFormatting.LIGHT_PURPLE, 1.5f, 4, true, true, "\u2726 ", " \u2726");

    private final int level;
    private final String displayName;
    private final ChatFormatting color;
    private final float statMultiplier;
    private final int modSlots;
    private final boolean bold;
    private final boolean italic;
    private final String prefix;
    private final String suffix;

    QualityTier(int level, String displayName, ChatFormatting color, float statMultiplier, int modSlots,
                boolean bold, boolean italic, String prefix, String suffix) {
        this.level = level;
        this.displayName = displayName;
        this.color = color;
        this.statMultiplier = statMultiplier;
        this.modSlots = modSlots;
        this.bold = bold;
        this.italic = italic;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    public ChatFormatting getColor() { return color; }
    public float getStatMultiplier() { return statMultiplier; }
    public int getModSlots() { return modSlots; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public String getPrefix() { return prefix; }
    public String getSuffix() { return suffix; }

    public MutableComponent applyToName(Component originalName) {
        Style style = Style.EMPTY.withColor(color);
        if (bold) {
            style = style.withBold(true);
        }
        if (italic) {
            style = style.withItalic(true);
        }
        String plainName = originalName.getString();
        String styledText = prefix + plainName + suffix;
        return Component.literal(styledText).setStyle(style);
    }

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
