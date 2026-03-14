package com.sevendaystominecraft.loot;

import com.sevendaystominecraft.SevenDaysConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public class LootStageCalculator {

    public static int calculate(ServerPlayer player) {
        int playerLevel = player.experienceLevel;
        long daysSurvived = player.level().getDayTime() / SevenDaysConstants.DAY_LENGTH;
        int biomeBonus = getBiomeBonus(player);
        int looterPerkBonus = 0;

        return (int) Math.floor(
                (playerLevel * 0.5) + (daysSurvived * 0.3) + biomeBonus + looterPerkBonus
        );
    }

    private static int getBiomeBonus(ServerPlayer player) {
        Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());

        ResourceLocation biomeLoc = player.level().registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getKey(biomeHolder.value());

        if (biomeLoc == null) return 0;

        String biomeName = biomeLoc.getPath().toLowerCase();

        if (biomeName.contains("wasteland") || biomeName.contains("badlands")) return 25;
        if (biomeName.contains("burn") || biomeName.contains("charred")) return 15;
        if (biomeName.contains("desert")) return 10;
        if (biomeName.contains("snow") || biomeName.contains("frozen") || biomeName.contains("ice")) return 10;
        if (biomeName.contains("forest") && !biomeName.contains("pine") && !biomeName.contains("taiga")) return 5;

        return 0;
    }
}
