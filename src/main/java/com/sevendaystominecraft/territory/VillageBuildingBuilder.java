package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;
import com.sevendaystominecraft.block.loot.LootContainerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.ArrayList;
import java.util.List;

public class VillageBuildingBuilder {

    public static class BuildingResult {
        public final BlockPos center;
        public final List<BlockPos> zombieSpawnPositions;
        public final List<BlockPos> lootPositions;
        public final List<LootContainerType> lootTypes;
        public final int sizeX;
        public final int sizeZ;

        public BuildingResult(BlockPos center, List<BlockPos> zombieSpawnPositions,
                              List<BlockPos> lootPositions, List<LootContainerType> lootTypes,
                              int sizeX, int sizeZ) {
            this.center = center;
            this.zombieSpawnPositions = zombieSpawnPositions;
            this.lootPositions = lootPositions;
            this.lootTypes = lootTypes;
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
        }
    }

    public static BuildingResult build(ServerLevel level, BlockPos origin, VillageBuildingType buildingType,
                                        TerritoryTier tier, RandomSource random) {
        int sizeX = buildingType.getMinSize() + random.nextInt(Math.max(1, buildingType.getMaxSize() - buildingType.getMinSize() + 1));
        int sizeZ = buildingType.getMinSize() + random.nextInt(Math.max(1, buildingType.getMaxSize() - buildingType.getMinSize() + 1));
        int halfX = sizeX / 2;
        int halfZ = sizeZ / 2;

        int minY = TerrainValidator.getMinSurfaceY(level, origin.getX(), origin.getZ(), halfX, halfZ);
        if (minY <= 0) {
            minY = TerrainValidator.findSolidGroundY(level, origin.getX(), origin.getZ());
        }
        BlockPos base = new BlockPos(origin.getX(), minY, origin.getZ());

        Block wallBlock = buildingType.getWallBlock();
        Block floorBlock = buildingType.getFloorBlock();
        Block roofBlock = buildingType.getRoofBlock();
        Block frameBlock = buildingType.getFrameBlock();
        int wallHeight = buildingType.getWallHeight();

        List<BlockPos> zombieSpawnPos = new ArrayList<>();
        List<BlockPos> lootPos = new ArrayList<>();
        List<LootContainerType> lootTypes = new ArrayList<>();

        boolean willHaveSecondFloor = sizeX >= 9 && sizeZ >= 9 && (buildingType == VillageBuildingType.RESIDENTIAL || random.nextFloat() < 0.3f);
        int clearHeight = willHaveSecondFloor ? wallHeight * 2 + 10 : wallHeight + 5;
        TerrainValidator.clearVegetation(level, base, halfX, halfZ, clearHeight);
        clearInterior(level, base, halfX, halfZ, clearHeight);
        TerrainValidator.fillFoundationColumns(level, base, halfX, halfZ, floorBlock);
        buildWalls(level, base, halfX, halfZ, wallHeight, wallBlock, frameBlock);
        placeWindows(level, base, halfX, halfZ, wallHeight, random);
        int doorSide = placeDoor(level, base, halfX, halfZ, buildingType, random);
        if (!willHaveSecondFloor) {
            buildPeakedRoof(level, base, halfX, halfZ, wallHeight, roofBlock);
        }

        if (sizeX >= 10 && sizeZ >= 10 && random.nextFloat() < 0.5f) {
            placeInteriorDivider(level, base, halfX, halfZ, wallHeight, wallBlock, random);
        }

        if (random.nextFloat() < 0.4f) {
            buildPorch(level, base, halfX, halfZ, floorBlock, frameBlock, random);
        }

        collectInteriorPositions(base, halfX, halfZ, zombieSpawnPos);
        placeLoot(level, base, halfX, halfZ, wallHeight, tier, buildingType, lootPos, lootTypes, random, doorSide);

        if (willHaveSecondFloor) {
            int secondFloorY = wallHeight + 1;
            buildSecondFloor(level, base, halfX, halfZ, secondFloorY, wallBlock, floorBlock, frameBlock, random);
            placeWindows(level, base.above(secondFloorY), halfX, halfZ, wallHeight, random);
            collectInteriorPositions(base.above(secondFloorY), halfX, halfZ, zombieSpawnPos);
            placeLoot(level, base.above(secondFloorY), halfX, halfZ, wallHeight, tier, buildingType, lootPos, lootTypes, random, doorSide);
            buildPeakedRoof(level, base, halfX, halfZ, secondFloorY + wallHeight, roofBlock);
        }

        return new BuildingResult(base, zombieSpawnPos, lootPos, lootTypes, sizeX, sizeZ);
    }

