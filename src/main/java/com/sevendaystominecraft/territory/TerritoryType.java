package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.block.loot.LootContainerType;

public enum TerritoryType {

    RESIDENTIAL("Abandoned House",       LootContainerType.KITCHEN_CABINET, LootContainerType.BOOKSHELF),
    COMMERCIAL("Derelict Store",         LootContainerType.CARDBOARD_BOX, LootContainerType.SUPPLY_CRATE),
    INDUSTRIAL("Ruined Factory",         LootContainerType.SUPPLY_CRATE, LootContainerType.MUNITIONS_BOX),
    MILITARY("Military Bunker",          LootContainerType.MUNITIONS_BOX, LootContainerType.GUN_SAFE),
    WILDERNESS("Wilderness Camp",        LootContainerType.TRASH_PILE, LootContainerType.CARDBOARD_BOX),
    MEDICAL("Abandoned Clinic",          LootContainerType.MEDICINE_CABINET, LootContainerType.SUPPLY_CRATE);

    private final String displayName;
    private final LootContainerType primaryLoot;
    private final LootContainerType secondaryLoot;

    TerritoryType(String displayName, LootContainerType primary, LootContainerType secondary) {
        this.displayName = displayName;
        this.primaryLoot = primary;
        this.secondaryLoot = secondary;
    }

    public String getDisplayName() { return displayName; }
    public LootContainerType getPrimaryLoot() { return primaryLoot; }
    public LootContainerType getSecondaryLoot() { return secondaryLoot; }

    public static TerritoryType random(net.minecraft.util.RandomSource random) {
        TerritoryType[] values = values();
        return values[random.nextInt(values.length)];
    }
}
