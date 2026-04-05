package com.sevendaystominecraft.loot;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.worldgen.BiomeProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.Holder;

public class LootStageCalculator {

    public static int calculate(ServerPlayer player) {
        int playerLevel = player.experienceLevel;
        long daysSurvived = player.level().getDayTime() / SevenDaysConstants.DAY_LENGTH;
        int biomeBonus = getBiomeBonus(player);

        int looterPerkBonus = 0;
        if (player.hasData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get())) {
            com.sevendaystominecraft.capability.SevenDaysPlayerStats stats =
                    player.getData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get());
            int keenRank = stats.getPerkRank("keen_scavenger");
            if (keenRank > 0) {
                int baseLootStage = (int) Math.floor(
                        (playerLevel * 0.5) + (daysSurvived * 0.3) + biomeBonus);
                looterPerkBonus = (int) (baseLootStage * 0.10f * keenRank);
            }
        }

        return (int) Math.floor(
                (playerLevel * 0.5) + (daysSurvived * 0.3) + biomeBonus + looterPerkBonus
        );
    }

    private static int getBiomeBonus(ServerPlayer player) {
        Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
        return (int) BiomeProperties.getBiomeLootBonus(biomeHolder);
    }
}
