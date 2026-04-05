package com.sevendaystominecraft.perk;

public enum Attribute {
    STRENGTH("STR", "Strength"),
    PERCEPTION("PER", "Perception"),
    FORTITUDE("FOR", "Fortitude"),
    AGILITY("AGI", "Agility"),
    INTELLECT("INT", "Intellect");

    private final String shortName;
    private final String displayName;

    Attribute(String shortName, String displayName) {
        this.shortName = shortName;
        this.displayName = displayName;
    }

    public String getShortName() { return shortName; }
    public String getDisplayName() { return displayName; }

    public static Attribute fromShortName(String shortName) {
        for (Attribute attr : values()) {
            if (attr.shortName.equalsIgnoreCase(shortName)) {
                return attr;
            }
        }
        return null;
    }
}
