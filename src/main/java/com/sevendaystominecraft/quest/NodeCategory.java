package com.sevendaystominecraft.quest;

public enum NodeCategory {
    SCAVENGE("Scavenge", 0xFFBB8844),
    WATER("Water", 0xFF4488FF),
    FOOD("Food", 0xFF44BB44),
    TOOL("Tool", 0xFFCCCC44),
    WEAPON("Weapon", 0xFFFF4444),
    SHELTER("Shelter", 0xFF8866CC),
    MEDICAL("Medical", 0xFFFF66AA);

    private final String displayName;
    private final int color;

    NodeCategory(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public int getColor() { return color; }
}
