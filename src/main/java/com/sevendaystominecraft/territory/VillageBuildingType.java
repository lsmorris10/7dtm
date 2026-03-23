package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.block.loot.LootContainerType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum VillageBuildingType {

    RESIDENTIAL("Abandoned Neighborhood", 20,
            new LootContainerType[]{LootContainerType.KITCHEN_CABINET, LootContainerType.MEDICINE_CABINET, LootContainerType.BOOKSHELF},
            2, 5, 7, 10, 4,
            Blocks.OAK_PLANKS, Blocks.OAK_PLANKS, Blocks.OAK_SLAB, Blocks.OAK_LOG),

    CRACK_A_BOOK("Crack-a-Book", 8,
            new LootContainerType[]{LootContainerType.BOOKSHELF, LootContainerType.BOOKSHELF, LootContainerType.CARDBOARD_BOX},
            2, 4, 8, 12, 5,
            Blocks.BRICKS, Blocks.OAK_PLANKS, Blocks.COBBLESTONE_SLAB, Blocks.BRICKS),

    WORKING_STIFFS("Working Stiffs Hardware", 8,
            new LootContainerType[]{LootContainerType.TOOL_CRATE, LootContainerType.SUPPLY_CRATE, LootContainerType.CARDBOARD_BOX},
            3, 6, 10, 14, 5,
            Blocks.STONE_BRICKS, Blocks.STONE, Blocks.STONE_BRICK_SLAB, Blocks.STONE_BRICKS),

    PASS_N_GAS("Pass-n-Gas", 8,
            new LootContainerType[]{LootContainerType.FUEL_CACHE, LootContainerType.VENDING_MACHINE, LootContainerType.TRASH_PILE},
            2, 4, 8, 11, 4,
            Blocks.LIGHT_GRAY_CONCRETE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.IRON_BARS),

    POP_N_PILLS("Pop-n-Pills", 8,
            new LootContainerType[]{LootContainerType.MEDICINE_CABINET, LootContainerType.MEDICINE_CABINET, LootContainerType.CARDBOARD_BOX},
            2, 4, 8, 11, 4,
            Blocks.WHITE_CONCRETE, Blocks.SMOOTH_QUARTZ, Blocks.STONE_BRICK_SLAB, Blocks.QUARTZ_PILLAR),

    FARM("Farm", 12,
            new LootContainerType[]{LootContainerType.FARM_CRATE, LootContainerType.KITCHEN_CABINET, LootContainerType.CARDBOARD_BOX},
            2, 5, 8, 12, 4,
            Blocks.SPRUCE_PLANKS, Blocks.DIRT, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_LOG),

    UTILITY("Utility Building", 8,
            new LootContainerType[]{LootContainerType.SUPPLY_CRATE, LootContainerType.MUNITIONS_BOX, LootContainerType.TOOL_CRATE},
            2, 4, 7, 10, 4,
            Blocks.COBBLESTONE, Blocks.STONE, Blocks.COBBLESTONE_SLAB, Blocks.COBBLESTONE),

    TRADER_OUTPOST("Trader Outpost", 3,
            new LootContainerType[]{LootContainerType.SUPPLY_CRATE, LootContainerType.VENDING_MACHINE},
            0, 0, 16, 16, 5,
            Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_LOG);

    private final String displayName;
    private final int spawnWeight;
    private final LootContainerType[] lootPool;
    private final int minZombies;
    private final int maxZombies;
    private final int minSize;
    private final int maxSize;
    private final int wallHeight;
    private final Block wallBlock;
    private final Block floorBlock;
    private final Block roofBlock;
    private final Block frameBlock;

    VillageBuildingType(String displayName, int spawnWeight, LootContainerType[] lootPool,
                        int minZombies, int maxZombies, int minSize, int maxSize, int wallHeight,
                        Block wallBlock, Block floorBlock, Block roofBlock, Block frameBlock) {
        this.displayName = displayName;
        this.spawnWeight = spawnWeight;
        this.lootPool = lootPool;
        this.minZombies = minZombies;
        this.maxZombies = maxZombies;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.wallHeight = wallHeight;
        this.wallBlock = wallBlock;
        this.floorBlock = floorBlock;
        this.roofBlock = roofBlock;
        this.frameBlock = frameBlock;
    }

    public String getDisplayName() { return displayName; }
    public int getSpawnWeight() { return spawnWeight; }
    public LootContainerType[] getLootPool() { return lootPool; }
    public int getMinZombies() { return minZombies; }
    public int getMaxZombies() { return maxZombies; }
    public int getMinSize() { return minSize; }
    public int getMaxSize() { return maxSize; }
    public int getWallHeight() { return wallHeight; }
    public Block getWallBlock() { return wallBlock; }
    public Block getFloorBlock() { return floorBlock; }
    public Block getRoofBlock() { return roofBlock; }
    public Block getFrameBlock() { return frameBlock; }

    public boolean isSafeZone() { return this == TRADER_OUTPOST; }

    public int getLootCount(int tier) {
        return Math.max(1, lootPool.length + (tier - 1));
    }

    public int getZombieCount(RandomSource random, int tier) {
        if (isSafeZone()) return 0;
        int base = minZombies + random.nextInt(Math.max(1, maxZombies - minZombies + 1));
        return Math.max(1, base + (tier - 1));
    }

    public LootContainerType pickLoot(RandomSource random) {
        return lootPool[random.nextInt(lootPool.length)];
    }

    public static VillageBuildingType weightedRandom(RandomSource random) {
        return weightedRandomExcluding(random, null);
    }

    public static VillageBuildingType weightedRandomExcluding(RandomSource random, VillageBuildingType excluded) {
        VillageBuildingType[] values = values();
        int totalWeight = 0;
        for (VillageBuildingType type : values) {
            if (type == TRADER_OUTPOST) continue;
            if (type == excluded) continue;
            totalWeight += type.spawnWeight;
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (VillageBuildingType type : values) {
            if (type == TRADER_OUTPOST) continue;
            if (type == excluded) continue;
            cumulative += type.spawnWeight;
            if (roll < cumulative) return type;
        }
        return RESIDENTIAL;
    }

    public static VillageBuildingType weightedRandomFrom(RandomSource random, java.util.List<VillageBuildingType> allowed) {
        if (allowed == null || allowed.isEmpty()) {
            return weightedRandom(random);
        }
        int totalWeight = 0;
        for (VillageBuildingType type : allowed) {
            totalWeight += type.spawnWeight;
        }
        if (totalWeight <= 0) {
            return allowed.get(random.nextInt(allowed.size()));
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (VillageBuildingType type : allowed) {
            cumulative += type.spawnWeight;
            if (roll < cumulative) return type;
        }
        return allowed.get(0);
    }
}
