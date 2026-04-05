package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.TraderConfig;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.territory.TerritoryData;
import com.sevendaystominecraft.territory.TerritoryTier;
import com.sevendaystominecraft.territory.TerritoryType;
import com.sevendaystominecraft.territory.TerrainValidator;
import com.sevendaystominecraft.territory.VillageClusterGenerator;
import com.sevendaystominecraft.territory.TerritoryRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.HashSet;
import java.util.Set;

public class TraderSpawnHandler {

    private static final int OFFSET_WITHIN_CHUNK = 8;

    private static volatile boolean generationInProgress = false;

    private static final Set<BlockPos> pendingOutpostPositions = new HashSet<>();

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        if (generationInProgress) return;

        ChunkPos chunkPos = event.getChunk().getPos();
        int blockX = chunkPos.getMinBlockX() + OFFSET_WITHIN_CHUNK;
        int blockZ = chunkPos.getMinBlockZ() + OFFSET_WITHIN_CHUNK;

        TraderData traderData = TraderData.getOrCreate(serverLevel);

        BlockPos candidate = new BlockPos(blockX, 64, blockZ);
        int minSpacing = TraderConfig.INSTANCE.minChunkSpacing.get();

        if (isNearPendingOutpost(candidate, minSpacing)) return;

        if (!traderData.isGuaranteedSpawnPlaced()) {
            double distFromSpawn = Math.sqrt((double) blockX * blockX + (double) blockZ * blockZ);
            int guaranteeRadius = TraderConfig.INSTANCE.guaranteeRadius.get();
            if (distFromSpawn <= guaranteeRadius) {
                TerritoryData territoryDataGuaranteed = TerritoryData.getOrCreate(serverLevel);
                if (!traderData.hasNearby(candidate, minSpacing) && !isNearPendingOutpost(candidate, minSpacing)
                        && !territoryDataGuaranteed.hasNearby(candidate, 5)) {
                    pendingOutpostPositions.add(candidate);
                    if (tryPlaceTraderOutpost(serverLevel, blockX, blockZ)) {
                        traderData.setGuaranteedSpawnPlaced(true);
                        SevenDaysToMinecraft.LOGGER.info(
                                "[BZHS Trader] Guaranteed near-spawn trader outpost placed at ({}, {})", blockX, blockZ);
                    }
                    pendingOutpostPositions.remove(candidate);
                    return;
                }
            }
        }

        int chanceDenom = TraderConfig.INSTANCE.spawnChanceDenominator.get();

        long seed = serverLevel.getSeed();
        long chunkSeed = chunkPos.toLong() ^ seed ^ 0xDEAD7BADE8123L;
        java.util.Random chunkRandom = new java.util.Random(chunkSeed);

        if (chunkRandom.nextInt(chanceDenom) != 0) return;

        if (traderData.hasNearby(candidate, minSpacing)) return;
        if (isNearPendingOutpost(candidate, minSpacing)) return;

        int maxInRadius = TraderConfig.INSTANCE.maxTradersInRadius.get();
        int checkRadius = TraderConfig.INSTANCE.maxTradersCheckRadius.get();
        if (traderData.countNearby(candidate, checkRadius) >= maxInRadius) return;

        TerritoryData territoryData = TerritoryData.getOrCreate(serverLevel);
        if (territoryData.hasNearby(candidate, 5)) return;

