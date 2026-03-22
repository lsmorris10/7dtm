package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.SpiderZombie;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

public class BlockBreakPathEvaluator {

    private static final int MAX_SEARCH_DEPTH = 16;
    private static final int CACHE_TTL_TICKS = 40;
    private static final float TIME_MULTIPLIER = 0.5f;
    private static final int MAX_CLIMB_HEIGHT = 16;
    private static final double DIAGONAL_COST = 1.414;
    private static final int MAX_FALL_DISTANCE = 3;

    private final BaseSevenDaysZombie zombie;
    private List<BlockPos> cachedPath;
    private long cacheTimestamp;
    private BlockPos cachedStart;
    private BlockPos cachedGoal;

    public BlockBreakPathEvaluator(BaseSevenDaysZombie zombie) {
        this.zombie = zombie;
        this.cachedPath = null;
        this.cacheTimestamp = -1;
        this.cachedStart = null;
        this.cachedGoal = null;
    }

    public List<BlockPos> findBreakPath(BlockPos start, BlockPos goal) {
        long currentTick = zombie.level().getGameTime();
        boolean withinCooldown = (currentTick - cacheTimestamp) < CACHE_TTL_TICKS;

        if (withinCooldown && cachedPath != null
                && start.equals(cachedStart) && goal.equals(cachedGoal)) {
            return cachedPath;
        }

        if (withinCooldown) {
            return cachedPath != null ? cachedPath : Collections.emptyList();
        }

        List<BlockPos> result = computeAStarPath(start, goal);
        cachedPath = result;
        cacheTimestamp = currentTick;
        cachedStart = start;
        cachedGoal = goal;
        return result;
    }

    public void invalidateCache() {
        cachedPath = null;
        cachedStart = null;
        cachedGoal = null;
    }

