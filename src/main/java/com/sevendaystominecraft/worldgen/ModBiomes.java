package com.sevendaystominecraft.worldgen;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class ModBiomes {

    public static final ResourceKey<Biome> PINE_FOREST = key("pine_forest");
    public static final ResourceKey<Biome> FOREST = key("forest");
    public static final ResourceKey<Biome> PLAINS = key("plains");
    public static final ResourceKey<Biome> DESERT = key("desert");
    public static final ResourceKey<Biome> SNOWY_TUNDRA = key("snowy_tundra");
    public static final ResourceKey<Biome> BURNED_FOREST = key("burned_forest");
    public static final ResourceKey<Biome> WASTELAND = key("wasteland");

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, name));
    }
}