        var biome = serverLevel.getBiome(candidate);
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) return;

        pendingOutpostPositions.add(candidate);
        tryPlaceTraderOutpost(serverLevel, blockX, blockZ);
        pendingOutpostPositions.remove(candidate);
    }

    private static boolean isNearPendingOutpost(BlockPos pos, int minChunkSpacing) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        for (BlockPos pending : pendingOutpostPositions) {
            int px = pending.getX() >> 4;
            int pz = pending.getZ() >> 4;
            int dx = Math.abs(chunkX - px);
            int dz = Math.abs(chunkZ - pz);
            if (dx <= minChunkSpacing && dz <= minChunkSpacing) return true;
        }
        return false;
    }

    private static final int SLOPE_CHECK_HALF = 8;

    private static boolean tryPlaceTraderOutpost(ServerLevel serverLevel, int blockX, int blockZ) {
        generationInProgress = true;
        try {
            return tryPlaceTraderOutpostInner(serverLevel, blockX, blockZ);
        } finally {
            generationInProgress = false;
        }
    }

    private static boolean tryPlaceTraderOutpostInner(ServerLevel serverLevel, int blockX, int blockZ) {
        int surfaceY = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockX, blockZ);
        if (surfaceY <= 0) return false;

        int maxElevation = TraderConfig.INSTANCE.maxElevation.get();
        if (surfaceY > maxElevation) return false;

        int slopeVariance = TerrainValidator.getSlopeVariance(serverLevel, blockX, blockZ, SLOPE_CHECK_HALF, SLOPE_CHECK_HALF);
        int maxSlope = TraderConfig.INSTANCE.maxSlopeVariance.get();
        if (slopeVariance > maxSlope) return false;

        BlockPos origin = new BlockPos(blockX, surfaceY, blockZ);
        if (serverLevel.getBlockState(origin).liquid() || serverLevel.getBlockState(origin.below()).liquid()) {
            return false;
        }

        TraderData traderData = TraderData.getOrCreate(serverLevel);
        TerritoryData territoryData = TerritoryData.getOrCreate(serverLevel);

        TerritoryTier tier = TerritoryTier.fromNumber(1);
        TerritoryType type = TerritoryType.TRADER_OUTPOST;

        TraderRecord placeholder = traderData.addTrader(origin, "__pending__", 1);
        int placeholderId = placeholder.getId();

        BlockPos pendingCandidate = new BlockPos(origin.getX(), 64, origin.getZ());
        territoryData.addPending(pendingCandidate);

        VillageClusterGenerator.VillageResult villageResult;
        try {
            villageResult = VillageClusterGenerator.generate(serverLevel, origin, tier, serverLevel.random, true, type);

            if (villageResult == null) {
                traderData.removeTrader(placeholderId);
                return false;
            }

            TerritoryRecord territoryRecord = territoryData.addTerritory(origin, tier, type);
            com.sevendaystominecraft.territory.TerritoryWorldGenerator.setTerritoryIdOnLootContainers(
                    serverLevel, villageResult.allLootPositions, territoryRecord.getId());
            territoryRecord.setBuildingCenters(villageResult.buildingCenters);
            territoryData.markDirtyRecord();

        } catch (Exception e) {
            SevenDaysToMinecraft.LOGGER.error("[BZHS Trader] Error generating outpost at {}: {}",
                    origin, e.getMessage());
            traderData.removeTrader(placeholderId);
            return false;
        } finally {
            territoryData.removePending(pendingCandidate);
        }

        traderData.removeTrader(placeholderId);

        BlockPos traderPos = origin;
        if (villageResult.buildingCenters != null && !villageResult.buildingCenters.isEmpty()) {
            traderPos = villageResult.buildingCenters.get(0);
        }
        spawnTraderEntity(serverLevel, traderPos, villageResult.center);
        return true;
    }

    private static void spawnTraderEntity(ServerLevel serverLevel, BlockPos origin, BlockPos compoundCenter) {
        TraderData data = TraderData.getOrCreate(serverLevel);

        int blockX = origin.getX();
        int blockZ = origin.getZ();
        int tier = getTraderTier(blockX, blockZ);
        String name = TraderEntity.randomName(serverLevel.random);

        TraderEntity trader = ModEntities.TRADER.get().create(serverLevel, EntitySpawnReason.STRUCTURE);
        if (trader == null) {
            SevenDaysToMinecraft.LOGGER.warn("[BZHS Trader] Failed to create trader entity at ({}, {}, {})",
                    blockX, origin.getY(), blockZ);
            return;
        }

        com.sevendaystominecraft.trader.TraderRecord record = data.addTrader(origin, name, tier);
        record.setCompoundCenter(compoundCenter);

        trader.moveTo(origin.getX() + 0.5, origin.getY() + 1, origin.getZ() + 0.5, 0f, 0f);
        trader.setTraderName(name);
        trader.setTraderTier(tier);
        trader.setTraderId(record.getId());
        serverLevel.addFreshEntity(trader);

        SevenDaysToMinecraft.LOGGER.info(
                "[BZHS Trader] Placed {} (Tier {}) at outpost ({}, {}, {})",
                name, tier, blockX, origin.getY(), blockZ);
    }

    public static int getTraderTier(int blockX, int blockZ) {
        double dist = Math.sqrt((double) blockX * blockX + (double) blockZ * blockZ);
        int tier1Max = TraderConfig.INSTANCE.tier1MaxDistance.get();
        int tier2Max = TraderConfig.INSTANCE.tier2MaxDistance.get();
        if (dist <= tier1Max) return 1;
        if (dist <= tier2Max) return 2;
        return 3;
    }
}
