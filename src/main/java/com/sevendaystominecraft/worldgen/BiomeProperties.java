package com.sevendaystominecraft.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.LinkedHashMap;
import java.util.Map;

public class BiomeProperties {

    private static final Map<ResourceKey<Biome>, BiomeStats> STATS = new LinkedHashMap<>();

    static {
        register(ModBiomes.PINE_FOREST,   50f, 70f, 0.6f, 1, 3);
        register(ModBiomes.FOREST,        55f, 75f, 1.0f, 2, 4);
        register(ModBiomes.PLAINS,        60f, 85f, 1.0f, 2, 4);
        register(ModBiomes.DESERT,        85f, 130f, 1.2f, 3, 5);
        register(ModBiomes.SNOWY_TUNDRA,  -10f, 32f, 0.8f, 3, 5);
        register(ModBiomes.BURNED_FOREST, 70f, 90f, 1.5f, 3, 5);
        register(ModBiomes.WASTELAND,     80f, 110f, 2.5f, 5, 6);
    }

    private static void register(ResourceKey<Biome> key, float tempMin, float tempMax,
                                  float zombieDensity, int lootTierMin, int lootTierMax) {
        STATS.put(key, new BiomeStats(tempMin, tempMax, zombieDensity, lootTierMin, lootTierMax));
    }

    public static BiomeStats getStats(ResourceKey<Biome> biomeKey) {
        return STATS.getOrDefault(biomeKey, BiomeStats.DEFAULT);
    }

    public static BiomeStats getStats(Holder<Biome> biomeHolder) {
        for (Map.Entry<ResourceKey<Biome>, BiomeStats> entry : STATS.entrySet()) {
            if (biomeHolder.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return mapVanillaBiome(biomeHolder);
    }

    private static BiomeStats mapVanillaBiome(Holder<Biome> biomeHolder) {
        float baseTemp = biomeHolder.value().getBaseTemperature();
        if (baseTemp < 0.0f) {
            return STATS.get(ModBiomes.SNOWY_TUNDRA);
        } else if (baseTemp < 0.3f) {
            return STATS.get(ModBiomes.PINE_FOREST);
        } else if (baseTemp < 0.7f) {
            return STATS.get(ModBiomes.FOREST);
        } else if (baseTemp < 1.0f) {
            return STATS.get(ModBiomes.PLAINS);
        } else if (baseTemp < 1.5f) {
            return STATS.get(ModBiomes.DESERT);
        } else {
            return STATS.get(ModBiomes.WASTELAND);
        }
    }

    public static float getBiomeLootBonus(ResourceKey<Biome> biomeKey) {
        if (biomeKey.equals(ModBiomes.PINE_FOREST)) return 0f;
        if (biomeKey.equals(ModBiomes.FOREST)) return 5f;
        if (biomeKey.equals(ModBiomes.PLAINS)) return 5f;
        if (biomeKey.equals(ModBiomes.DESERT)) return 10f;
        if (biomeKey.equals(ModBiomes.SNOWY_TUNDRA)) return 10f;
        if (biomeKey.equals(ModBiomes.BURNED_FOREST)) return 15f;
        if (biomeKey.equals(ModBiomes.WASTELAND)) return 25f;
        return 0f;
    }

    public static float getBiomeLootBonus(Holder<Biome> biomeHolder) {
        for (ResourceKey<Biome> key : STATS.keySet()) {
            if (biomeHolder.is(key)) {
                return getBiomeLootBonus(key);
            }
        }
        ResourceKey<Biome> mapped = mapVanillaBiomeKey(biomeHolder);
        return mapped != null ? getBiomeLootBonus(mapped) : 0f;
    }

    private static ResourceKey<Biome> mapVanillaBiomeKey(Holder<Biome> biomeHolder) {
        float baseTemp = biomeHolder.value().getBaseTemperature();
        if (baseTemp < 0.0f) return ModBiomes.SNOWY_TUNDRA;
        if (baseTemp < 0.3f) return ModBiomes.PINE_FOREST;
        if (baseTemp < 0.7f) return ModBiomes.FOREST;
        if (baseTemp < 1.0f) return ModBiomes.PLAINS;
        if (baseTemp < 1.5f) return ModBiomes.DESERT;
        return ModBiomes.WASTELAND;
    }

    public record BiomeStats(float tempMinF, float tempMaxF, float zombieDensityMultiplier,
                             int lootTierMin, int lootTierMax) {
        public static final BiomeStats DEFAULT = new BiomeStats(55f, 75f, 1.0f, 2, 4);

        public float ambientTemperature(float timeOfDayFraction) {
            float cycle = (float) Math.cos((timeOfDayFraction - 0.25) * 2.0 * Math.PI);
            float t = (cycle + 1.0f) * 0.5f;
            return tempMinF + (tempMaxF - tempMinF) * t;
        }
    }
}
