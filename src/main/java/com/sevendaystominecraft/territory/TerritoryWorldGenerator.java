package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.TerritoryConfig;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.territory.TerritoryStructureBuilder.BuildResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
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

        var biome = serverLevel.getBiome(candidate);
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) return;

        TerritoryTier tier = TerritoryTier.roll(serverLevel.random);
        TerritoryType type = TerritoryType.random(serverLevel.random);

        int surfaceY = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockX, blockZ);
        if (surfaceY <= 0) return;

        BlockPos origin = new BlockPos(blockX, surfaceY, blockZ);

        try {
            TerritoryRecord record = data.addTerritory(origin, tier, type);

            BuildResult result = TerritoryStructureBuilder.build(
                    serverLevel, origin, tier, type, serverLevel.random);

            spawnLabelEntity(serverLevel, record, result.labelPos);

            data.markDirtyRecord();

            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Territory] Placed {} {} structure at ({}, {}, {}). Zombies spawn on player entry.",
                    type.getDisplayName(), tier.getStars(), blockX, surfaceY, blockZ);

        } catch (Exception e) {
            SevenDaysToMinecraft.LOGGER.error("[BZHS Territory] Error generating territory at {}: {}",
                    origin, e.getMessage());
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
