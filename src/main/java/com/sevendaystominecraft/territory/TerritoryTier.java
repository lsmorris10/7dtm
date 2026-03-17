package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public enum TerritoryTier {

    TIER_1(1, "★", 5, 8,   0.40f,  3, 2, 1),
    TIER_2(2, "★★", 8, 11,  0.25f,  5, 3, 2),
    TIER_3(3, "★★★", 10, 13, 0.15f,  8, 5, 3),
    TIER_4(4, "★★★★", 12, 15, 0.12f, 10, 7, 4),
    TIER_5(5, "★★★★★", 15, 20, 0.08f, 14, 9, 5);

    private final int tier;
    private final String stars;
    private final int minSize;
    private final int maxSize;
    private final float spawnWeight;
    private final int maxZombies;
    private final int lootContainerCount;
    private final int labelHeight;

    TerritoryTier(int tier, String stars, int minSize, int maxSize,
                  float spawnWeight, int maxZombies, int lootContainerCount, int labelHeight) {
        this.tier = tier;
        this.stars = stars;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.spawnWeight = spawnWeight;
        this.maxZombies = maxZombies;
        this.lootContainerCount = lootContainerCount;
        this.labelHeight = labelHeight;
    }

    public int getTier() { return tier; }
    public String getStars() { return stars; }
    public int getMinSize() { return minSize; }
    public int getMaxSize() { return maxSize; }
    public float getSpawnWeight() { return spawnWeight; }
    public int getMaxZombies() { return maxZombies; }
    public int getLootContainerCount() { return lootContainerCount; }
    public int getLabelHeight() { return labelHeight; }

    @SuppressWarnings("unchecked")
    public Supplier<EntityType<? extends BaseSevenDaysZombie>>[] getZombiePool() {
        return switch (this) {
            case TIER_1 -> new Supplier[]{
                    ModEntities.WALKER, ModEntities.WALKER, ModEntities.CRAWLER
            };
            case TIER_2 -> new Supplier[]{
                    ModEntities.WALKER, ModEntities.WALKER, ModEntities.CRAWLER,
                    ModEntities.BLOATED_WALKER
            };
            case TIER_3 -> new Supplier[]{
                    ModEntities.WALKER, ModEntities.CRAWLER,
                    ModEntities.BLOATED_WALKER, ModEntities.FERAL_WIGHT,
                    ModEntities.SPIDER_ZOMBIE
            };
            case TIER_4 -> new Supplier[]{
                    ModEntities.FERAL_WIGHT, ModEntities.COP, ModEntities.SPIDER_ZOMBIE,
                    ModEntities.BLOATED_WALKER, ModEntities.NURSE
            };
            case TIER_5 -> new Supplier[]{
                    ModEntities.FERAL_WIGHT, ModEntities.SOLDIER, ModEntities.DEMOLISHER,
                    ModEntities.COP, ModEntities.SPIDER_ZOMBIE
            };
        };
    }

    public static TerritoryTier fromNumber(int num) {
        for (TerritoryTier t : values()) {
            if (t.tier == num) return t;
        }
        return TIER_1;
    }

    public static TerritoryTier roll(net.minecraft.util.RandomSource random) {
        float r = random.nextFloat();
        float cumulative = 0f;
        float total = 0f;
        for (TerritoryTier t : values()) total += t.spawnWeight;
        for (TerritoryTier t : values()) {
            cumulative += t.spawnWeight / total;
            if (r <= cumulative) return t;
        }
        return TIER_1;
    }
}
