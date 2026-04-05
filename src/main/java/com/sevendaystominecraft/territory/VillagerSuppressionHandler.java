package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.entity.npc.VanillaVillagerTraderEntity;
import com.sevendaystominecraft.trader.TraderData;
import com.sevendaystominecraft.trader.TraderRecord;
import com.sevendaystominecraft.trader.TraderSpawnHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class VillagerSuppressionHandler {

    private static final double MOD_TERRITORY_RADIUS = 80.0;

    /** ~20% chance to replace a suppressed villager with a VanillaVillagerTrader */
    private static final float TRADER_REPLACEMENT_CHANCE = 0.20f;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide()) return;
        if (!(villager.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        BlockPos villagerPos = villager.blockPosition();

        if (isNearModTerritory(serverLevel, villagerPos)) {
            event.setCanceled(true);

            // Chance to spawn a VanillaVillagerTrader in place of the suppressed villager
            if (serverLevel.random.nextFloat() < TRADER_REPLACEMENT_CHANCE) {
                spawnVillagerTrader(serverLevel, villagerPos);
            }
        }
    }

    private static void spawnVillagerTrader(ServerLevel level, BlockPos pos) {
        VanillaVillagerTraderEntity trader = ModEntities.VANILLA_VILLAGER_TRADER.get()
                .create(level, EntitySpawnReason.EVENT);
        if (trader == null) return;

        String name = VanillaVillagerTraderEntity.randomName(level.random);
        int tier = TraderSpawnHandler.getTraderTier(pos.getX(), pos.getZ());

        TraderData data = TraderData.getOrCreate(level);
        TraderRecord record = data.addTrader(pos, name, tier);

        trader.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360, 0f);
        trader.setTraderName(name);
        trader.setTraderTier(tier);
        trader.setTraderId(record.getId());
        level.addFreshEntity(trader);

        SevenDaysToMinecraft.LOGGER.info(
                "[BZHS] Spawned VanillaVillagerTrader '{}' (Tier {}) at ({}, {}, {}) replacing suppressed villager",
                name, tier, pos.getX(), pos.getY(), pos.getZ());
    }

    private static boolean isNearModTerritory(ServerLevel level, BlockPos pos) {
        TerritoryData data = TerritoryData.getOrCreate(level);
        return !data.getNearby(pos, MOD_TERRITORY_RADIUS).isEmpty();
    }
}
