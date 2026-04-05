package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.util.BiomeWoodMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Detects vanilla Woodland Mansions in newly generated chunks,
 * swaps their Dark Oak blocks for biome-appropriate wood,
 * and registers them as Abandoned Estate territories.
 */
@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class MansionWoodSwapper {

    private static final ResourceLocation MANSION_ID =
            ResourceLocation.withDefaultNamespace("mansion");

    // Track mansion structure starts we've already registered as territories
    private static final Set<Long> registeredMansionChunks = new HashSet<>();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        ChunkPos chunkPos = event.getChunk().getPos();

        // Check if this chunk has any woodland mansion structure pieces
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (var entry : event.getChunk().getAllStarts().entrySet()) {
            StructureStart start = entry.getValue();
            if (start == null || !start.isValid()) continue;

            var key = registry.getKey(entry.getKey());
            if (key == null || !key.equals(MANSION_ID)) continue;

            // This chunk contains the structure start of a mansion
            long chunkKey = chunkPos.toLong();
            if (registeredMansionChunks.contains(chunkKey)) continue;
            registeredMansionChunks.add(chunkKey);

            BoundingBox bb = start.getBoundingBox();
            BlockPos mansionCenter = new BlockPos(
                    (bb.minX() + bb.maxX()) / 2,
                    bb.minY(),
                    (bb.minZ() + bb.maxZ()) / 2
            );

            // Get the biome at the mansion center
            BiomeWoodMapper.WoodSet woodSet = BiomeWoodMapper.getWoodForBiome(level.getBiome(mansionCenter));

            // Only swap if the biome wood is NOT dark oak (no work needed in dark forests)
            boolean needsSwap = woodSet.planks != Blocks.DARK_OAK_PLANKS;

            // Schedule the wood swap for after the mansion chunks are fully generated
            // We process the entire bounding box of the structure start
            if (needsSwap) {
                // Process all currently loaded chunks within the mansion bounding box
                swapWoodInBoundingBox(level, bb, woodSet);
            }

            // Register this mansion as an Abandoned Estate territory
            registerMansionTerritory(level, mansionCenter, bb);

            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Mansion] Detected vanilla Woodland Mansion at ({}, {}, {}). Wood swap: {}. Biome wood: {}",
                    mansionCenter.getX(), mansionCenter.getY(), mansionCenter.getZ(),
                    needsSwap ? "YES" : "NO (Dark Forest)",
                    woodSet.planks.getName().getString()
            );
        }

        // Also check if this chunk is within ANY mansion's bounding box (for chunks loaded after the start)
        swapChunkIfInMansion(level, chunkPos);
    }

    /**
     * For chunks that generate after the structure start chunk, check if they
     * overlap a mansion bounding box and swap blocks accordingly.
     */
    private static void swapChunkIfInMansion(ServerLevel level, ChunkPos chunkPos) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        // Check referenced structures in this chunk
        var references = level.getChunk(chunkPos.x, chunkPos.z).getAllReferences();
        for (var entry : references.entrySet()) {
            var key = registry.getKey(entry.getKey());
            if (key == null || !key.equals(MANSION_ID)) continue;
            if (entry.getValue().isEmpty()) continue;

            // This chunk references a mansion structure — find the structure start
            for (long startChunkLong : entry.getValue()) {
                ChunkPos startChunkPos = new ChunkPos(startChunkLong);
                ChunkAccess startChunk = level.getChunk(startChunkPos.x, startChunkPos.z, ChunkStatus.STRUCTURE_STARTS, false);
                if (startChunk == null) continue;

                for (var startEntry : startChunk.getAllStarts().entrySet()) {
                    StructureStart start = startEntry.getValue();
                    if (start == null || !start.isValid()) continue;

                    var startKey = registry.getKey(startEntry.getKey());
                    if (startKey == null || !startKey.equals(MANSION_ID)) continue;

                    BoundingBox bb = start.getBoundingBox();
                    BlockPos mansionCenter = new BlockPos(
                            (bb.minX() + bb.maxX()) / 2,
                            bb.minY(),
                            (bb.minZ() + bb.maxZ()) / 2
                    );

                    BiomeWoodMapper.WoodSet woodSet = BiomeWoodMapper.getWoodForBiome(level.getBiome(mansionCenter));
                    if (woodSet.planks == Blocks.DARK_OAK_PLANKS) continue; // No swap needed

                    // Swap only the blocks within THIS chunk that are inside the mansion bounding box
                    int minX = Math.max(chunkPos.getMinBlockX(), bb.minX());
                    int maxX = Math.min(chunkPos.getMaxBlockX(), bb.maxX());
                    int minZ = Math.max(chunkPos.getMinBlockZ(), bb.minZ());
                    int maxZ = Math.min(chunkPos.getMaxBlockZ(), bb.maxZ());

                    for (int x = minX; x <= maxX; x++) {
                        for (int y = bb.minY(); y <= bb.maxY(); y++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                BlockPos pos = new BlockPos(x, y, z);
                                swapBlock(level, pos, woodSet);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void swapWoodInBoundingBox(ServerLevel level, BoundingBox bb, BiomeWoodMapper.WoodSet woodSet) {
        for (int x = bb.minX(); x <= bb.maxX(); x++) {
            for (int y = bb.minY(); y <= bb.maxY(); y++) {
                for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.isLoaded(pos)) {
                        swapBlock(level, pos, woodSet);
                    }
                }
            }
        }
    }


    private static void swapBlock(ServerLevel level, BlockPos pos, BiomeWoodMapper.WoodSet woodSet) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Block replacement = null;

        // Dark Oak replacements
        if (block == Blocks.DARK_OAK_PLANKS) replacement = woodSet.planks;
        else if (block == Blocks.DARK_OAK_LOG) replacement = woodSet.log;
        else if (block == Blocks.DARK_OAK_SLAB) replacement = woodSet.slab;
        else if (block == Blocks.DARK_OAK_STAIRS) replacement = woodSet.stairs;
        else if (block == Blocks.DARK_OAK_FENCE) replacement = woodSet.fence;
        else if (block == Blocks.DARK_OAK_FENCE_GATE) replacement = woodSet.fenceGate;
        else if (block == Blocks.DARK_OAK_DOOR) replacement = woodSet.door;
        else if (block == Blocks.DARK_OAK_TRAPDOOR) replacement = woodSet.trapdoor;
        else if (block == Blocks.DARK_OAK_PRESSURE_PLATE) replacement = woodSet.pressurePlate;
        else if (block == Blocks.DARK_OAK_BUTTON) replacement = woodSet.button;

        if (replacement != null && replacement != block) {
            // Preserve block state properties (direction, half, waterlogged, etc.)
            BlockState newState = replacement.defaultBlockState();
            for (Property<?> property : state.getProperties()) {
                if (newState.hasProperty(property)) {
                    newState = copyProperty(state, newState, property);
                }
            }
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        }
    }


    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }

    private static void registerMansionTerritory(ServerLevel level, BlockPos center, BoundingBox bb) {
        TerritoryData data = TerritoryData.getOrCreate(level);

        // Check if there's already a territory near this mansion
        if (data.hasNearby(center, 5)) {
            SevenDaysToMinecraft.LOGGER.debug(
                    "[BZHS Mansion] Skipped territory registration — already exists near ({}, {}, {})",
                    center.getX(), center.getY(), center.getZ());
            return;
        }

        TerritoryTier tier = TerritoryTier.fromNumber(5); // Always 5-star
        TerritoryType type = TerritoryType.ABANDONED_ESTATE;

        TerritoryRecord record = data.addTerritory(center, tier, type);

        // Set up building data — use bounding box corners so the territory radius covers the full mansion
        BlockPos corner1 = new BlockPos(bb.minX(), bb.minY(), bb.minZ());
        BlockPos corner2 = new BlockPos(bb.maxX(), bb.minY(), bb.minZ());
        BlockPos corner3 = new BlockPos(bb.minX(), bb.minY(), bb.maxZ());
        BlockPos corner4 = new BlockPos(bb.maxX(), bb.minY(), bb.maxZ());
        record.setBuildingCenters(java.util.List.of(center, corner1, corner2, corner3, corner4));
        record.setBuildingTypeNames(java.util.List.of("Abandoned Estate", "", "", "", ""));

        // Spawn a label entity above the mansion
        int labelY = bb.maxY() + 5;
        BlockPos labelPos = new BlockPos(center.getX(), labelY, center.getZ());
        TerritoryLabelEntity label = ModEntities.TERRITORY_LABEL.get().create(level, EntitySpawnReason.STRUCTURE);
        if (label != null) {
            label.moveTo(labelPos.getX() + 0.5, labelPos.getY(), labelPos.getZ() + 0.5, 0f, 0f);
            label.setLabelText(record.getLabel());
            label.setTerritoryTier(tier.getTier());
            label.setTerritoryId(record.getId());
            level.addFreshEntity(label);
        }

        // Generate interior spawn positions by scanning the mansion bounding box
        java.util.List<BlockPos> spawnPositions = generateMansionSpawnPositions(level, bb);

        // Spawn sleeper zombies inside the mansion
        if (!spawnPositions.isEmpty()) {
            java.util.List<java.util.List<BlockPos>> perBuildingSpawns = new java.util.ArrayList<>();
            perBuildingSpawns.add(spawnPositions);
            SleeperZombieManager.spawnSleepers(level, record, perBuildingSpawns);

            SevenDaysToMinecraft.LOGGER.info(
                    "[BZHS Mansion] Spawned sleeper zombies in Abandoned Estate at ({}, {}, {}). {} spawn positions found",
                    center.getX(), center.getY(), center.getZ(), spawnPositions.size());
        } else {
            SevenDaysToMinecraft.LOGGER.warn(
                    "[BZHS Mansion] No valid spawn positions found in mansion at ({}, {}, {})",
                    center.getX(), center.getY(), center.getZ());
        }

        data.markDirtyRecord();
    }

    /**
     * Scans the mansion's bounding box for valid interior spawn positions.
     * Looks for 2-block air gaps above solid/non-air blocks inside the structure.
     * Samples every 3 blocks in X/Z and checks multiple Y levels (each floor).
     */
    private static java.util.List<BlockPos> generateMansionSpawnPositions(ServerLevel level, BoundingBox bb) {
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();

        // Sample every 3 blocks in X/Z to get good coverage without excessive checks
        for (int x = bb.minX() + 1; x < bb.maxX(); x += 3) {
            for (int z = bb.minZ() + 1; z < bb.maxZ(); z += 3) {
                // Scan each Y level within the bounding box for valid floor positions
                for (int y = bb.minY(); y <= bb.maxY(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.isLoaded(pos)) continue;

                    net.minecraft.world.level.block.state.BlockState below = level.getBlockState(pos.below());
                    net.minecraft.world.level.block.state.BlockState here = level.getBlockState(pos);
                    net.minecraft.world.level.block.state.BlockState above = level.getBlockState(pos.above());

                    // Valid spawn: solid/non-air floor, 2-block air gap for the zombie
                    if ((below.isSolid() || !below.isAir()) && here.isAir() && above.isAir()) {
                        positions.add(pos);
                        break; // Only take the lowest valid position per X/Z column
                    }
                }
            }
        }

        // Shuffle and cap to tier max zombies to avoid over-spawning
        java.util.Collections.shuffle(positions, new java.util.Random(
                (long) bb.minX() * 31 + bb.minZ()));
        int maxZombies = TerritoryTier.fromNumber(5).getMaxZombies();
        if (positions.size() > maxZombies) {
            positions = new java.util.ArrayList<>(positions.subList(0, maxZombies));
        }

        return positions;
    }

    public static void clearCache() {
        registeredMansionChunks.clear();
    }
}