    private List<BlockPos> computeAStarPath(BlockPos start, BlockPos goal) {
        Level level = zombie.level();
        float attackDamage = (float) zombie.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (attackDamage <= 0) attackDamage = 1.0f;
        boolean isSpider = zombie instanceof SpiderZombie;

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<BlockPos, AStarNode> allNodes = new HashMap<>();

        AStarNode startNode = new AStarNode(start, 0, heuristic(start, goal), null);
        openSet.add(startNode);
        allNodes.put(start, startNode);

        int iterations = 0;
        int maxIterations = MAX_SEARCH_DEPTH * MAX_SEARCH_DEPTH * 8;

        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;
            AStarNode current = openSet.poll();

            if (current.pos.closerThan(goal, 2.0)) {
                return reconstructBreakablePath(current, level);
            }

            if (current.depth >= MAX_SEARCH_DEPTH) {
                continue;
            }

            for (NeighborEdge edge : getNeighbors(current.pos, level, attackDamage, isSpider)) {
                double tentativeG = current.gCost + edge.cost;
                AStarNode existing = allNodes.get(edge.pos);

                if (existing == null || tentativeG < existing.gCost) {
                    AStarNode neighbor = new AStarNode(edge.pos, tentativeG,
                            tentativeG + heuristic(edge.pos, goal), current);
                    neighbor.depth = current.depth + 1;
                    allNodes.put(edge.pos, neighbor);
                    openSet.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<NeighborEdge> getNeighbors(BlockPos pos, Level level, float attackDamage, boolean isSpider) {
        List<NeighborEdge> neighbors = new ArrayList<>();

        int[][] cardinalOffsets = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        int[][] diagonalOffsets = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] offset : cardinalOffsets) {
            addCardinalEdges(neighbors, pos, offset, level, attackDamage);
        }

        for (int[] offset : diagonalOffsets) {
            addDiagonalEdges(neighbors, pos, offset, level, attackDamage);
        }

        if (isSpider) {
            addSpiderClimbEdges(neighbors, pos, level, cardinalOffsets);
        }

        return neighbors;
    }

    private void addCardinalEdges(List<NeighborEdge> neighbors, BlockPos pos, int[] offset,
                                  Level level, float attackDamage) {
        BlockPos neighborBase = pos.offset(offset[0], 0, offset[1]);
        tryAddWalkableEdge(neighbors, neighborBase, level, attackDamage, 1.0);

        BlockPos sameLevel = pos.offset(offset[0], 0, offset[1]);
        BlockState sameLevelState = level.getBlockState(sameLevel);
        if (isSolidOrBreakable(sameLevelState)) {
            BlockPos stepUp = pos.offset(offset[0], 1, offset[1]);
            BlockState headAboveStepUp = level.getBlockState(stepUp.above());
            BlockState originHead = level.getBlockState(pos.above(2));
            if ((isPassable(headAboveStepUp) || BlockHPRegistry.isBreakable(headAboveStepUp))
                    && (isPassable(originHead) || BlockHPRegistry.isBreakable(originHead))) {
                tryAddWalkableEdge(neighbors, stepUp, level, attackDamage, 1.4);
            }
        }

        if (isPassable(level.getBlockState(sameLevel))) {
            for (int fall = 1; fall <= MAX_FALL_DISTANCE; fall++) {
                BlockPos below = pos.offset(offset[0], -fall, offset[1]);
                BlockState groundState = level.getBlockState(below.below());
                if (isSolidOrBreakable(groundState)) {
                    tryAddWalkableEdge(neighbors, below, level, attackDamage, 1.0 + fall * 0.5);
                    break;
                }
                BlockState belowState = level.getBlockState(below);
                if (!isPassable(belowState) && !BlockHPRegistry.isBreakable(belowState)) {
                    break;
                }
            }
        }
    }

    private void addDiagonalEdges(List<NeighborEdge> neighbors, BlockPos pos, int[] offset,
                                  Level level, float attackDamage) {
        BlockPos sideA = pos.offset(offset[0], 0, 0);
        BlockPos sideB = pos.offset(0, 0, offset[1]);
        BlockState sideAFeet = level.getBlockState(sideA);
        BlockState sideAHead = level.getBlockState(sideA.above());
        BlockState sideBFeet = level.getBlockState(sideB);
        BlockState sideBHead = level.getBlockState(sideB.above());

        boolean sideAClear = (isPassable(sideAFeet) || BlockHPRegistry.isBreakable(sideAFeet))
                && (isPassable(sideAHead) || BlockHPRegistry.isBreakable(sideAHead));
        boolean sideBClear = (isPassable(sideBFeet) || BlockHPRegistry.isBreakable(sideBFeet))
                && (isPassable(sideBHead) || BlockHPRegistry.isBreakable(sideBHead));

        if (!sideAClear && !sideBClear) {
            return;
        }

        BlockPos neighborBase = pos.offset(offset[0], 0, offset[1]);
        tryAddWalkableEdge(neighbors, neighborBase, level, attackDamage, DIAGONAL_COST);
    }

    private void tryAddWalkableEdge(List<NeighborEdge> neighbors, BlockPos neighborPos,
                                    Level level, float attackDamage, double baseCost) {
        if (!hasGroundSupport(neighborPos, level)) {
            return;
        }

        BlockState feetState = level.getBlockState(neighborPos);
        BlockState headState = level.getBlockState(neighborPos.above());

        double totalCost = baseCost;
        boolean passable = true;

        if (!isPassable(feetState)) {
            if (BlockHPRegistry.isBreakable(feetState)) {
                totalCost += computeBreakCost(feetState, attackDamage);
            } else {
                passable = false;
            }
        }

        if (!isPassable(headState)) {
            if (BlockHPRegistry.isBreakable(headState)) {
                totalCost += computeBreakCost(headState, attackDamage);
            } else {
                passable = false;
            }
        }

        if (passable) {
            PathType vanillaType = WalkNodeEvaluator.getPathTypeStatic(zombie, neighborPos);
            if (vanillaType == PathType.DAMAGE_FIRE || vanillaType == PathType.DAMAGE_OTHER
                    || vanillaType == PathType.DANGER_FIRE || vanillaType == PathType.LAVA) {
                totalCost += 8.0;
            }

            neighbors.add(new NeighborEdge(neighborPos, totalCost));
        }
    }

    private boolean hasGroundSupport(BlockPos pos, Level level) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return !isPassable(belowState) || !belowState.getFluidState().isEmpty();
    }

