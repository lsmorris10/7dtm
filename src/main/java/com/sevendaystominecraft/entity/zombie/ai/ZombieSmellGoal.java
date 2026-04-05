package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.DetectionState;
import com.sevendaystominecraft.smell.ScentTrail;
import com.sevendaystominecraft.smell.SmellTracker;
import com.sevendaystominecraft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class ZombieSmellGoal extends Goal {

    private static final int RECHECK_COOLDOWN = 100;
    private static final int FAILED_SCAN_COOLDOWN = 60;
    private static final double TRAIL_POINT_REACH_DIST_SQR = 4.0;
    private static final int SEARCHING_DURATION_MIN = 100;
    private static final int SEARCHING_DURATION_MAX = 200;
    private static final double SEARCH_WANDER_RADIUS = 6.0;
    private static final int SEARCH_WANDER_INTERVAL = 40;
    private static final double SNIFF_SOUND_RANGE = 16.0;

    private final BaseSevenDaysZombie zombie;
    private ServerPlayer targetPlayer;
    private int recheckCooldown;

    private int currentTrailIndex;
    private boolean isSearching;
    private int searchTicksRemaining;
    private int searchWanderCooldown;
    private Vec3 lastTrailPoint;

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
        if (targetPlayer == null) return;

        zombie.setDetectionState(DetectionState.SUSPICIOUS);
        isSearching = false;
        searchTicksRemaining = 0;
        searchWanderCooldown = 0;
        lastTrailPoint = null;

        ScentTrail trail = SmellTracker.getTrail(targetPlayer.getUUID());
        if (trail != null && trail.size() > 0) {
            int nearestIdx = trail.getNearestIndex(zombie.position());
            currentTrailIndex = (nearestIdx >= 0) ? nearestIdx : trail.size() - 1;
            navigateToTrailPoint(trail);
        } else {
            currentTrailIndex = 0;
            zombie.getNavigation().moveTo(
                    targetPlayer.getX(),
                    targetPlayer.getY(),
                    targetPlayer.getZ(),
                    1.0);
        }

        playSniffSound();
    }

    @Override
    public boolean canContinueToUse() {
        if (zombie.getTarget() != null) return false;
        if (zombie.getDetectionState() == DetectionState.ALERT) return false;

        if (isSearching) {
            if (targetPlayer != null && (!targetPlayer.isAlive() || targetPlayer.isSpectator())) {
                return false;
            }
            return searchTicksRemaining > 0;
        }

        if (targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isSpectator()) return false;

        int smellValue = SmellTracker.getSmellValue(targetPlayer.getUUID());
        if (smellValue <= 0) return false;

        int baseRange = ZombieConfig.INSTANCE.smellRange.get();
        double effectiveRange = Math.min(baseRange * (smellValue / 10.0), baseRange * 4.0);
        return zombie.distanceTo(targetPlayer) <= effectiveRange;
    }

    @Override
    public void tick() {
        if (isSearching) {
            tickSearching();
            spawnSniffParticles();
            return;
        }

        if (targetPlayer == null) return;

        ScentTrail trail = SmellTracker.getTrail(targetPlayer.getUUID());

        if (trail != null && trail.size() > 0) {
            Vec3 currentPoint = trail.get(currentTrailIndex);
            if (currentPoint != null) {
                double distSqr = zombie.position().distanceToSqr(currentPoint);
                if (distSqr < TRAIL_POINT_REACH_DIST_SQR) {
                    currentTrailIndex++;
                    if (currentTrailIndex >= trail.size()) {
                        if (!hasLineOfSight()) {
                            enterSearchingState(currentPoint);
                            return;
                        }
                        currentTrailIndex = trail.size() - 1;
                    }
                }
            }

            if (zombie.getNavigation().isDone()) {
                navigateToTrailPoint(trail);
            }
        } else {
            if (zombie.getNavigation().isDone()) {
                zombie.getNavigation().moveTo(
                        targetPlayer.getX(),
                        targetPlayer.getY(),
                        targetPlayer.getZ(),
                        1.0);
            }
        }

        spawnSniffParticles();
    }

    private void navigateToTrailPoint(ScentTrail trail) {
        Vec3 point = trail.get(Math.min(currentTrailIndex, trail.size() - 1));
        if (point != null) {
            zombie.getNavigation().moveTo(point.x, point.y, point.z, 1.0);
        }
    }

    private boolean hasLineOfSight() {
        if (targetPlayer == null) return false;
        return zombie.getSensing().hasLineOfSight(targetPlayer);
    }

    private void enterSearchingState(Vec3 arrivedAt) {
        isSearching = true;
        lastTrailPoint = arrivedAt;
        searchTicksRemaining = SEARCHING_DURATION_MIN
                + zombie.getRandom().nextInt(SEARCHING_DURATION_MAX - SEARCHING_DURATION_MIN);
        searchWanderCooldown = 0;
    }

    private void tickSearching() {
        searchTicksRemaining--;

        if (targetPlayer != null && hasLineOfSight()) {
            isSearching = false;
            currentTrailIndex = 0;
            ScentTrail trail = SmellTracker.getTrail(targetPlayer.getUUID());
            if (trail != null && trail.size() > 0) {
                currentTrailIndex = trail.size() - 1;
            }
            zombie.getNavigation().moveTo(
                    targetPlayer.getX(),
                    targetPlayer.getY(),
                    targetPlayer.getZ(),
                    1.0);
            return;
        }

        searchWanderCooldown--;
        if (searchWanderCooldown <= 0 && lastTrailPoint != null) {
            double offsetX = (zombie.getRandom().nextDouble() - 0.5) * 2.0 * SEARCH_WANDER_RADIUS;
            double offsetZ = (zombie.getRandom().nextDouble() - 0.5) * 2.0 * SEARCH_WANDER_RADIUS;
            zombie.getNavigation().moveTo(
                    lastTrailPoint.x + offsetX,
                    lastTrailPoint.y,
                    lastTrailPoint.z + offsetZ,
                    0.8);
            searchWanderCooldown = SEARCH_WANDER_INTERVAL;
        }
    }

    private void spawnSniffParticles() {
        if (!(zombie.level() instanceof ServerLevel serverLevel)) return;
        if (zombie.tickCount % 10 != 0) return;

        serverLevel.sendParticles(ParticleTypes.WITCH,
                zombie.getX(), zombie.getY() + zombie.getBbHeight() + 0.3, zombie.getZ(),
                2, 0.15, 0.1, 0.15, 0.01);
    }

    private void playSniffSound() {
        if (!(zombie.level() instanceof ServerLevel serverLevel)) return;
        if (targetPlayer == null) return;

        double dist = zombie.distanceTo(targetPlayer);
        if (dist <= SNIFF_SOUND_RANGE) {
            ModSounds.playAtEntity(ModSounds.ZOMBIE_SNIFF, zombie,
                    SoundSource.HOSTILE, 1.0f, 0.9f + zombie.getRandom().nextFloat() * 0.2f);
        }
    }

    @Override
    public void stop() {
        targetPlayer = null;
        isSearching = false;
        searchTicksRemaining = 0;
        lastTrailPoint = null;
        currentTrailIndex = 0;
        recheckCooldown = RECHECK_COOLDOWN;
        if (zombie.getDetectionState() == DetectionState.SUSPICIOUS && zombie.getTarget() == null) {
            zombie.setDetectionState(DetectionState.UNAWARE);
        }
    }
}
