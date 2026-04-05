package com.sevendaystominecraft.perk;

public class PerkDefinition {

    private final String id;
    private final String displayName;
    private final Attribute attribute;
    private final int maxRank;
    private final int[] attributeRequirements;
    private final String description;
    private final boolean isMastery;

    public PerkDefinition(String id, String displayName, Attribute attribute,
                          int maxRank, int[] attributeRequirements, String description,
                          boolean isMastery) {
        this.id = id;
        this.displayName = displayName;
        this.attribute = attribute;
        this.maxRank = maxRank;
        this.attributeRequirements = attributeRequirements;
        this.description = description;
        this.isMastery = isMastery;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Attribute getAttribute() { return attribute; }
    public int getMaxRank() { return maxRank; }
    public String getDescription() { return description; }
    public boolean isMastery() { return isMastery; }

    public int getAttributeRequirement(int rank) {
        if (rank < 1 || rank > maxRank) return Integer.MAX_VALUE;
        return attributeRequirements[rank - 1];
    }

    public boolean canUnlock(int rank, int currentAttributeLevel) {
        if (rank < 1 || rank > maxRank) return false;
        return currentAttributeLevel >= getAttributeRequirement(rank);
    }
}
