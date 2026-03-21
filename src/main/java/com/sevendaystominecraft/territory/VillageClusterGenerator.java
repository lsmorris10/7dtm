package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;
import com.sevendaystominecraft.block.loot.LootContainerType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

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
    private static final int MAX_BUILDINGS = 12;
    private static final int BUILDING_SPACING = 18;
    private static final int PATH_BLOCK_RADIUS = 1;

    public static VillageResult generate(ServerLevel level, BlockPos center, TerritoryTier tier, RandomSource random) {
        NBTTemplateLoader.init(level);

        int buildingCount = MIN_BUILDINGS + random.nextInt(MAX_BUILDINGS - MIN_BUILDINGS + 1);

        List<BlockPos> allZombieSpawns = new ArrayList<>();
        List<List<BlockPos>> perBuildingSpawns = new ArrayList<>();
        List<BlockPos> allLootPos = new ArrayList<>();
        List<LootContainerType> allLootTypes = new ArrayList<>();
        List<VillageBuildingType> types = new ArrayList<>();
        List<BlockPos> placedCenters = new ArrayList<>();

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, center.getX(), center.getZ());
        BlockPos villageCenter = new BlockPos(center.getX(), surfaceY, center.getZ());

        boolean hasTrader = false;
        int slotIndex = 0;
        int maxAttempts = buildingCount + 8;
        int attempts = 0;

        while (types.size() < buildingCount && attempts < maxAttempts) {
            int i = attempts;
            VillageBuildingType buildingType;
            if (i == 0 && random.nextFloat() < 0.15f) {
                buildingType = VillageBuildingType.TRADER_OUTPOST;
                hasTrader = true;
            } else {
                buildingType = VillageBuildingType.weightedRandom(random);
                if (buildingType == VillageBuildingType.TRADER_OUTPOST && hasTrader) {
                    buildingType = VillageBuildingType.RESIDENTIAL;
                }
                if (buildingType == VillageBuildingType.TRADER_OUTPOST) {
                    hasTrader = true;
                }
            }

            BlockPos buildingPos = null;
            for (int retry = 0; retry < 3; retry++) {
                buildingPos = findBuildingPosition(level, villageCenter, placedCenters, random, slotIndex + retry);
                if (buildingPos != null) break;
            }
            slotIndex++;
            if (buildingPos == null) {
                attempts++;
                continue;
            }

            VillageBuildingBuilder.BuildingResult result;
            if (NBTTemplateLoader.hasTemplate(buildingType)) {
                result = NBTTemplateLoader.placeTemplate(level, buildingPos, buildingType, tier, random);
                if (result == null) {
                    result = VillageBuildingBuilder.build(level, buildingPos, buildingType, tier, random);
                }
            } else {
                result = VillageBuildingBuilder.build(level, buildingPos, buildingType, tier, random);
            }

            if (result != null) {
                int maxZombiesForBuilding = buildingType.getZombieCount(random, tier.getTier());
                List<BlockPos> cappedSpawns = new ArrayList<>(result.zombieSpawnPositions);
                if (cappedSpawns.size() > maxZombiesForBuilding) {
                    cappedSpawns = new ArrayList<>(cappedSpawns.subList(0, maxZombiesForBuilding));
                }
                allZombieSpawns.addAll(cappedSpawns);
                perBuildingSpawns.add(cappedSpawns);
                allLootPos.addAll(result.lootPositions);
                allLootTypes.addAll(result.lootTypes);
                types.add(buildingType);
                placedCenters.add(buildingPos);

                if (placedCenters.size() > 1) {
                    BlockPos prevCenter = placedCenters.get(placedCenters.size() - 2);
                    buildPath(level, prevCenter, buildingPos);
                }
            }
            attempts++;
        }

        if (placedCenters.size() < MIN_BUILDINGS) {
            return null;
        }

        if (!placedCenters.isEmpty()) {
            buildPath(level, villageCenter, placedCenters.get(0));
        }

        scatterExteriorProps(level, villageCenter, placedCenters, tier, random);

        return new VillageResult(villageCenter, allZombieSpawns, perBuildingSpawns, placedCenters,
                allLootPos, allLootTypes, types, placedCenters.size());
    }

    private static BlockPos findBuildingPosition(ServerLevel level, BlockPos center,
                                                  List<BlockPos> placed, RandomSource random, int index) {
        int gridSize = (int) Math.ceil(Math.sqrt(MAX_BUILDINGS));
        int row = index / gridSize;
        int col = index % gridSize;

        int offsetX = (col - gridSize / 2) * BUILDING_SPACING + random.nextInt(5) - 2;
        int offsetZ = (row - gridSize / 2) * BUILDING_SPACING + random.nextInt(5) - 2;

        int x = center.getX() + offsetX;
        int z = center.getZ() + offsetZ;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        if (y <= 0) return null;

        BlockPos candidate = new BlockPos(x, y, z);

        BlockState surfaceBlock = level.getBlockState(candidate.below());
        if (surfaceBlock.liquid() || level.getBlockState(candidate).liquid()) return null;

        for (int cx = -4; cx <= 4; cx += 4) {
            for (int cz = -4; cz <= 4; cz += 4) {
                int checkY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x + cx, z + cz);
                BlockPos checkPos = new BlockPos(x + cx, checkY, z + cz);
                if (level.getBlockState(checkPos).liquid() || level.getBlockState(checkPos.below()).liquid()) {
                    return null;
                }
            }
        }

        for (BlockPos existing : placed) {
            double dist = Math.sqrt(
                    Math.pow(candidate.getX() - existing.getX(), 2) +
                    Math.pow(candidate.getZ() - existing.getZ(), 2)
            );
            if (dist < BUILDING_SPACING * 0.6) return null;
        }

        return candidate;
    }

    private static void buildPath(ServerLevel level, BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) return;

        for (int i = 0; i <= steps; i++) {
            int x = from.getX() + dx * i / steps;
            int z = from.getZ() + dz * i / steps;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

            for (int px = -PATH_BLOCK_RADIUS; px <= PATH_BLOCK_RADIUS; px++) {
                for (int pz = -PATH_BLOCK_RADIUS; pz <= PATH_BLOCK_RADIUS; pz++) {
                    BlockPos pathPos = new BlockPos(x + px, y - 1, z + pz);
                    if (level.isLoaded(pathPos)) {
                        BlockState current = level.getBlockState(pathPos.above());
                        if (current.isAir() || current.getBlock() == Blocks.SHORT_GRASS || current.getBlock() == Blocks.TALL_GRASS) {
                            setBlock(level, pathPos, Blocks.GRAVEL.defaultBlockState());
                            setBlock(level, pathPos.above(), Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    private static void scatterExteriorProps(ServerLevel level, BlockPos center,
                                              List<BlockPos> buildingCenters,
                                              TerritoryTier tier, RandomSource random) {
        int propCount = 3 + random.nextInt(5) + tier.getTier();

        for (int i = 0; i < propCount; i++) {
            int offsetX = random.nextInt(60) - 30;
            int offsetZ = random.nextInt(60) - 30;
            int x = center.getX() + offsetX;
            int z = center.getZ() + offsetZ;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos propPos = new BlockPos(x, y, z);

            if (!level.isLoaded(propPos)) continue;

            float roll = random.nextFloat();
            if (roll < 0.25f) {
                placeTrashPile(level, propPos, tier, random);
            } else if (roll < 0.45f) {
                placeMailbox(level, propPos, tier, random);
            } else if (roll < 0.6f) {
                placeVendingMachine(level, propPos, tier, random);
            } else {
                placeVehicleWreckage(level, propPos, random);
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
        Block[] vehicles = {
                ModBlocks.BURNT_CAR_BLOCK.get(),
                ModBlocks.BROKEN_TRUCK_BLOCK.get(),
                ModBlocks.WRECKED_CAMPER_BLOCK.get()
        };
        Block vehicle = vehicles[random.nextInt(vehicles.length)];

        setBlock(level, pos, vehicle.defaultBlockState());
        if (random.nextFloat() < 0.5f) {
            setBlock(level, pos.east(), vehicle.defaultBlockState());
        }
        if (random.nextFloat() < 0.3f) {
            setBlock(level, pos.north(), vehicle.defaultBlockState());
        }
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.isLoaded(pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
