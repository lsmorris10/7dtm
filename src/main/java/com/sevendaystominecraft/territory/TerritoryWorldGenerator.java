package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;
import com.sevendaystominecraft.config.TerritoryConfig;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.worldgen.BiomeProperties;
import com.sevendaystominecraft.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class TerritoryWorldGenerator {

    private static final int OFFSET_WITHIN_CHUNK = 8;

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk()) return;

        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        ChunkPos chunkPos = event.getChunk().getPos();

        int chanceDenom = TerritoryConfig.INSTANCE.spawnChanceDenominator.get();
        int minSpacing = TerritoryConfig.INSTANCE.minChunkSpacing.get();

        long seed = serverLevel.getSeed();
        long chunkSeed = chunkPos.toLong() ^ seed ^ 0x7DEADBEEF12345L;
        java.util.Random chunkRandom = new java.util.Random(chunkSeed);

        if (chunkRandom.nextInt(chanceDenom) != 0) return;

        TerritoryData data = TerritoryData.getOrCreate(serverLevel);

        int blockX = chunkPos.getMinBlockX() + OFFSET_WITHIN_CHUNK;
        int blockZ = chunkPos.getMinBlockZ() + OFFSET_WITHIN_CHUNK;
        BlockPos candidate = new BlockPos(blockX, 64, blockZ);

        if (data.hasNearby(candidate, minSpacing)) return;

        com.sevendaystominecraft.trader.TraderData traderData = com.sevendaystominecraft.trader.TraderData.getOrCreate(serverLevel);
        if (traderData.hasNearby(candidate, 5)) return;

        int vanillaExclusionRadius = TerritoryConfig.INSTANCE.vanillaStructureExclusionRadius.get();
        if (vanillaExclusionRadius > 0 && hasNearbyVanillaStructure(serverLevel, candidate, vanillaExclusionRadius)) {
            SevenDaysToMinecraft.LOGGER.debug(
                    "[BZHS Village] Skipped territory at ({}, {}) — vanilla structure nearby (within {} blocks)",
                    blockX, blockZ, vanillaExclusionRadius);
            return;
        }

        var biome = serverLevel.getBiome(candidate);
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) return;

        int distanceMaxTier = getMaxTierForDistance(blockX, blockZ);
        int[] biomeTierRange = getVillageBiomeTierRange(biome);
        int biomeMin = biomeTierRange[0];
        int biomeMax = biomeTierRange[1];

        if (distanceMaxTier < biomeMin) return;

        int maxTier = Math.min(distanceMaxTier, biomeMax);

        TerritoryTier tier = TerritoryTier.roll(serverLevel.random, biomeMin, maxTier);

        // 25% chance for Tier 5 to become an underground bunker
        int roll = chunkRandom.nextInt(100);
        boolean isBunker = tier.getTier() == 5 && roll < 25;

        TerritoryType type;
        if (isBunker) {
            type = TerritoryType.UNDERGROUND_BUNKER;
        } else {
            type = TerritoryType.randomNonTrader(serverLevel.random);
        }

        int surfaceY = TerrainValidator.findSolidGroundY(serverLevel, blockX, blockZ);
        if (surfaceY <= 0) return;

        if (TerrainValidator.isInRavine(serverLevel, blockX, blockZ, surfaceY)) return;

        BlockPos origin = new BlockPos(blockX, surfaceY, blockZ);
        if (serverLevel.getBlockState(origin).liquid() || serverLevel.getBlockState(origin.below()).liquid()) return;

        data.addPending(candidate);
        try {
            if (isBunker) {
                // Generate underground bunker
                BunkerGenerator.BunkerResult bunkerResult =
                        BunkerGenerator.generate(serverLevel, origin, serverLevel.random);

                TerritoryRecord record = data.addTerritory(origin, tier, type);
                data.removePending(candidate);

                setTerritoryIdOnLootContainers(serverLevel, bunkerResult.lootPositions, record.getId());

                spawnLabelEntity(serverLevel, record, origin.above(3));

                record.setBuildingCenters(java.util.List.of(origin));
                record.setBuildingTypeNames(java.util.List.of("Underground Bunker"));

                // Spawn bunker zombies via sleeper system
                java.util.List<java.util.List<BlockPos>> perBuildingSpawns = new java.util.ArrayList<>();
                perBuildingSpawns.add(bunkerResult.zombieSpawnPositions);
                SleeperZombieManager.spawnSleepers(serverLevel, record, perBuildingSpawns);

                data.markDirtyRecord();

                SevenDaysToMinecraft.LOGGER.info(
                        "[BZHS Bunker] Placed underground bunker at ({}, {}, {}). {} rooms, {} zombies, {} loot",
                        blockX, surfaceY, blockZ,
                        bunkerResult.roomCount, bunkerResult.zombieSpawnPositions.size(),
                        bunkerResult.lootPositions.size());
                return;
            }

            VillageClusterGenerator.VillageResult villageResult =
                    VillageClusterGenerator.generate(serverLevel, origin, tier, serverLevel.random, type);

            if (villageResult == null) {
                data.removePending(candidate);
                return;
            }

            TerritoryRecord record = data.addTerritory(origin, tier, type);
            data.removePending(candidate);

            setTerritoryIdOnLootContainers(serverLevel, villageResult.allLootPositions, record.getId());

            spawnLabelEntity(serverLevel, record, origin.above(tier.getLabelHeight() + 5));

            record.setBuildingCenters(villageResult.buildingCenters);
            java.util.List<String> typeNames = new java.util.ArrayList<>();
            for (VillageBuildingType bt : villageResult.buildingTypes) {
                typeNames.add(bt.getDisplayName());
            }
            record.setBuildingTypeNames(typeNames);
            SleeperZombieManager.spawnSleepers(serverLevel, record, villageResult.perBuildingZombieSpawns);

            data.markDirtyRecord();

            double distFromSpawn = Math.sqrt((double) blockX * blockX + (double) blockZ * blockZ);
            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Village] Placed village ({} buildings, {} type) at ({}, {}, {}). Tier: {} Distance: {} Biome tier: {}-{}",
                    villageResult.buildingCount, type.getDisplayName(), blockX, surfaceY, blockZ,
                    tier.getStars(), String.format("%.0f", distFromSpawn),
                    biomeMin, biomeMax);

        } catch (Exception e) {
            data.removePending(candidate);
            SevenDaysToMinecraft.LOGGER.error("[BZHS Village] Error generating village at {}: {}",
                    origin, e.getMessage());
        }
    }

    static int getMaxTierForDistance(int blockX, int blockZ) {
        double dist = Math.sqrt((double) blockX * blockX + (double) blockZ * blockZ);
        int safeZone = TerritoryConfig.INSTANCE.safeZoneRadius.get();
        int midRange = Math.max(TerritoryConfig.INSTANCE.midRangeRadius.get(), safeZone);
        int farRange = Math.max(TerritoryConfig.INSTANCE.farRangeRadius.get(), midRange);

        if (dist <= safeZone) {
            return TerritoryConfig.INSTANCE.safeZoneMaxTier.get();
        } else if (dist <= midRange) {
            return TerritoryConfig.INSTANCE.midRangeMaxTier.get();
        } else if (dist <= farRange) {
            return TerritoryConfig.INSTANCE.farRangeMaxTier.get();
        }
        return 5;
    }

    static int getBiomeMaxTier(Holder<Biome> biome) {
        BiomeProperties.BiomeStats stats = BiomeProperties.getStats(biome);
        float density = stats.zombieDensityMultiplier();

        if (density <= 0.6f) return 2;
        if (density <= 1.0f) return 3;
        if (density <= 1.2f) return 4;
        if (density <= 1.5f) return 4;
        return 5;
    }

    static int[] getVillageBiomeTierRange(Holder<Biome> biome) {
        if (biome.is(ModBiomes.PINE_FOREST) || biome.is(ModBiomes.FOREST)) return new int[]{1, 2};
        if (biome.is(ModBiomes.PLAINS)) return new int[]{2, 3};
        if (biome.is(ModBiomes.DESERT) || biome.is(ModBiomes.SNOWY_TUNDRA)) return new int[]{3, 4};
        if (biome.is(ModBiomes.BURNED_FOREST)) return new int[]{4, 5};
        if (biome.is(ModBiomes.WASTELAND)) return new int[]{4, 5};

        float baseTemp = biome.value().getBaseTemperature();
        if (baseTemp < 0.3f) return new int[]{1, 2};
        if (baseTemp < 0.7f) return new int[]{1, 2};
        if (baseTemp < 1.0f) return new int[]{2, 3};
        if (baseTemp < 1.5f) return new int[]{3, 4};
        return new int[]{4, 5};
    }

    private static final net.minecraft.resources.ResourceLocation PILLAGER_OUTPOST_ID =
            net.minecraft.resources.ResourceLocation.withDefaultNamespace("pillager_outpost");

    private static boolean isExcludedStructure(
            net.minecraft.core.Registry<Structure> registry, Structure structure) {
        var holder = registry.wrapAsHolder(structure);
        if (holder.is(StructureTags.VILLAGE)) return true;
        if (holder.is(StructureTags.ON_WOODLAND_EXPLORER_MAPS)) return true;
        if (holder.is(StructureTags.ON_OCEAN_EXPLORER_MAPS)) return true;
        var key = registry.getKey(structure);
        return key != null && key.equals(PILLAGER_OUTPOST_ID);
    }

    private static boolean hasNearbyVanillaStructure(ServerLevel level, BlockPos candidate, int radiusBlocks) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        int radiusChunks = (radiusBlocks >> 4) + 1;
        int candidateChunkX = SectionPos.blockToSectionCoord(candidate.getX());
        int candidateChunkZ = SectionPos.blockToSectionCoord(candidate.getZ());
        long radiusSq = (long) radiusBlocks * radiusBlocks;

        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                int cx = candidateChunkX + dx;
                int cz = candidateChunkZ + dz;
                ChunkAccess chunk = level.getChunk(cx, cz, ChunkStatus.STRUCTURE_STARTS, false);
                if (chunk == null) continue;
                for (var entry : chunk.getAllStarts().entrySet()) {
                    StructureStart start = entry.getValue();
                    if (start == null || !start.isValid()) continue;
                    if (!isExcludedStructure(registry, entry.getKey())) continue;
                    var bb = start.getBoundingBox();
                    int closestX = Math.max(bb.minX(), Math.min(candidate.getX(), bb.maxX()));
                    int closestZ = Math.max(bb.minZ(), Math.min(candidate.getZ(), bb.maxZ()));
                    long distSq = (long)(candidate.getX() - closestX) * (candidate.getX() - closestX) +
                                  (long)(candidate.getZ() - closestZ) * (candidate.getZ() - closestZ);
                    if (distSq <= radiusSq) {
                        return true;
                    }
                }
            }
        }

        var structureManager = level.structureManager();
        StructureStart directCheck = structureManager.getStructureWithPieceAt(candidate, StructureTags.VILLAGE);
        if (directCheck.isValid()) return true;
        directCheck = structureManager.getStructureWithPieceAt(candidate, StructureTags.ON_WOODLAND_EXPLORER_MAPS);
        if (directCheck.isValid()) return true;
        directCheck = structureManager.getStructureWithPieceAt(candidate, StructureTags.ON_OCEAN_EXPLORER_MAPS);
        if (directCheck.isValid()) return true;

        return false;
    }

    public static boolean isInSafeZone(int blockX, int blockZ) {
        double dist = Math.sqrt((double) blockX * blockX + (double) blockZ * blockZ);
        return dist <= TerritoryConfig.INSTANCE.safeZoneRadius.get();
    }

    public static void setTerritoryIdOnLootContainers(ServerLevel level, java.util.List<BlockPos> lootPositions, int territoryId) {
        for (BlockPos pos : lootPositions) {
            if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity be) {
                be.setTerritoryId(territoryId);
            }
        }
    }

    private static void spawnLabelEntity(ServerLevel level, TerritoryRecord record, BlockPos labelPos) {
        TerritoryLabelEntity label = ModEntities.TERRITORY_LABEL.get().create(level, EntitySpawnReason.STRUCTURE);
        if (label == null) return;

        label.moveTo(labelPos.getX() + 0.5, labelPos.getY(), labelPos.getZ() + 0.5, 0f, 0f);
        label.setLabelText(record.getLabel());
        label.setTerritoryTier(record.getTier().getTier());
        label.setTerritoryId(record.getId());
        level.addFreshEntity(label);
    }
}
