package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;
import com.sevendaystominecraft.block.loot.LootContainerType;
import com.sevendaystominecraft.block.vehicle.VehicleWreckageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VillageClusterGenerator {

    public static class VillageResult {
        public final BlockPos center;
        public final List<BlockPos> allZombieSpawnPositions;
        public final List<List<BlockPos>> perBuildingZombieSpawns;
        public final List<BlockPos> buildingCenters;
        public final List<BlockPos> allLootPositions;
        public final List<LootContainerType> allLootTypes;
        public final List<VillageBuildingType> buildingTypes;
        public final int buildingCount;

        public VillageResult(BlockPos center, List<BlockPos> allZombieSpawnPositions,
                             List<List<BlockPos>> perBuildingZombieSpawns,
                             List<BlockPos> buildingCenters,
                             List<BlockPos> allLootPositions, List<LootContainerType> allLootTypes,
                             List<VillageBuildingType> buildingTypes, int buildingCount) {
            this.center = center;
            this.allZombieSpawnPositions = allZombieSpawnPositions;
            this.perBuildingZombieSpawns = perBuildingZombieSpawns;
            this.buildingCenters = buildingCenters;
            this.allLootPositions = allLootPositions;
            this.allLootTypes = allLootTypes;
            this.buildingTypes = buildingTypes;
            this.buildingCount = buildingCount;
        }
    }

    private static final int MIN_BUILDINGS = 4;
    private static final int MAX_BUILDINGS = 7;
    private static final int TRADER_MIN_BUILDINGS = 2;
    private static final int TRADER_MAX_BUILDINGS = 3;
    private static final int BUILDING_SPACING = 26;
    private static final int BUILDING_GAP = 7;
    private static final int PATH_BLOCK_RADIUS = 1;
    private static final int ROAD_CONNECTION_DISTANCE = 40;

    public static VillageResult generate(ServerLevel level, BlockPos center, TerritoryTier tier, RandomSource random) {
        return generate(level, center, tier, random, false, null);
    }

    public static VillageResult generate(ServerLevel level, BlockPos center, TerritoryTier tier, RandomSource random, boolean isTraderCompound) {
        return generate(level, center, tier, random, isTraderCompound, null);
    }

    public static VillageResult generate(ServerLevel level, BlockPos center, TerritoryTier tier, RandomSource random, TerritoryType territoryType) {
        return generate(level, center, tier, random, false, territoryType);
    }

    public static VillageResult generate(ServerLevel level, BlockPos center, TerritoryTier tier, RandomSource random, boolean isTraderCompound, TerritoryType territoryType) {
        NBTTemplateLoader.init(level);

        int minBuildings = isTraderCompound ? TRADER_MIN_BUILDINGS : MIN_BUILDINGS;
        int maxBuildings = isTraderCompound ? TRADER_MAX_BUILDINGS : MAX_BUILDINGS;
        int buildingCount = minBuildings + random.nextInt(maxBuildings - minBuildings + 1);

        int gridSize = (int) Math.ceil(Math.sqrt(MAX_BUILDINGS));
        int maxBuildingHalf = 0;
        for (VillageBuildingType type : VillageBuildingType.values()) {
            maxBuildingHalf = Math.max(maxBuildingHalf, (type.getMaxSize() + 1) / 2);
        }
        maxBuildingHalf = Math.max(maxBuildingHalf, 16);
        int maxOffset = (gridSize / 2) * BUILDING_SPACING + 6 + maxBuildingHalf + ROAD_CONNECTION_DISTANCE / 2;
        List<ChunkPos> forcedChunks = new ArrayList<>();
        int minChunkX = (center.getX() - maxOffset) >> 4;
        int maxChunkX = (center.getX() + maxOffset) >> 4;
        int minChunkZ = (center.getZ() - maxOffset) >> 4;
        int maxChunkZ = (center.getZ() + maxOffset) >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                if (!level.getForcedChunks().contains(cp.toLong())) {
                    level.setChunkForced(cx, cz, true);
                    forcedChunks.add(cp);
                }
            }
        }

        try {
            return generateInner(level, center, tier, random, isTraderCompound, territoryType, buildingCount, minBuildings);
        } finally {
            for (ChunkPos cp : forcedChunks) {
                level.setChunkForced(cp.x, cp.z, false);
            }
        }
    }

    private static VillageResult generateInner(ServerLevel level, BlockPos center, TerritoryTier tier, RandomSource random,
                                                boolean isTraderCompound, TerritoryType territoryType,
                                                int buildingCount, int minBuildings) {
        List<BlockPos> allZombieSpawns = new ArrayList<>();
        List<List<BlockPos>> perBuildingSpawns = new ArrayList<>();
        List<BlockPos> allLootPos = new ArrayList<>();
        List<LootContainerType> allLootTypes = new ArrayList<>();
        List<VillageBuildingType> types = new ArrayList<>();
        List<BlockPos> placedCenters = new ArrayList<>();
        List<int[]> placedFootprints = new ArrayList<>();

        int surfaceY = TerrainValidator.findSolidGroundY(level, center.getX(), center.getZ());
        BlockPos villageCenter = new BlockPos(center.getX(), surfaceY, center.getZ());

        int slotIndex = 0;
        int maxAttempts = buildingCount + 8;
        int attempts = 0;

        while (types.size() < buildingCount && attempts < maxAttempts) {
            VillageBuildingType buildingType;
            if (territoryType != null) {
                buildingType = VillageBuildingType.weightedRandomFrom(random, territoryType.getAllowedBuildings());
            } else {
                buildingType = VillageBuildingType.weightedRandom(random);
            }

            int sizeRange = Math.max(1, buildingType.getMaxSize() - buildingType.getMinSize() + 1);
            int sizeX = buildingType.getMinSize() + random.nextInt(sizeRange);
            int sizeZ = buildingType.getMinSize() + random.nextInt(sizeRange);
            int halfX = sizeX / 2;
            int halfZ = sizeZ / 2;

            ResourceLocation chosenTemplate = null;
            if (NBTTemplateLoader.hasTemplate(buildingType)) {
                chosenTemplate = NBTTemplateLoader.chooseTemplate(buildingType, random);
                int[] templateSize = NBTTemplateLoader.getTemplateSize(level, chosenTemplate);
                if (templateSize != null) {
                    halfX = templateSize[0] / 2;
                    halfZ = templateSize[1] / 2;
                }
            }

            BlockPos buildingPos = null;
            for (int retry = 0; retry < 3; retry++) {
                buildingPos = findBuildingPosition(level, villageCenter, placedCenters, placedFootprints, random, slotIndex + retry, halfX, halfZ);
                if (buildingPos != null) break;
            }
            slotIndex++;
            if (buildingPos == null) {
                attempts++;
                continue;
            }

            if (hasOverlapWithExisting(buildingPos, halfX, halfZ, placedCenters, placedFootprints)) {
                attempts++;
                continue;
            }

            VillageBuildingBuilder.BuildingResult result;
            if (chosenTemplate != null) {
                BlockPos templateOrigin = buildingPos.offset(-halfX, 0, -halfZ);
                result = NBTTemplateLoader.placeTemplate(level, templateOrigin, buildingType, tier, random, chosenTemplate);
                if (result == null) {
                    int fallbackHalfX = sizeX / 2;
                    int fallbackHalfZ = sizeZ / 2;
                    if (fallbackHalfX != halfX || fallbackHalfZ != halfZ) {
                        if (hasOverlapWithExisting(buildingPos, fallbackHalfX, fallbackHalfZ, placedCenters, placedFootprints)) {
                            attempts++;
                            continue;
                        }
                    }
                    result = VillageBuildingBuilder.build(level, buildingPos, buildingType, tier, random, sizeX, sizeZ);
                }
            } else {
                result = VillageBuildingBuilder.build(level, buildingPos, buildingType, tier, random, sizeX, sizeZ);
            }

            if (result != null) {
                int actualHalfX = result.sizeX / 2;
                int actualHalfZ = result.sizeZ / 2;

                List<BlockPos> cappedSpawns;
                if (isTraderCompound) {
                    cappedSpawns = new ArrayList<>();
                } else {
                    int maxZombiesForBuilding = buildingType.getZombieCount(random, tier.getTier());
                    cappedSpawns = new ArrayList<>(result.zombieSpawnPositions);
                    if (cappedSpawns.size() > maxZombiesForBuilding) {
                        cappedSpawns = new ArrayList<>(cappedSpawns.subList(0, maxZombiesForBuilding));
                    }
                }
                allZombieSpawns.addAll(cappedSpawns);
                perBuildingSpawns.add(cappedSpawns);
                allLootPos.addAll(result.lootPositions);
                allLootTypes.addAll(result.lootTypes);
                types.add(buildingType);
                placedCenters.add(result.center);
                placedFootprints.add(new int[]{actualHalfX, actualHalfZ});
            }
            attempts++;
        }

        if (placedCenters.size() < minBuildings) {
            return null;
        }

        Set<BlockPos> roadPositionSet = new HashSet<>();

        if (!placedCenters.isEmpty()) {
            buildPath(level, villageCenter, placedCenters.get(0), roadPositionSet);
        }

        for (int i = 0; i < placedCenters.size(); i++) {
            for (int j = i + 1; j < placedCenters.size(); j++) {
                BlockPos a = placedCenters.get(i);
                BlockPos b = placedCenters.get(j);
                double dx = a.getX() - b.getX();
                double dz = a.getZ() - b.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= ROAD_CONNECTION_DISTANCE) {
                    buildPath(level, a, b, roadPositionSet);
                }
            }
        }

        List<BlockPos> roadPositions = new ArrayList<>(roadPositionSet);

        scatterExteriorProps(level, villageCenter, placedCenters, roadPositions, tier, random);

        return new VillageResult(villageCenter, allZombieSpawns, perBuildingSpawns, placedCenters,
                allLootPos, allLootTypes, types, placedCenters.size());
    }

    private static BlockPos findBuildingPosition(ServerLevel level, BlockPos center,
                                                  List<BlockPos> placed, List<int[]> placedFootprints,
                                                  RandomSource random, int index, int halfX, int halfZ) {
        int gridSize = (int) Math.ceil(Math.sqrt(MAX_BUILDINGS));
        int row = index / gridSize;
        int col = index % gridSize;

        int offsetX = (col - gridSize / 2) * BUILDING_SPACING + random.nextInt(7) - 3;
        int offsetZ = (row - gridSize / 2) * BUILDING_SPACING + random.nextInt(7) - 3;

        int x = center.getX() + offsetX;
        int z = center.getZ() + offsetZ;
        int y = TerrainValidator.findSolidGroundY(level, x, z);

        if (y <= 0) return null;

        BlockPos candidate = new BlockPos(x, y, z);

        BlockState surfaceBlock = level.getBlockState(candidate.below());
        if (surfaceBlock.liquid() || level.getBlockState(candidate).liquid()) return null;

        int slopeVariance = TerrainValidator.getSlopeVariance(level, x, z, halfX, halfZ);
        if (slopeVariance > TerrainValidator.MAX_SLOPE_VARIANCE) return null;

        if (TerrainValidator.isInLocalDepression(level, x, z, y)) return null;

        for (int cx = -4; cx <= 4; cx += 4) {
            for (int cz = -4; cz <= 4; cz += 4) {
                int checkY = TerrainValidator.findSolidGroundY(level, x + cx, z + cz);
                BlockPos checkPos = new BlockPos(x + cx, checkY, z + cz);
                if (level.getBlockState(checkPos).liquid() || level.getBlockState(checkPos.below()).liquid()) {
                    return null;
                }
            }
        }

        for (int i = 0; i < placed.size(); i++) {
            BlockPos existing = placed.get(i);
            int[] existingFoot = placedFootprints.get(i);
            int distX = Math.abs(candidate.getX() - existing.getX());
            int distZ = Math.abs(candidate.getZ() - existing.getZ());
            int minDistX = halfX + existingFoot[0] + BUILDING_GAP;
            int minDistZ = halfZ + existingFoot[1] + BUILDING_GAP;
            if (distX < minDistX && distZ < minDistZ) return null;
        }

        return candidate;
    }

    private static boolean hasOverlapWithExisting(BlockPos center, int halfX, int halfZ,
                                                   List<BlockPos> placedCenters, List<int[]> placedFootprints) {
        for (int i = 0; i < placedCenters.size(); i++) {
            BlockPos existing = placedCenters.get(i);
            int[] existingFoot = placedFootprints.get(i);
            int distX = Math.abs(center.getX() - existing.getX());
            int distZ = Math.abs(center.getZ() - existing.getZ());
            int minDistX = halfX + existingFoot[0] + BUILDING_GAP;
            int minDistZ = halfZ + existingFoot[1] + BUILDING_GAP;
            if (distX < minDistX && distZ < minDistZ) return true;
        }
        return false;
    }

    private static void buildPath(ServerLevel level, BlockPos from, BlockPos to, Set<BlockPos> roadPositions) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) return;

        for (int i = 0; i <= steps; i++) {
            int x = from.getX() + dx * i / steps;
            int z = from.getZ() + dz * i / steps;
            int y = TerrainValidator.findSolidGroundY(level, x, z);

            for (int px = -PATH_BLOCK_RADIUS; px <= PATH_BLOCK_RADIUS; px++) {
                for (int pz = -PATH_BLOCK_RADIUS; pz <= PATH_BLOCK_RADIUS; pz++) {
                    int pathY = TerrainValidator.findSolidGroundY(level, x + px, z + pz);
                    BlockPos pathPos = new BlockPos(x + px, pathY - 1, z + pz);
                    if (level.isLoaded(pathPos)) {
                        BlockState current = level.getBlockState(pathPos.above());
                        if (current.isAir() || current.getBlock() == Blocks.SHORT_GRASS || current.getBlock() == Blocks.TALL_GRASS
                                || TerrainValidator.isVegetation(current.getBlock())) {
                            setBlock(level, pathPos, Blocks.GRAVEL.defaultBlockState());
                            setBlock(level, pathPos.above(), Blocks.AIR.defaultBlockState());
                            roadPositions.add(pathPos.above());
                        }
                    }
                }
            }
        }
    }

    private static void scatterExteriorProps(ServerLevel level, BlockPos center,
                                              List<BlockPos> buildingCenters,
                                              List<BlockPos> roadPositions,
                                              TerritoryTier tier, RandomSource random) {
        int propCount = 3 + random.nextInt(5) + tier.getTier();

        int vehicleCount = 1 + random.nextInt(3);
        if (!roadPositions.isEmpty()) {
            List<BlockPos> availableRoadPositions = new ArrayList<>(roadPositions);
            for (int i = 0; i < vehicleCount && !availableRoadPositions.isEmpty(); i++) {
                int idx = random.nextInt(availableRoadPositions.size());
                BlockPos roadPos = availableRoadPositions.remove(idx);
                if (level.isLoaded(roadPos)) {
                    placeVehicleWreckage(level, roadPos, random);
                }
            }
        }

        for (int i = 0; i < propCount; i++) {
            int offsetX = random.nextInt(60) - 30;
            int offsetZ = random.nextInt(60) - 30;
            int x = center.getX() + offsetX;
            int z = center.getZ() + offsetZ;
            int y = TerrainValidator.findSolidGroundY(level, x, z);
            BlockPos propPos = new BlockPos(x, y, z);

            if (!level.isLoaded(propPos)) continue;

            BlockState belowState = level.getBlockState(propPos.below());
            if (!TerrainValidator.isSolidTerrainBlock(belowState.getBlock())) continue;

            float roll = random.nextFloat();
            if (roll < 0.4f) {
                placeTrashPile(level, propPos, tier, random);
            } else if (roll < 0.7f) {
                placeMailbox(level, propPos, tier, random);
            } else {
                placeVendingMachine(level, propPos, tier, random);
            }
        }
    }

    private static void placeTrashPile(ServerLevel level, BlockPos pos, TerritoryTier tier, RandomSource random) {
        Block block = ModBlocks.TRASH_PILE_BLOCK.get();
        setBlock(level, pos, block.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity be) {
            be.setTerritoryTier(tier.getTier());
        }
    }

    private static void placeMailbox(ServerLevel level, BlockPos pos, TerritoryTier tier, RandomSource random) {
        Block block = ModBlocks.MAILBOX_BLOCK.get();
        setBlock(level, pos, block.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity be) {
            be.setTerritoryTier(tier.getTier());
        }
    }

    private static void placeVendingMachine(ServerLevel level, BlockPos pos, TerritoryTier tier, RandomSource random) {
        if (pos.getY() >= level.getMaxY() || !level.getBlockState(pos.above()).canBeReplaced()) return;
        Block block = ModBlocks.VENDING_MACHINE_BLOCK.get();
        BlockState lowerState = block.defaultBlockState()
                .setValue(com.sevendaystominecraft.block.loot.VendingMachineBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
        BlockState upperState = lowerState
                .setValue(com.sevendaystominecraft.block.loot.VendingMachineBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
        setBlock(level, pos, lowerState);
        setBlock(level, pos.above(), upperState);
        if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity be) {
            be.setTerritoryTier(tier.getTier());
        }
    }

    private static void placeVehicleWreckage(ServerLevel level, BlockPos pos, RandomSource random) {
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int vehicleType = random.nextInt(3);
        int footprintSize = switch (vehicleType) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 2;
            default -> 1;
        };
        int variance = TerrainValidator.getSlopeVariance(level, pos.getX(), pos.getZ(), footprintSize, footprintSize);
        if (variance > 2) return;
        switch (vehicleType) {
            case 0 -> placeBurntCar(level, pos, facing);
            case 1 -> placeBrokenTruck(level, pos, facing);
            case 2 -> placeWreckedCamper(level, pos, facing);
        }
    }

    private static void placeVehicleBlock(ServerLevel level, BlockPos origin,
                                           int right, int up, int forward,
                                           Direction facing, Block block) {
        Direction rightDir = facing.getClockWise();
        BlockPos target = origin.relative(rightDir, right)
                .above(up)
                .relative(facing, forward);
        if (level.isLoaded(target) && level.getBlockState(target).canBeReplaced()) {
            BlockState state = block.defaultBlockState()
                    .setValue(VehicleWreckageBlock.FACING, facing);
            level.setBlock(target, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void placeBurntCar(ServerLevel level, BlockPos pos, Direction facing) {
        Block body = ModBlocks.VEHICLE_BODY_CHARRED_BLOCK.get();
        Block wheel = ModBlocks.VEHICLE_WHEEL_BLOCK.get();
        Block window = ModBlocks.VEHICLE_WINDOW_BLOCK.get();
        Block roof = ModBlocks.VEHICLE_ROOF_BLOCK.get();
        Block core = ModBlocks.BURNT_CAR_BLOCK.get();

        for (int z = 0; z < 2; z++) {
            placeVehicleBlock(level, pos, 0, 0, z, facing, wheel);
            placeVehicleBlock(level, pos, 1, 0, z, facing, body);
            placeVehicleBlock(level, pos, 2, 0, z, facing, wheel);
        }
        placeVehicleBlock(level, pos, 0, 1, 0, facing, window);
        placeVehicleBlock(level, pos, 1, 1, 0, facing, core);
        placeVehicleBlock(level, pos, 2, 1, 0, facing, window);
        placeVehicleBlock(level, pos, 0, 1, 1, facing, body);
        placeVehicleBlock(level, pos, 1, 1, 1, facing, roof);
        placeVehicleBlock(level, pos, 2, 1, 1, facing, body);
    }

    private static void placeBrokenTruck(ServerLevel level, BlockPos pos, Direction facing) {
        Block body = ModBlocks.VEHICLE_BODY_BLOCK.get();
        Block wheel = ModBlocks.VEHICLE_WHEEL_BLOCK.get();
        Block window = ModBlocks.VEHICLE_WINDOW_BLOCK.get();
        Block roof = ModBlocks.VEHICLE_ROOF_BLOCK.get();
        Block bed = ModBlocks.TRUCK_BED_BLOCK.get();
        Block core = ModBlocks.BROKEN_TRUCK_BLOCK.get();

        placeVehicleBlock(level, pos, 0, 0, 0, facing, wheel);
        placeVehicleBlock(level, pos, 1, 0, 0, facing, core);
        placeVehicleBlock(level, pos, 2, 0, 0, facing, wheel);
        placeVehicleBlock(level, pos, 0, 0, 1, facing, wheel);
        placeVehicleBlock(level, pos, 1, 0, 1, facing, bed);
        placeVehicleBlock(level, pos, 2, 0, 1, facing, bed);
        placeVehicleBlock(level, pos, 3, 0, 1, facing, wheel);

        placeVehicleBlock(level, pos, 0, 1, 0, facing, body);
        placeVehicleBlock(level, pos, 1, 1, 0, facing, window);
        placeVehicleBlock(level, pos, 2, 1, 0, facing, body);
        placeVehicleBlock(level, pos, 1, 1, 1, facing, bed);
        placeVehicleBlock(level, pos, 2, 1, 1, facing, bed);

        placeVehicleBlock(level, pos, 0, 2, 0, facing, roof);
        placeVehicleBlock(level, pos, 1, 2, 0, facing, roof);
        placeVehicleBlock(level, pos, 2, 2, 0, facing, roof);
    }

    private static void placeWreckedCamper(ServerLevel level, BlockPos pos, Direction facing) {
        Block body = ModBlocks.CAMPER_BODY_BLOCK.get();
        Block wheel = ModBlocks.VEHICLE_WHEEL_BLOCK.get();
        Block window = ModBlocks.VEHICLE_WINDOW_BLOCK.get();
        Block roof = ModBlocks.VEHICLE_ROOF_BLOCK.get();
        Block core = ModBlocks.WRECKED_CAMPER_BLOCK.get();
        Block vbody = ModBlocks.VEHICLE_BODY_BLOCK.get();

        for (int z = 0; z < 3; z++) {
            placeVehicleBlock(level, pos, 0, 0, z, facing, wheel);
            placeVehicleBlock(level, pos, 4, 0, z, facing, wheel);
            for (int x = 1; x <= 3; x++) {
                Block fill = (z == 1 && x == 2) ? core : body;
                placeVehicleBlock(level, pos, x, 0, z, facing, fill);
            }
        }

        for (int z = 0; z < 3; z++) {
            placeVehicleBlock(level, pos, 0, 1, z, facing, vbody);
            placeVehicleBlock(level, pos, 4, 1, z, facing, vbody);
            for (int x = 1; x <= 3; x++) {
                if (z == 0 || z == 2) {
                    placeVehicleBlock(level, pos, x, 1, z, facing, window);
                } else {
                    placeVehicleBlock(level, pos, x, 1, z, facing, body);
                }
            }
        }

        for (int z = 0; z < 3; z++) {
            for (int x = 0; x < 5; x++) {
                placeVehicleBlock(level, pos, x, 2, z, facing, roof);
            }
        }
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.isLoaded(pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
