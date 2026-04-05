package com.sevendaystominecraft.item.armor;

public enum ArmorTier {
    LIGHT("light", "Padded", 0.0f, 0.10f),
    MEDIUM("medium", "Scrap Iron", -0.05f, 0.0f),
    HEAVY("heavy", "Military", -0.15f, 0.0f);

    private final String id;
    private final String displayName;
    private final float movementModifier;
    private final float stealthReductionPerPiece;

    ArmorTier(String id, String displayName, float movementModifier, float stealthReductionPerPiece) {
        this.id = id;
        this.displayName = displayName;
        this.movementModifier = movementModifier;
        this.stealthReductionPerPiece = stealthReductionPerPiece;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public float getMovementModifier() { return movementModifier; }
    public float getStealthReductionPerPiece() { return stealthReductionPerPiece; }
}
