package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class VillagerSuppressionHandler {

    private static final double MOD_TERRITORY_RADIUS = 80.0;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide()) return;
        if (!(villager.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        BlockPos villagerPos = villager.blockPosition();

        if (isNearModTerritory(serverLevel, villagerPos)) {
            event.setCanceled(true);
        }
    }

    private static boolean isNearModTerritory(ServerLevel level, BlockPos pos) {
        TerritoryData data = TerritoryData.getOrCreate(level);
        return !data.getNearby(pos, MOD_TERRITORY_RADIUS).isEmpty();
    }
}
