package com.sevendaystominecraft.block.loot;

public enum LootContainerType {
    TRASH_PILE("trash_pile", "Trash Pile", 5, 9),
    CARDBOARD_BOX("cardboard_box", "Cardboard Box", 5, 9),
    GUN_SAFE("gun_safe", "Gun Safe", 7, 18),
    MUNITIONS_BOX("munitions_box", "Munitions Box", 7, 18),
    SUPPLY_CRATE("supply_crate", "Supply Crate", 0, 27),
    KITCHEN_CABINET("kitchen_cabinet", "Kitchen Cabinet", 5, 9),
    MEDICINE_CABINET("medicine_cabinet", "Medicine Cabinet", 5, 9),
    BOOKSHELF("bookshelf", "Bookshelf", 5, 9);

    private final String id;
    private final String displayName;
    private final int defaultRespawnDays;
    private final int slotCount;

    LootContainerType(String id, String displayName, int defaultRespawnDays, int slotCount) {
        this.id = id;
        this.displayName = displayName;
        this.defaultRespawnDays = defaultRespawnDays;
        this.slotCount = slotCount;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getDefaultRespawnDays() { return defaultRespawnDays; }
    public int getSlotCount() { return slotCount; }
}
