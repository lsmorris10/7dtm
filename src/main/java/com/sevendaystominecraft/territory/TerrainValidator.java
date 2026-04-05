package com.sevendaystominecraft.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class TerrainValidator {

    private static final int RAVINE_CHECK_RADIUS = 18;
    private static final int RAVINE_DEPTH_THRESHOLD = 8;
    public static final int MAX_SLOPE_VARIANCE = 4;
    private static final int DEPRESSION_CHECK_RADIUS = 20;
    private static final int DEPRESSION_DEPTH_THRESHOLD = 6;

    public static boolean isInRavine(ServerLevel level, int x, int z, int surfaceY) {
        int totalY = 0;
        int count = 0;
        int[][] offsets = {
            {RAVINE_CHECK_RADIUS, 0}, {-RAVINE_CHECK_RADIUS, 0},
            {0, RAVINE_CHECK_RADIUS}, {0, -RAVINE_CHECK_RADIUS},
            {RAVINE_CHECK_RADIUS, RAVINE_CHECK_RADIUS}, {-RAVINE_CHECK_RADIUS, -RAVINE_CHECK_RADIUS},
            {RAVINE_CHECK_RADIUS, -RAVINE_CHECK_RADIUS}, {-RAVINE_CHECK_RADIUS, RAVINE_CHECK_RADIUS}
        };
        for (int[] offset : offsets) {
            int sy = findSolidGroundY(level, x + offset[0], z + offset[1]);
            if (sy > 0) {
                totalY += sy;
                count++;
            }
        }
        if (count == 0) return false;
        int avgSurrounding = totalY / count;
        return surfaceY < avgSurrounding - RAVINE_DEPTH_THRESHOLD;
    }

    public static boolean isInLocalDepression(ServerLevel level, int x, int z, int surfaceY) {
        int totalY = 0;
        int count = 0;
        for (int dx = -DEPRESSION_CHECK_RADIUS; dx <= DEPRESSION_CHECK_RADIUS; dx += DEPRESSION_CHECK_RADIUS) {
            for (int dz = -DEPRESSION_CHECK_RADIUS; dz <= DEPRESSION_CHECK_RADIUS; dz += DEPRESSION_CHECK_RADIUS) {
                if (dx == 0 && dz == 0) continue;
                int sy = findSolidGroundY(level, x + dx, z + dz);
                if (sy > 0) {
                    totalY += sy;
                    count++;
                }
            }
        }
        if (count == 0) return false;
        int avgSurrounding = totalY / count;
        return surfaceY < avgSurrounding - DEPRESSION_DEPTH_THRESHOLD;
    }

    public static int getSlopeVariance(ServerLevel level, int x, int z, int halfX, int halfZ) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int[][] samplePoints = {
            {-halfX, -halfZ}, {halfX, -halfZ}, {-halfX, halfZ}, {halfX, halfZ},
            {0, -halfZ}, {0, halfZ}, {-halfX, 0}, {halfX, 0},
            {0, 0}
        };
        for (int[] point : samplePoints) {
            int y = findSolidGroundY(level, x + point[0], z + point[1]);
            if (y > 0) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        if (minY == Integer.MAX_VALUE) return 0;
        return maxY - minY;
    }

    public static int getMinSurfaceY(ServerLevel level, int x, int z, int halfX, int halfZ) {
        int minY = Integer.MAX_VALUE;
        int[][] samplePoints = {
            {-halfX, -halfZ}, {halfX, -halfZ}, {-halfX, halfZ}, {halfX, halfZ}, {0, 0},
            {0, -halfZ}, {0, halfZ}, {-halfX, 0}, {halfX, 0}
        };
        for (int[] point : samplePoints) {
            int y = findSolidGroundY(level, x + point[0], z + point[1]);
            if (y > 0) {
                minY = Math.min(minY, y);
            }
        }
        return minY == Integer.MAX_VALUE ? -1 : minY;
    }

    public static int findSolidGroundY(ServerLevel level, int x, int z) {
        int heightmapY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        if (heightmapY <= 0) return heightmapY;

        for (int y = heightmapY; y > level.getMinY(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (isSolidTerrainBlock(block)) {
                return y + 1;
            }
        }
        return heightmapY;
    }

    public static boolean isSolidTerrainBlock(Block block) {
        return block == Blocks.DIRT ||
               block == Blocks.GRASS_BLOCK ||
               block == Blocks.STONE ||
               block == Blocks.DEEPSLATE ||
               block == Blocks.SAND ||
               block == Blocks.RED_SAND ||
               block == Blocks.GRAVEL ||
               block == Blocks.CLAY ||
               block == Blocks.TERRACOTTA ||
               block == Blocks.SANDSTONE ||
               block == Blocks.RED_SANDSTONE ||
               block == Blocks.COBBLESTONE ||
               block == Blocks.MOSSY_COBBLESTONE ||
               block == Blocks.ANDESITE ||
               block == Blocks.DIORITE ||
               block == Blocks.GRANITE ||
               block == Blocks.TUFF ||
               block == Blocks.CALCITE ||
               block == Blocks.PODZOL ||
               block == Blocks.MYCELIUM ||
               block == Blocks.COARSE_DIRT ||
               block == Blocks.ROOTED_DIRT ||
               block == Blocks.MUD ||
               block == Blocks.PACKED_MUD ||
               block == Blocks.SNOW_BLOCK ||
               block == Blocks.BEDROCK;
    }

    public static boolean isVegetation(Block block) {
        return block instanceof LeavesBlock ||
               block == Blocks.OAK_LOG ||
               block == Blocks.SPRUCE_LOG ||
               block == Blocks.BIRCH_LOG ||
               block == Blocks.JUNGLE_LOG ||
               block == Blocks.ACACIA_LOG ||
               block == Blocks.DARK_OAK_LOG ||
               block == Blocks.CHERRY_LOG ||
               block == Blocks.MANGROVE_LOG ||
               block == Blocks.SHORT_GRASS ||
               block == Blocks.TALL_GRASS ||
               block == Blocks.FERN ||
               block == Blocks.LARGE_FERN ||
               block == Blocks.DANDELION ||
               block == Blocks.POPPY ||
               block == Blocks.BLUE_ORCHID ||
               block == Blocks.ALLIUM ||
               block == Blocks.AZURE_BLUET ||
               block == Blocks.CORNFLOWER ||
               block == Blocks.LILY_OF_THE_VALLEY ||
               block == Blocks.SUNFLOWER ||
               block == Blocks.LILAC ||
               block == Blocks.ROSE_BUSH ||
               block == Blocks.PEONY ||
               block == Blocks.VINE ||
               block == Blocks.DEAD_BUSH ||
               block == Blocks.SWEET_BERRY_BUSH ||
               block == Blocks.MOSS_BLOCK ||
               block == Blocks.MOSS_CARPET ||
               block == Blocks.HANGING_ROOTS;
    }

    public static void clearVegetation(ServerLevel level, BlockPos base, int halfX, int halfZ, int height) {
        for (int dx = -halfX - 1; dx <= halfX + 1; dx++) {
            for (int dz = -halfZ - 1; dz <= halfZ + 1; dz++) {
                for (int dy = -2; dy < height; dy++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (isVegetation(state.getBlock())) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    public static void fillFoundationColumns(ServerLevel level, BlockPos base, int halfX, int halfZ, Block floorBlock) {
        fillFoundationColumns(level, base, halfX, halfZ, floorBlock, true);
    }

    public static void fillFoundationColumns(ServerLevel level, BlockPos base, int halfX, int halfZ, Block floorBlock, boolean placeFloor) {
        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dz = -halfZ; dz <= halfZ; dz++) {
                BlockPos floorPos = base.offset(dx, 0, dz);
                if (!level.isLoaded(floorPos)) continue;

                if (placeFloor) {
                    level.setBlock(floorPos, floorBlock.defaultBlockState(), Block.UPDATE_CLIENTS);
                }

                int maxFoundationDepth = 4;
                for (int depth = 1; depth <= maxFoundationDepth; depth++) {
                    BlockPos below = floorPos.below(depth);
                    if (!level.isLoaded(below) || below.getY() <= level.getMinY()) break;
                    BlockState existing = level.getBlockState(below);
                    if (isSolidTerrainBlock(existing.getBlock())) break;
                    Block fillBlock = depth <= 2 ? Blocks.STONE : Blocks.COBBLESTONE;
                    level.setBlock(below, fillBlock.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }
}
