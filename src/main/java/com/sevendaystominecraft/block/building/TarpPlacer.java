package com.sevendaystominecraft.block.building;

import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that calculates and places a rectangular tarp canopy
 * between four corner tarp post positions.
 * <p>
 * The 4 posts define the bounding rectangle. Posts can be at different
 * heights — the tarp is placed at the Y-level one block above the
 * highest post. Sag levels are calculated based on distance from the
 * nearest edge, creating a realistic droop toward the center. If a
 * center pole exists below the tarp, the sag inverts (tent mode).
 */
public class TarpPlacer {

    /** Maximum side length for each tarp tier. */
    private static final int[] MAX_SIDE = {3, 6, 15};

    /**
     * Attempts to place a tarp canopy using 4 corner posts.
     * The rectangles bounding box is computed from the 4 positions.
     *
     * @param level    the world
     * @param corners  list of exactly 4 corner post positions (top of each stack)
     * @param tarpTier 1=small(3×3), 2=medium(6×6), 3=large(15×15)
     * @return true if placed successfully
     */
    public static boolean tryPlaceTarp(Level level, List<BlockPos> corners, int tarpTier) {
        if (corners.size() != 4) return false;
        if (tarpTier < 1 || tarpTier > 3) return false;

        int maxSide = MAX_SIDE[tarpTier - 1];

        // Calculate the bounding rectangle from all 4 corners
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (BlockPos corner : corners) {
            minX = Math.min(minX, corner.getX());
            maxX = Math.max(maxX, corner.getX());
            minZ = Math.min(minZ, corner.getZ());
            maxZ = Math.max(maxZ, corner.getZ());
            maxY = Math.max(maxY, corner.getY());
        }

        int sizeX = maxX - minX + 1;
        int sizeZ = maxZ - minZ + 1;

        // Validate dimensions
        if (sizeX > maxSide || sizeZ > maxSide) return false;
        if (sizeX < 2 || sizeZ < 2) return false; // Must span at least 2 in both axes

        // Validate that posts actually form the 4 corners of the rectangle.
        // Each actual corner of the bounding box must have a post column nearby.
        boolean hasCornerNW = false, hasCornerNE = false, hasCornerSW = false, hasCornerSE = false;
        for (BlockPos corner : corners) {
            int cx = corner.getX();
            int cz = corner.getZ();
            if (cx == minX && cz == minZ) hasCornerNW = true;
            if (cx == maxX && cz == minZ) hasCornerNE = true;
            if (cx == minX && cz == maxZ) hasCornerSW = true;
            if (cx == maxX && cz == maxZ) hasCornerSE = true;
        }
        if (!(hasCornerNW && hasCornerNE && hasCornerSW && hasCornerSE)) {
            return false; // Posts don't form a proper rectangle
        }

        // Tarp placed one block above the highest corner post
        int tarpY = maxY + 1;

        // Check that all positions are replaceable (air or similar)
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos checkPos = new BlockPos(x, tarpY, z);
                BlockState existing = level.getBlockState(checkPos);
                if (!existing.isAir() && !existing.canBeReplaced()) {
                    return false;
                }
            }
        }

        // Check for center pole (tarp post at or below center position)
        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;
        boolean hasCenterPole = false;
        for (int y = tarpY - 1; y >= tarpY - 3; y--) {
            BlockPos checkCenter = new BlockPos(centerX, y, centerZ);
            if (level.getBlockState(checkCenter).getBlock() instanceof TarpPostBlock) {
                hasCenterPole = true;
                break;
            }
        }

        // Collect all positions for sibling tracking
        List<BlockPos> allPositions = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                allPositions.add(new BlockPos(x, tarpY, z));
            }
        }

        // Place tarp blocks with calculated sag
        BlockState tarpState = ModBlocks.TARP_BLOCK.get().defaultBlockState();
        for (BlockPos tarpPos : allPositions) {
            int sag = calculateSag(tarpPos.getX(), tarpPos.getZ(),
                    minX, maxX, minZ, maxZ, hasCenterPole, centerX, centerZ);
            level.setBlock(tarpPos, tarpState.setValue(TarpBlock.SAG_LEVEL, sag), 3);

            // Set up block entity with sibling tracking
            if (level.getBlockEntity(tarpPos) instanceof TarpBlock.TarpBlockEntity tarpBE) {
                tarpBE.setSiblingPositions(allPositions);
                tarpBE.setTarpTier(tarpTier);
            }
        }

        return true;
    }

    /**
     * Calculates sag level for a position based on distance from edges.
     * If hasCenterPole is true, inverts the pattern (tent mode).
     */
    private static int calculateSag(int x, int z, int minX, int maxX, int minZ, int maxZ,
                                      boolean hasCenterPole, int centerX, int centerZ) {
        int distFromEdge = Math.min(
                Math.min(x - minX, maxX - x),
                Math.min(z - minZ, maxZ - z)
        );

        if (hasCenterPole) {
            // Tent mode: center is highest (sag=0), edges droop
            int distFromCenter = Math.max(Math.abs(x - centerX), Math.abs(z - centerZ));
            return Math.min(3, distFromCenter);
        } else {
            // Normal sag: edges are highest (sag=0), center droops
            return Math.min(3, distFromEdge);
        }
    }

    /**
     * Returns the tarp item stack for the given tier.
     */
    public static ItemStack getTarpItemForTier(int tier) {
        return switch (tier) {
            case 1 -> new ItemStack(ModItems.TARP_SMALL.get());
            case 2 -> new ItemStack(ModItems.TARP_MEDIUM.get());
            case 3 -> new ItemStack(ModItems.TARP_LARGE.get());
            default -> ItemStack.EMPTY;
        };
    }
}
