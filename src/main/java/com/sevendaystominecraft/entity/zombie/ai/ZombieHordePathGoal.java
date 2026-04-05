package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.horde.BloodMoonTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.List;

public class ZombieHordePathGoal extends Goal {

    private final BaseSevenDaysZombie zombie;
    private ServerPlayer targetPlayer;
    private int repathCooldown;
    private static final int REPATH_INTERVAL = 40;

    public ZombieHordePathGoal(BaseSevenDaysZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(zombie.level() instanceof ServerLevel serverLevel)) return false;
        if (serverLevel.dimension() != Level.OVERWORLD) return false;

        boolean isBloodMoon = false;
        if (zombie.isHordeMob()) {
            isBloodMoon = true;
        } else {
            BloodMoonTracker tracker = BloodMoonTracker.getOrCreate(serverLevel);
            if (tracker.isBloodMoonActive()) {
                isBloodMoon = true;
            }
        }
        if (!isBloodMoon) return false;

        if (zombie.getTarget() != null) return false;

        targetPlayer = findNearestPlayer(serverLevel);
        return targetPlayer != null;
    }

    @Override
    public void start() {
        repathCooldown = 0;
        navigateToPlayer();
    }

    @Override
    public boolean canContinueToUse() {
        if (!(zombie.level() instanceof ServerLevel serverLevel)) return false;
        if (serverLevel.dimension() != Level.OVERWORLD) return false;

        if (!zombie.isHordeMob()) {
            BloodMoonTracker tracker = BloodMoonTracker.getOrCreate(serverLevel);
            if (!tracker.isBloodMoonActive()) return false;
        }

        if (zombie.getTarget() != null) return false;

        if (targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isSpectator()) {
            targetPlayer = findNearestPlayer(serverLevel);
            return targetPlayer != null;
        }

        double range = getPathRange(serverLevel);
        return zombie.distanceToSqr(targetPlayer) <= range * range;
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;

        repathCooldown--;
        if (repathCooldown <= 0) {
            repathCooldown = REPATH_INTERVAL;
            navigateToPlayer();
        }
    }

    @Override
    public void stop() {
        targetPlayer = null;
    }

    private void navigateToPlayer() {
        if (targetPlayer != null) {
            zombie.getNavigation().moveTo(targetPlayer, 1.0);
        }
    }

    private ServerPlayer findNearestPlayer(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        double range = getPathRange(level);
        double rangeSq = range * range;

        ServerPlayer nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (ServerPlayer player : players) {
            if (player.isSpectator() || player.isCreative()) continue;
            double distSq = zombie.distanceToSqr(player);
            if (distSq <= rangeSq && distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = player;
            }
        }

        return nearest;
    }

    private double getPathRange(ServerLevel level) {
        int currentDay = BloodMoonTracker.calculateGameDay(level);
        if (currentDay >= 21) {
            return ZombieConfig.INSTANCE.hordePathRangeDay21.get();
        }
        return ZombieConfig.INSTANCE.hordePathRange.get();
    }
}
