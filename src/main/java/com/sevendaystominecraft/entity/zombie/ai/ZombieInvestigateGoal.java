package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.heatmap.HeatmapData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ChunkPos;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class ZombieInvestigateGoal extends Goal {

    private final BaseSevenDaysZombie zombie;
    private BlockPos investigateTarget;
    private int wanderTicksRemaining;
    private int recheckCooldown;
    private static final int WANDER_DURATION = 100;
    private static final int RECHECK_COOLDOWN = 60;

    public ZombieInvestigateGoal(BaseSevenDaysZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (zombie.getTarget() != null) return false;
        if (recheckCooldown > 0) {
            recheckCooldown--;
            return false;
        }
        if (!(zombie.level() instanceof ServerLevel serverLevel)) return false;

        investigateTarget = findHighHeatTarget(serverLevel);
        return investigateTarget != null;
    }

    @Override
    public void start() {
        wanderTicksRemaining = 0;
        if (investigateTarget != null) {
            zombie.getNavigation().moveTo(
                    investigateTarget.getX() + 0.5,
                    investigateTarget.getY(),
                    investigateTarget.getZ() + 0.5,
                    1.0);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (zombie.getTarget() != null) return false;
        if (investigateTarget == null) return false;
        return wanderTicksRemaining > 0 || !zombie.getNavigation().isDone();
    }

    @Override
    public void tick() {
        if (investigateTarget == null) return;

        if (!zombie.getNavigation().isDone() && wanderTicksRemaining <= 0) {
            double distSq = zombie.distanceToSqr(
                    investigateTarget.getX() + 0.5,
                    investigateTarget.getY(),
                    investigateTarget.getZ() + 0.5);
            if (distSq < 9.0) {
                zombie.getNavigation().stop();
                wanderTicksRemaining = WANDER_DURATION;
            }
            return;
        }

        if (wanderTicksRemaining > 0) {
            wanderTicksRemaining--;

            if (zombie.getNavigation().isDone() && wanderTicksRemaining % 40 == 0) {
                double offsetX = (zombie.getRandom().nextDouble() - 0.5) * 10.0;
                double offsetZ = (zombie.getRandom().nextDouble() - 0.5) * 10.0;
                double wanderX = investigateTarget.getX() + offsetX;
                double wanderZ = investigateTarget.getZ() + offsetZ;
                int wanderY = zombie.level().getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (int) wanderX, (int) wanderZ);
                zombie.getNavigation().moveTo(wanderX, wanderY, wanderZ, 1.0);
            }

            if (wanderTicksRemaining <= 0) {
                recheckCooldown = RECHECK_COOLDOWN;
                investigateTarget = null;
            }
        }
    }

    @Override
    public void stop() {
        investigateTarget = null;
        wanderTicksRemaining = 0;
    }

    private BlockPos findHighHeatTarget(ServerLevel serverLevel) {
        HeatmapData data = HeatmapData.getOrCreate(serverLevel);
        Map<Long, List<com.sevendaystominecraft.heatmap.HeatSource>> allSources = data.getAllChunkSources();
        if (allSources.isEmpty()) return null;

        int range = ZombieConfig.INSTANCE.investigateRange.get();
        int chunkRange = range / 16;
        ChunkPos zombieChunk = new ChunkPos(zombie.blockPosition());

        long bestKey = -1;
        float bestHeat = 5.0f;

        for (Map.Entry<Long, List<com.sevendaystominecraft.heatmap.HeatSource>> entry : allSources.entrySet()) {
            ChunkPos chunkPos = new ChunkPos(entry.getKey());
            int dx = Math.abs(chunkPos.x - zombieChunk.x);
            int dz = Math.abs(chunkPos.z - zombieChunk.z);
            if (dx > chunkRange || dz > chunkRange) continue;

            float heat = 0;
            for (com.sevendaystominecraft.heatmap.HeatSource source : entry.getValue()) {
                heat += source.getAmount();
            }
            if (heat > bestHeat) {
                bestHeat = heat;
                bestKey = entry.getKey();
            }
        }

        if (bestKey == -1) return null;

        ChunkPos bestChunk = new ChunkPos(bestKey);
        int centerX = (bestChunk.x << 4) + 8;
        int centerZ = (bestChunk.z << 4) + 8;
        int y = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ);
        return new BlockPos(centerX, y, centerZ);
    }
}
