package com.sevendaystominecraft.block.structural;

import com.mojang.logging.LogUtils;
import com.sevendaystominecraft.config.StructuralIntegrityConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class StructuralIntegrityManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] HORIZONTAL_DIRS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };
    private static final int MAX_BFS_RANGE = 64;

    private static final List<PendingCollapse> PENDING_COLLAPSES = new ArrayList<>();

    private StructuralIntegrityManager() {}

    public static void onBlockBroken(ServerLevel level, BlockPos brokenPos) {
        if (!StructuralIntegrityConfig.INSTANCE.enabled.get()) return;

        int delay = StructuralIntegrityConfig.INSTANCE.collapseDelayTicks.get();

        Set<BlockPos> unsupported = findUnsupportedBlocks(level, brokenPos);
        if (!unsupported.isEmpty()) {
            scheduleCollapse(level, unsupported, delay);
        }
    }

    public static void onServerTick(ServerLevel level) {
        if (PENDING_COLLAPSES.isEmpty()) return;

        long currentTick = level.getGameTime();
        Iterator<PendingCollapse> it = PENDING_COLLAPSES.iterator();
        List<PendingCollapse> ready = new ArrayList<>();

        while (it.hasNext()) {
            PendingCollapse pending = it.next();
            if (pending.level == level && currentTick >= pending.executeTick) {
                ready.add(pending);
                it.remove();
            }
        }

        for (PendingCollapse collapse : ready) {
            executeCollapse(collapse.level, collapse.blocksToCollapse);
        }
    }

    public static void clearPending() {
        PENDING_COLLAPSES.clear();
    }

    static Set<BlockPos> findUnsupportedBlocks(ServerLevel level, BlockPos brokenPos) {
        Set<BlockPos> candidates = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = brokenPos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (StructuralIntegrityRegistry.isStructuralBlock(neighborState)) {
                queue.add(neighbor.immutable());
            }
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) continue;
            if (current.distManhattan(brokenPos) > MAX_BFS_RANGE) continue;

            BlockState state = level.getBlockState(current);
            if (!StructuralIntegrityRegistry.isStructuralBlock(state)) continue;

            candidates.add(current);

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (!visited.contains(next)) {
                    BlockState nextState = level.getBlockState(next);
                    if (StructuralIntegrityRegistry.isStructuralBlock(nextState)) {
                        queue.add(next.immutable());
                    }
                }
            }
        }

        Set<BlockPos> unsupported = new HashSet<>();
        for (BlockPos candidate : candidates) {
            if (!hasGroundSupport(level, candidate, brokenPos, candidates)) {
                if (!hasSufficientHorizontalSupport(level, candidate, brokenPos, candidates)) {
                    unsupported.add(candidate);
                }
            }
        }

        return unsupported;
    }

    private static boolean hasGroundSupport(ServerLevel level, BlockPos pos, BlockPos brokenPos,
                                             Set<BlockPos> candidateRegion) {
        Set<BlockPos> visitedDown = new HashSet<>();
        Queue<BlockPos> downQueue = new ArrayDeque<>();
        downQueue.add(pos);

        while (!downQueue.isEmpty()) {
            BlockPos current = downQueue.poll();
            if (!visitedDown.add(current)) continue;

            if (current.equals(brokenPos)) continue;

            BlockPos below = current.below();

            if (below.getY() < level.getMinY()) return true;

            if (below.equals(brokenPos)) continue;

            BlockState belowState = level.getBlockState(below);

            if (belowState.isAir() || belowState.liquid()) continue;

            if (StructuralIntegrityRegistry.isStructuralBlock(belowState)) {
                if (!candidateRegion.contains(below)) {
                    if (hasColumnToFoundation(level, below, brokenPos)) return true;
                } else {
                    downQueue.add(below);
                }
            } else if (isFullSolidBlock(level, belowState, below)) {
                if (hasColumnToFoundation(level, below, brokenPos)) return true;
            }
        }

        return false;
    }

    private static boolean hasColumnToFoundation(ServerLevel level, BlockPos pos, BlockPos brokenPos) {
        BlockPos current = pos.below();
        while (current.getY() >= level.getMinY()) {
            if (current.equals(brokenPos)) return false;
            BlockState state = level.getBlockState(current);
            if (state.isAir() || state.liquid()) return false;
            if (!isFullSolidBlock(level, state, current)
                    && !StructuralIntegrityRegistry.isStructuralBlock(state)) return false;
            current = current.below();
        }
        return true;
    }

    private static boolean isFullSolidBlock(ServerLevel level, BlockState state, BlockPos pos) {
        return state.isSolidRender() || StructuralIntegrityRegistry.isGroundBlock(state);
    }

    private static boolean hasSufficientHorizontalSupport(ServerLevel level, BlockPos pos,
                                                           BlockPos brokenPos, Set<BlockPos> candidateRegion) {
        BlockState state = level.getBlockState(pos);
        StructuralIntegrityRegistry.SIMaterial material = StructuralIntegrityRegistry.getMaterial(state);
        if (material == null) return false;

        int maxSpan = material.horizontalSpan;

        for (Direction dir : HORIZONTAL_DIRS) {
            int accumulatedWeight = material.weight;
            boolean foundSupport = false;
            BlockPos current = pos;

            for (int i = 0; i < maxSpan; i++) {
                current = current.relative(dir);

                if (current.equals(brokenPos)) break;

                BlockState neighborState = level.getBlockState(current);
                if (!StructuralIntegrityRegistry.isStructuralBlock(neighborState)) break;

                StructuralIntegrityRegistry.SIMaterial neighborMat =
                        StructuralIntegrityRegistry.getMaterial(neighborState);
                if (neighborMat == null) break;

                if (accumulatedWeight > neighborMat.maxSupport) break;

                if (!candidateRegion.contains(current) || hasDirectGroundColumn(level, current, brokenPos)) {
                    foundSupport = true;
                    break;
                }

                accumulatedWeight += neighborMat.weight;
            }

            if (foundSupport) return true;
        }

        return false;
    }

    private static boolean hasDirectGroundColumn(ServerLevel level, BlockPos pos, BlockPos brokenPos) {
        return hasColumnToFoundation(level, pos, brokenPos);
    }

    private static void scheduleCollapse(ServerLevel level, Set<BlockPos> blocks, int delayTicks) {
        Map<BlockPos, BlockState> snapshot = new HashMap<>();
        for (BlockPos pos : blocks) {
            snapshot.put(pos.immutable(), level.getBlockState(pos));
        }

        long executeTick = level.getGameTime() + delayTicks;
        PENDING_COLLAPSES.add(new PendingCollapse(level, snapshot, executeTick));
    }

    private static void executeCollapse(ServerLevel level, Map<BlockPos, BlockState> blocksToCollapse) {
        Set<BlockPos> collapsedPositions = new HashSet<>();

        for (Map.Entry<BlockPos, BlockState> entry : blocksToCollapse.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState expectedState = entry.getValue();

            BlockState currentState = level.getBlockState(pos);
            if (!currentState.equals(expectedState)) continue;

            collapseBlock(level, pos, currentState);
            collapsedPositions.add(pos);
        }

        if (!collapsedPositions.isEmpty()) {
            int cascadeDelay = StructuralIntegrityConfig.INSTANCE.collapseDelayTicks.get();
            Set<BlockPos> cascadeUnsupported = new HashSet<>();

            for (BlockPos collapsed : collapsedPositions) {
                Set<BlockPos> newUnsupported = findUnsupportedBlocks(level, collapsed);
                cascadeUnsupported.addAll(newUnsupported);
            }

            cascadeUnsupported.removeAll(collapsedPositions);

            if (!cascadeUnsupported.isEmpty()) {
                scheduleCollapse(level, cascadeUnsupported, cascadeDelay);
            }
        }
    }

    private static void collapseBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof FallingBlock) {
            level.removeBlock(pos, false);
            net.minecraft.world.entity.item.FallingBlockEntity.fall(level, pos, state);
        } else {
            level.destroyBlock(pos, true);
        }

        net.minecraft.sounds.SoundEvent collapseSound = getCollapseSoundForMaterial(state);
        level.playSound(null, pos,
                collapseSound,
                net.minecraft.sounds.SoundSource.BLOCKS,
                1.0f, 0.8f);

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.CLOUD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                10, 0.5, 0.5, 0.5, 0.05
        );
    }

    private static net.minecraft.sounds.SoundEvent getCollapseSoundForMaterial(BlockState state) {
        StructuralIntegrityRegistry.SIMaterial material = StructuralIntegrityRegistry.getMaterial(state);
        if (material == null) return net.minecraft.sounds.SoundEvents.WOOD_BREAK;

        return switch (material) {
            case WOOD_FRAME -> net.minecraft.sounds.SoundEvents.WOOD_BREAK;
            case COBBLESTONE -> net.minecraft.sounds.SoundEvents.STONE_BREAK;
            case CONCRETE, REINFORCED_CONCRETE -> net.minecraft.sounds.SoundEvents.DEEPSLATE_BREAK;
            case STEEL -> net.minecraft.sounds.SoundEvents.IRON_GOLEM_HURT;
        };
    }

    private record PendingCollapse(ServerLevel level, Map<BlockPos, BlockState> blocksToCollapse, long executeTick) {}
}
