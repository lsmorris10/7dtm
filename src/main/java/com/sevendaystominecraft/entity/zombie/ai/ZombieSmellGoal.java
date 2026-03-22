package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.DetectionState;
import com.sevendaystominecraft.smell.SmellTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class ZombieSmellGoal extends Goal {

    private static final int RECHECK_COOLDOWN = 100;
    private static final int FAILED_SCAN_COOLDOWN = 60;

    private final BaseSevenDaysZombie zombie;
    private ServerPlayer targetPlayer;
    private int recheckCooldown;

    public ZombieSmellGoal(BaseSevenDaysZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (zombie.getTarget() != null) return false;
        if (zombie.getDetectionState() == DetectionState.ALERT) return false;
        if (recheckCooldown > 0) {
            recheckCooldown--;
            return false;
        }
        if (!(zombie.level() instanceof ServerLevel serverLevel)) return false;

        int baseRange = ZombieConfig.INSTANCE.smellRange.get();

        List<ServerPlayer> players = serverLevel.getPlayers(
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative()
        );

        ServerPlayer bestTarget = null;
        double bestScore = 0;

        for (ServerPlayer player : players) {
            int smellValue = SmellTracker.getSmellValue(player.getUUID());
            if (smellValue <= 0) continue;

            double effectiveRange = Math.min(baseRange * (smellValue / 10.0), baseRange * 4.0);
            double distance = zombie.distanceTo(player);
            if (distance > effectiveRange) continue;

            double score = smellValue / (distance + 1.0);
            if (score > bestScore) {
                bestScore = score;
                bestTarget = player;
            }
        }

        targetPlayer = bestTarget;
        if (targetPlayer == null) {
            recheckCooldown = FAILED_SCAN_COOLDOWN;
        }
        return targetPlayer != null;
    }

    @Override
    public void start() {
        if (targetPlayer != null) {
            zombie.setDetectionState(DetectionState.SUSPICIOUS);
            zombie.getNavigation().moveTo(
                    targetPlayer.getX(),
                    targetPlayer.getY(),
                    targetPlayer.getZ(),
                    1.0);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (zombie.getTarget() != null) return false;
        if (zombie.getDetectionState() == DetectionState.ALERT) return false;
        if (targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isSpectator()) return false;

        int smellValue = SmellTracker.getSmellValue(targetPlayer.getUUID());
        if (smellValue <= 0) return false;

        int baseRange = ZombieConfig.INSTANCE.smellRange.get();
        double effectiveRange = Math.min(baseRange * (smellValue / 10.0), baseRange * 4.0);
        return zombie.distanceTo(targetPlayer) <= effectiveRange;
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;

        if (zombie.getNavigation().isDone()) {
            zombie.getNavigation().moveTo(
                    targetPlayer.getX(),
                    targetPlayer.getY(),
                    targetPlayer.getZ(),
                    1.0);
        }
    }

    @Override
    public void stop() {
        targetPlayer = null;
        recheckCooldown = RECHECK_COOLDOWN;
        if (zombie.getDetectionState() == DetectionState.SUSPICIOUS && zombie.getTarget() == null) {
            zombie.setDetectionState(DetectionState.UNAWARE);
        }
    }
}
