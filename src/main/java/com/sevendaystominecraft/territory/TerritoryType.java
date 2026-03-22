package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.block.loot.LootContainerType;

public enum TerritoryType {

    RESIDENTIAL("Abandoned Neighborhood", LootContainerType.KITCHEN_CABINET, LootContainerType.BOOKSHELF),
    COMMERCIAL("Derelict Store",         LootContainerType.CARDBOARD_BOX, LootContainerType.SUPPLY_CRATE),
    INDUSTRIAL("Ruined Factory",         LootContainerType.SUPPLY_CRATE, LootContainerType.MUNITIONS_BOX),
    MILITARY("Military Bunker",          LootContainerType.MUNITIONS_BOX, LootContainerType.GUN_SAFE),
    WILDERNESS("Wilderness Camp",        LootContainerType.TRASH_PILE, LootContainerType.CARDBOARD_BOX),
    MEDICAL("Abandoned Clinic",          LootContainerType.MEDICINE_CABINET, LootContainerType.SUPPLY_CRATE),
    CRACK_A_BOOK("Crack-a-Book",         LootContainerType.BOOKSHELF, LootContainerType.CARDBOARD_BOX),
    WORKING_STIFFS("Working Stiffs",     LootContainerType.TOOL_CRATE, LootContainerType.SUPPLY_CRATE),
    PASS_N_GAS("Pass-n-Gas",             LootContainerType.FUEL_CACHE, LootContainerType.VENDING_MACHINE),
    POP_N_PILLS("Pop-n-Pills",           LootContainerType.MEDICINE_CABINET, LootContainerType.CARDBOARD_BOX),
    FARM("Farm",                         LootContainerType.FARM_CRATE, LootContainerType.KITCHEN_CABINET),
    UTILITY("Utility Building",          LootContainerType.SUPPLY_CRATE, LootContainerType.TOOL_CRATE),
    TRADER_OUTPOST("Trader Outpost",     LootContainerType.SUPPLY_CRATE, LootContainerType.VENDING_MACHINE);

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
