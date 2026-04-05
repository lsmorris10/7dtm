package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public enum TerritoryTier {

    TIER_1(1, "★", 5, 8,   0.35f,  3, 2, 1),
    TIER_2(2, "★★", 8, 11,  0.20f,  5, 3, 2),
    TIER_3(3, "★★★", 10, 13, 0.15f,  8, 5, 3),
    TIER_4(4, "★★★★", 12, 15, 0.15f, 14, 7, 4),
    TIER_5(5, "★★★★★", 15, 20, 0.15f, 20, 9, 5);

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
                    ModEntities.CRAWLER,
                    ModEntities.BLOATED_WALKER, ModEntities.FERAL_WIGHT, ModEntities.FERAL_WIGHT,
                    ModEntities.SPIDER_ZOMBIE, ModEntities.SPIDER_ZOMBIE,
                    ModEntities.SOLDIER, ModEntities.COP, ModEntities.NURSE
            };
            case TIER_4 -> new Supplier[]{
                    ModEntities.FERAL_WIGHT, ModEntities.COP, ModEntities.COP,
                    ModEntities.SPIDER_ZOMBIE, ModEntities.BLOATED_WALKER,
                    ModEntities.NURSE, ModEntities.NURSE, ModEntities.SOLDIER,
                    ModEntities.DEMOLISHER, ModEntities.MUTATED_CHUCK, ModEntities.ZOMBIE_BEAR
            };
            case TIER_5 -> new Supplier[]{
                    ModEntities.FERAL_WIGHT, ModEntities.SOLDIER, ModEntities.SOLDIER,
                    ModEntities.DEMOLISHER, ModEntities.DEMOLISHER,
                    ModEntities.COP, ModEntities.SPIDER_ZOMBIE, ModEntities.NURSE,
                    ModEntities.BEHEMOTH, ModEntities.ZOMBIE_BEAR
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
        return roll(random, 5);
    }

    public static TerritoryTier roll(net.minecraft.util.RandomSource random, int maxTier) {
        return roll(random, 1, maxTier);
    }

    public static TerritoryTier roll(net.minecraft.util.RandomSource random, int minTier, int maxTier) {
        float r = random.nextFloat();
        float cumulative = 0f;
        float total = 0f;
        for (TerritoryTier t : values()) {
            if (t.tier >= minTier && t.tier <= maxTier) total += t.spawnWeight;
        }
        if (total <= 0f) return fromNumber(minTier);
        for (TerritoryTier t : values()) {
            if (t.tier < minTier || t.tier > maxTier) continue;
            cumulative += t.spawnWeight / total;
            if (r <= cumulative) return t;
        }
        return fromNumber(minTier);
    }
}