    private boolean isSolidOrBreakable(BlockState state) {
        if (isPassable(state)) return false;
        return true;
    }

    private void addSpiderClimbEdges(List<NeighborEdge> neighbors, BlockPos pos,
                                     Level level, int[][] cardinalOffsets) {
        for (int dy = 1; dy <= MAX_CLIMB_HEIGHT; dy++) {
            BlockPos above = pos.above(dy);
            BlockState aboveState = level.getBlockState(above);

            if (!aboveState.isAir() && !isPassable(aboveState)) {
                break;
            }

            boolean hasAdjacentWall = false;
            for (int[] offset : cardinalOffsets) {
                BlockPos wallCheck = above.offset(offset[0], 0, offset[1]);
                BlockState wallState = level.getBlockState(wallCheck);
                if (!wallState.isAir() && !isPassable(wallState)) {
                    hasAdjacentWall = true;
                    break;
                }
            }
            if (!hasAdjacentWall) continue;

            boolean pathClear = true;
            for (int checkY = 1; checkY < dy; checkY++) {
                BlockState midState = level.getBlockState(pos.above(checkY));
                if (!midState.isAir() && !isPassable(midState)) {
                    pathClear = false;
                    break;
                }
            }
            if (pathClear) {
                neighbors.add(new NeighborEdge(above, dy * 0.5));
            }
        }
    }

    private double computeBreakCost(BlockState state, float attackDamage) {
        float blockHP = BlockHPRegistry.getBlockHP(state);
        if (blockHP <= 0) return Double.MAX_VALUE / 2;
        return (blockHP / attackDamage) * TIME_MULTIPLIER;
    }

    private boolean isPassable(BlockState state) {
        if (state.isAir()) return true;
        if (!state.getFluidState().isEmpty() && !state.isSolid()) return true;
        return !state.blocksMotion();
    }

    private double heuristic(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        int min = Math.min(dx, dz);
        int max = Math.max(dx, dz);
        return (max - min) + min * DIAGONAL_COST + dy;
    }

    private List<BlockPos> reconstructBreakablePath(AStarNode endNode, Level level) {
        List<BlockPos> breakableBlocks = new ArrayList<>();
        AStarNode current = endNode;

        while (current != null) {
            BlockState feetState = level.getBlockState(current.pos);
            BlockState headState = level.getBlockState(current.pos.above());

            if (!isPassable(feetState) && BlockHPRegistry.isBreakable(feetState)) {
                breakableBlocks.add(current.pos);
            }
            if (!isPassable(headState) && BlockHPRegistry.isBreakable(headState)) {
                breakableBlocks.add(current.pos.above());
            }

            current = current.parent;
        }

        Collections.reverse(breakableBlocks);
        return breakableBlocks;
    }

    private static class AStarNode {
        final BlockPos pos;
        final double gCost;
        final double fCost;
        final AStarNode parent;
        int depth;

        AStarNode(BlockPos pos, double gCost, double fCost, AStarNode parent) {
            this.pos = pos;
            this.gCost = gCost;
            this.fCost = fCost;
            this.parent = parent;
            this.depth = 0;
        }
    }

    private static class NeighborEdge {
        final BlockPos pos;
        final double cost;

        NeighborEdge(BlockPos pos, double cost) {
            this.pos = pos;
            this.cost = cost;
        }
    }
}