    private static void clearInterior(ServerLevel level, BlockPos base, int halfX, int halfZ, int height) {
        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dz = -halfZ; dz <= halfZ; dz++) {
                for (int dy = 1; dy < height; dy++) {
                    setBlock(level, base.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }


    private static void buildWalls(ServerLevel level, BlockPos base, int halfX, int halfZ,
                                    int wallHeight, Block wallBlock, Block frameBlock) {
        for (int dy = 1; dy <= wallHeight; dy++) {
            for (int dx = -halfX; dx <= halfX; dx++) {
                boolean isCorner = (dx == -halfX || dx == halfX);
                Block block = isCorner ? frameBlock : wallBlock;
                setBlock(level, base.offset(dx, dy, -halfZ), block.defaultBlockState());
                setBlock(level, base.offset(dx, dy, halfZ), block.defaultBlockState());
            }
            for (int dz = -halfZ + 1; dz < halfZ; dz++) {
                setBlock(level, base.offset(-halfX, dy, dz), wallBlock.defaultBlockState());
                setBlock(level, base.offset(halfX, dy, dz), wallBlock.defaultBlockState());
            }
        }

        for (int dx = -halfX; dx <= halfX; dx++) {
            boolean isCorner = (dx == -halfX || dx == halfX);
            Block block = isCorner ? frameBlock : wallBlock;
            setBlock(level, base.offset(dx, 1, -halfZ), block.defaultBlockState());
            setBlock(level, base.offset(dx, 1, halfZ), block.defaultBlockState());
        }
    }

    private static void placeWindows(ServerLevel level, BlockPos base, int halfX, int halfZ,
                                      int wallHeight, RandomSource random) {
        int windowY = 2;
        if (wallHeight < 3) return;

        for (int dx = -halfX + 2; dx <= halfX - 2; dx += 3) {
            if (random.nextFloat() < 0.7f) {
                setBlock(level, base.offset(dx, windowY, -halfZ), Blocks.GLASS_PANE.defaultBlockState());
                if (wallHeight > 3) {
                    setBlock(level, base.offset(dx, windowY + 1, -halfZ), Blocks.GLASS_PANE.defaultBlockState());
                }
            }
            if (random.nextFloat() < 0.7f) {
                setBlock(level, base.offset(dx, windowY, halfZ), Blocks.GLASS_PANE.defaultBlockState());
                if (wallHeight > 3) {
                    setBlock(level, base.offset(dx, windowY + 1, halfZ), Blocks.GLASS_PANE.defaultBlockState());
                }
            }
        }

        for (int dz = -halfZ + 2; dz <= halfZ - 2; dz += 3) {
            if (random.nextFloat() < 0.7f) {
                setBlock(level, base.offset(-halfX, windowY, dz), Blocks.GLASS_PANE.defaultBlockState());
            }
            if (random.nextFloat() < 0.7f) {
                setBlock(level, base.offset(halfX, windowY, dz), Blocks.GLASS_PANE.defaultBlockState());
            }
        }
    }

    private static int placeDoor(ServerLevel level, BlockPos base, int halfX, int halfZ,
                                   VillageBuildingType buildingType, RandomSource random) {
        int side = random.nextInt(4);
        BlockPos doorBase;
        Direction facing;

        switch (side) {
            case 0 -> { doorBase = base.offset(0, 1, -halfZ); facing = Direction.SOUTH; }
            case 1 -> { doorBase = base.offset(0, 1, halfZ); facing = Direction.NORTH; }
            case 2 -> { doorBase = base.offset(-halfX, 1, 0); facing = Direction.EAST; }
            default -> { doorBase = base.offset(halfX, 1, 0); facing = Direction.WEST; }
        }

        Block doorBlock = getDoorBlock(buildingType);
        BlockState lowerDoor = doorBlock.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.FACING, facing);
        BlockState upperDoor = doorBlock.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.FACING, facing);
        setBlock(level, doorBase, lowerDoor);
        setBlock(level, doorBase.above(), upperDoor);
        return side;
    }

