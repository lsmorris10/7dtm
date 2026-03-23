package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.block.loot.LootContainerType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

    private static final Map<TerritoryType, List<VillageBuildingType>> ALLOWED_BUILDINGS = new EnumMap<>(TerritoryType.class);

    static {
        ALLOWED_BUILDINGS.put(RESIDENTIAL,    List.of(VillageBuildingType.RESIDENTIAL, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(COMMERCIAL,     List.of(VillageBuildingType.CRACK_A_BOOK, VillageBuildingType.WORKING_STIFFS, VillageBuildingType.PASS_N_GAS, VillageBuildingType.POP_N_PILLS));
        ALLOWED_BUILDINGS.put(INDUSTRIAL,     List.of(VillageBuildingType.WORKING_STIFFS, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(MILITARY,       List.of(VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(WILDERNESS,     List.of(VillageBuildingType.FARM, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(MEDICAL,        List.of(VillageBuildingType.POP_N_PILLS, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(CRACK_A_BOOK,   List.of(VillageBuildingType.CRACK_A_BOOK, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(WORKING_STIFFS, List.of(VillageBuildingType.WORKING_STIFFS, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(PASS_N_GAS,     List.of(VillageBuildingType.PASS_N_GAS, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(POP_N_PILLS,    List.of(VillageBuildingType.POP_N_PILLS, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(FARM,           List.of(VillageBuildingType.FARM, VillageBuildingType.RESIDENTIAL, VillageBuildingType.UTILITY));
        ALLOWED_BUILDINGS.put(UTILITY,        List.of(VillageBuildingType.UTILITY, VillageBuildingType.WORKING_STIFFS));
        ALLOWED_BUILDINGS.put(TRADER_OUTPOST, List.of(VillageBuildingType.TRADER_OUTPOST));
    }

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

    public List<VillageBuildingType> getAllowedBuildings() {
        return ALLOWED_BUILDINGS.getOrDefault(this, List.of(VillageBuildingType.UTILITY));
    }

    public static TerritoryType random(net.minecraft.util.RandomSource random) {
        TerritoryType[] values = values();
        return values[random.nextInt(values.length)];
    }

    public static TerritoryType randomNonTrader(net.minecraft.util.RandomSource random) {
        TerritoryType[] values = values();
        TerritoryType result;
        do {
            result = values[random.nextInt(values.length)];
        } while (result == TRADER_OUTPOST);
        return result;
    }
}