    private static Block getDoorBlock(VillageBuildingType type) {
        return switch (type) {
            case RESIDENTIAL, FARM -> Blocks.OAK_DOOR;
            case CRACK_A_BOOK, POP_N_PILLS -> Blocks.BIRCH_DOOR;
            case WORKING_STIFFS, UTILITY -> Blocks.IRON_DOOR;
            case PASS_N_GAS -> Blocks.SPRUCE_DOOR;
            case TRADER_OUTPOST -> Blocks.DARK_OAK_DOOR;
        };
    }

    private static void buildPeakedRoof(ServerLevel level, BlockPos base, int halfX, int halfZ,
                                         int wallHeight, Block roofBlock) {
        int roofY = wallHeight + 1;
        int peakHeight = Math.min(halfX, halfZ) / 2 + 1;

        for (int layer = 0; layer < peakHeight; layer++) {
            int innerX = halfX - layer;
            int innerZ = halfZ - layer;
            if (innerX < 0 || innerZ < 0) break;

            for (int dx = -innerX; dx <= innerX; dx++) {
                for (int dz = -innerZ; dz <= innerZ; dz++) {
                    boolean isEdge = (dx == -innerX || dx == innerX || dz == -innerZ || dz == innerZ);
                    if (layer < peakHeight - 1 && !isEdge) continue;
                    setBlock(level, base.offset(dx, roofY + layer, dz), roofBlock.defaultBlockState());
                }
            }
        }

        int topY = roofY + peakHeight - 1;
        int topInnerX = halfX - (peakHeight - 1);
        int topInnerZ = halfZ - (peakHeight - 1);
        if (topInnerX >= 0 && topInnerZ >= 0) {
            for (int dx = -topInnerX; dx <= topInnerX; dx++) {
                for (int dz = -topInnerZ; dz <= topInnerZ; dz++) {
                    setBlock(level, base.offset(dx, topY, dz), roofBlock.defaultBlockState());
                }
            }
        }
    }

    private static void placeInteriorDivider(ServerLevel level, BlockPos base, int halfX, int halfZ,
                                              int wallHeight, Block wallBlock, RandomSource random) {
        boolean divideX = random.nextBoolean();
        if (divideX) {
            int divZ = random.nextInt(halfZ) - halfZ / 2;
            for (int dx = -halfX + 1; dx < halfX; dx++) {
                for (int dy = 1; dy <= wallHeight; dy++) {
                    setBlock(level, base.offset(dx, dy, divZ), wallBlock.defaultBlockState());
                }
            }
            int doorX = -halfX + 1 + random.nextInt(Math.max(1, 2 * halfX - 2));
            setBlock(level, base.offset(doorX, 1, divZ), Blocks.AIR.defaultBlockState());
            setBlock(level, base.offset(doorX, 2, divZ), Blocks.AIR.defaultBlockState());
        } else {
            int divX = random.nextInt(halfX) - halfX / 2;
            for (int dz = -halfZ + 1; dz < halfZ; dz++) {
                for (int dy = 1; dy <= wallHeight; dy++) {
                    setBlock(level, base.offset(divX, dy, dz), wallBlock.defaultBlockState());
                }
            }
            int doorZ = -halfZ + 1 + random.nextInt(Math.max(1, 2 * halfZ - 2));
            setBlock(level, base.offset(divX, 1, doorZ), Blocks.AIR.defaultBlockState());
            setBlock(level, base.offset(divX, 2, doorZ), Blocks.AIR.defaultBlockState());
        }
    }

    private static void buildPorch(ServerLevel level, BlockPos base, int halfX, int halfZ,
                                    Block floorBlock, Block frameBlock, RandomSource random) {
        int side = random.nextInt(4);
        int porchDepth = 2;

        switch (side) {
            case 0 -> {
                for (int dx = -halfX; dx <= halfX; dx++) {
                    for (int dz = -halfZ - porchDepth; dz < -halfZ; dz++) {
                        setBlock(level, base.offset(dx, 0, dz), floorBlock.defaultBlockState());
                    }
                }
                setBlock(level, base.offset(-halfX, 1, -halfZ - porchDepth), frameBlock.defaultBlockState());
                setBlock(level, base.offset(halfX, 1, -halfZ - porchDepth), frameBlock.defaultBlockState());
                setBlock(level, base.offset(-halfX, 2, -halfZ - porchDepth), frameBlock.defaultBlockState());
                setBlock(level, base.offset(halfX, 2, -halfZ - porchDepth), frameBlock.defaultBlockState());
            }
            case 1 -> {
                for (int dx = -halfX; dx <= halfX; dx++) {
                    for (int dz = halfZ + 1; dz <= halfZ + porchDepth; dz++) {
                        setBlock(level, base.offset(dx, 0, dz), floorBlock.defaultBlockState());
                    }
                }
                setBlock(level, base.offset(-halfX, 1, halfZ + porchDepth), frameBlock.defaultBlockState());
                setBlock(level, base.offset(halfX, 1, halfZ + porchDepth), frameBlock.defaultBlockState());
                setBlock(level, base.offset(-halfX, 2, halfZ + porchDepth), frameBlock.defaultBlockState());
                setBlock(level, base.offset(halfX, 2, halfZ + porchDepth), frameBlock.defaultBlockState());
            }
            case 2 -> {
                for (int dz = -halfZ; dz <= halfZ; dz++) {
                    for (int dx = -halfX - porchDepth; dx < -halfX; dx++) {
                        setBlock(level, base.offset(dx, 0, dz), floorBlock.defaultBlockState());
                    }
                }
            }
            default -> {
                for (int dz = -halfZ; dz <= halfZ; dz++) {
                    for (int dx = halfX + 1; dx <= halfX + porchDepth; dx++) {
                        setBlock(level, base.offset(dx, 0, dz), floorBlock.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void buildSecondFloor(ServerLevel level, BlockPos base, int halfX, int halfZ,
                                           int floorLevel, Block wallBlock, Block floorBlock,
                                           Block frameBlock, RandomSource random) {
        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dz = -halfZ; dz <= halfZ; dz++) {
                setBlock(level, base.offset(dx, floorLevel, dz), floorBlock.defaultBlockState());
            }
        }

        int secondWallHeight = 4;
        for (int dy = 1; dy <= secondWallHeight; dy++) {
            int y = floorLevel + dy;
            for (int dx = -halfX; dx <= halfX; dx++) {
                for (int dz = -halfZ; dz <= halfZ; dz++) {
                    boolean isWall = (dx == -halfX || dx == halfX || dz == -halfZ || dz == halfZ);
                    if (isWall) {
                        boolean isCorner = (dx == -halfX || dx == halfX) && (dz == -halfZ || dz == halfZ);
                        setBlock(level, base.offset(dx, y, dz),
                                (isCorner ? frameBlock : wallBlock).defaultBlockState());
                    } else {
                        setBlock(level, base.offset(dx, y, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        int stairX = -halfX + 2;
        int stairZ = -halfZ + 1;
        for (int step = 0; step < 4 && step < halfZ; step++) {
            BlockPos stepPos = base.offset(stairX, floorLevel - 4 + step + 1, stairZ + step);
            setBlock(level, stepPos, Blocks.OAK_STAIRS.defaultBlockState());
            setBlock(level, stepPos.above(), Blocks.AIR.defaultBlockState());
            setBlock(level, stepPos.above(2), Blocks.AIR.defaultBlockState());
        }
        for (int step = 0; step < 4 && step < halfZ; step++) {
            BlockPos holePos = base.offset(stairX, floorLevel, stairZ + step);
            setBlock(level, holePos, Blocks.AIR.defaultBlockState());
        }
    }

    private static void collectInteriorPositions(BlockPos base, int halfX, int halfZ,
                                                  List<BlockPos> spawnPositions) {
        int innerHalfX = halfX - 1;
        int innerHalfZ = halfZ - 1;
        if (innerHalfX <= 0 || innerHalfZ <= 0) {
            spawnPositions.add(base.above(1));
            return;
        }
        for (int dx = -innerHalfX; dx <= innerHalfX; dx += 2) {
            for (int dz = -innerHalfZ; dz <= innerHalfZ; dz += 2) {
                spawnPositions.add(base.offset(dx, 1, dz));
            }
        }
    }

    private static void placeLoot(ServerLevel level, BlockPos base, int halfX, int halfZ, int wallHeight,
                                   TerritoryTier tier, VillageBuildingType buildingType,
                                   List<BlockPos> lootPos, List<LootContainerType> lootTypes,
                                   RandomSource random, int doorSide) {
        int innerHalfX = halfX - 1;
        int innerHalfZ = halfZ - 1;
        if (innerHalfX <= 0 || innerHalfZ <= 0) return;

        int count = buildingType.getLootCount(tier.getTier());
        int wallCycle = 0;
        for (int i = 0; i < count; i++) {
            int attempts = 0;
            LootContainerType lootType = buildingType.pickLoot(random);

            while (attempts < 20) {
                int dx, dz;
                if (lootType.isWallFurniture()) {
                    int wall = (wallCycle + random.nextInt(2)) % 4;
                    int freeRangeX = Math.max(1, innerHalfX - 1);
                    int freeRangeZ = Math.max(1, innerHalfZ - 1);
                    switch (wall) {
                        case 0 -> { dz = -innerHalfZ; dx = random.nextInt(freeRangeX * 2 + 1) - freeRangeX; }
                        case 1 -> { dz = innerHalfZ;  dx = random.nextInt(freeRangeX * 2 + 1) - freeRangeX; }
                        case 2 -> { dx = -innerHalfX; dz = random.nextInt(freeRangeZ * 2 + 1) - freeRangeZ; }
                        default -> { dx = innerHalfX;  dz = random.nextInt(freeRangeZ * 2 + 1) - freeRangeZ; }
                    }
                    if (wall == doorSide && ((wall <= 1 && dx == 0) || (wall >= 2 && dz == 0))) {
                        attempts++;
                        continue;
                    }
                    wallCycle = (wall + 1) % 4;
                } else {
                    dx = random.nextInt(innerHalfX * 2 + 1) - innerHalfX;
                    dz = random.nextInt(innerHalfZ * 2 + 1) - innerHalfZ;
                }
                BlockPos pos = base.offset(dx, 1, dz);
                if (!lootPos.contains(pos)) {
                    Block lootBlock = TerritoryStructureBuilder.getLootBlock(lootType);
                    if (lootBlock != null) {
                        setBlock(level, pos, lootBlock.defaultBlockState());
                        if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity be) {
                            be.setTerritoryTier(tier.getTier());
                        }
                        lootPos.add(pos);
                        lootTypes.add(lootType);
                    }
                    break;
                }
                attempts++;
            }
        }
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.isLoaded(pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
