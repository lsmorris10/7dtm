package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.DetectionState;
import com.sevendaystominecraft.entity.zombie.FeralWightZombie;
import com.sevendaystominecraft.stealth.NoiseManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class ZombieDetectionGoal extends Goal {

    private static final double BASE_DETECTION_RANGE = 15.0;
    private static final double FERAL_WIGHT_DETECTION_RANGE = 25.0;
    private static final double CROUCH_DETECTION_MULTIPLIER = 0.5;
    private static final double FERAL_WIGHT_DETECTION_MULTIPLIER = 1.6;
    private static final int SUSPICIOUS_DURATION_TICKS = 60;
    private static final int SCAN_INTERVAL_TICKS = 20;

    private final BaseSevenDaysZombie zombie;
    private int suspiciousTicksRemaining;
    private Vec3 suspiciousLookTarget;
    private int scanCooldown;

    public ZombieDetectionGoal(BaseSevenDaysZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (zombie.level().isClientSide()) return false;
        if (!(zombie.level() instanceof ServerLevel)) return false;

        DetectionState state = zombie.getDetectionState();

        if (state == DetectionState.ALERT && zombie.getTarget() != null) {
            return false;
        }

        if (state == DetectionState.ALERT && zombie.getTarget() == null) {
            zombie.setDetectionState(DetectionState.UNAWARE);
            return false;
        }

        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        if (!(zombie.level() instanceof ServerLevel serverLevel)) return;

        scanCooldown = SCAN_INTERVAL_TICKS;

        DetectionState currentState = zombie.getDetectionState();

        if (currentState == DetectionState.SUSPICIOUS) {
            return;
        }

        double maxRange = getDetectionRange();

        List<ServerPlayer> nearbyPlayers = serverLevel.getPlayers(
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative()
                        && zombie.distanceToSqr(p) <= maxRange * maxRange
        );

        ServerPlayer bestTarget = null;
        double bestChance = 0;
        Vec3 bestNoisePos = null;

        for (ServerPlayer player : nearbyPlayers) {
            double detectionChance = calculateDetectionChance(serverLevel, player);

            if (detectionChance > bestChance) {
                bestChance = detectionChance;
                bestTarget = player;
                bestNoisePos = NoiseManager.getLoudestNoisePosition(player.getUUID());
                if (bestNoisePos == null) {
                    bestNoisePos = player.position();
                }
            }
        }

        if (bestChance >= 0.1) {
            zombie.setDetectionState(DetectionState.SUSPICIOUS);
            suspiciousTicksRemaining = SUSPICIOUS_DURATION_TICKS;
            suspiciousLookTarget = bestNoisePos;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (zombie.level().isClientSide()) return false;
        return zombie.getDetectionState() == DetectionState.SUSPICIOUS && suspiciousTicksRemaining > 0;
    }

    @Override
    public void tick() {
        if (!(zombie.level() instanceof ServerLevel serverLevel)) return;

        suspiciousTicksRemaining--;

        if (suspiciousLookTarget != null) {
            zombie.getLookControl().setLookAt(
                    suspiciousLookTarget.x, suspiciousLookTarget.y, suspiciousLookTarget.z);
        }

        if (suspiciousTicksRemaining <= 0) {
            double maxRange = getDetectionRange();
            List<ServerPlayer> nearbyPlayers = serverLevel.getPlayers(
                    p -> p.isAlive() && !p.isSpectator() && !p.isCreative()
                            && zombie.distanceToSqr(p) <= maxRange * maxRange
            );

            ServerPlayer bestTarget = null;
            double bestChance = 0;

            for (ServerPlayer player : nearbyPlayers) {
                double detectionChance = calculateDetectionChance(serverLevel, player);
                if (detectionChance > bestChance) {
                    bestChance = detectionChance;
                    bestTarget = player;
                }
            }

            if (bestChance >= 1.0) {
                zombie.forceAlertTarget(bestTarget);
            } else {
                zombie.setDetectionState(DetectionState.UNAWARE);
            }
            suspiciousLookTarget = null;
        }
    }

    @Override
    public void stop() {
        suspiciousLookTarget = null;
        suspiciousTicksRemaining = 0;
    }

    private double calculateDetectionChance(ServerLevel serverLevel, ServerPlayer player) {
        double distance = zombie.distanceTo(player);
        if (distance < 0.5) distance = 0.5;

        double effectiveRange = getDetectionRange();
        if (player.isCrouching()) {
            effectiveRange *= CROUCH_DETECTION_MULTIPLIER;
        }

        if (distance > effectiveRange) return 0.0;

        float noise = NoiseManager.getCurrentNoise(player.getUUID());

        BlockPos playerBlockPos = player.blockPosition();
        int blockLight = serverLevel.getBrightness(LightLayer.BLOCK, playerBlockPos);
        int skyLight = serverLevel.getBrightness(LightLayer.SKY, playerBlockPos);
        int maxLight = Math.max(blockLight, skyLight);

        double lightFactor;
        if (maxLight > 10) {
            lightFactor = 1.5;
        } else if (maxLight < 5) {
            lightFactor = 0.5;
        } else {
            lightFactor = 1.0;
        }

        double detectionChance = (noise * lightFactor) / (distance * distance);

        if (zombie instanceof FeralWightZombie) {
            detectionChance *= FERAL_WIGHT_DETECTION_MULTIPLIER;
        }

        return detectionChance;
    }

    private double getDetectionRange() {
        if (zombie instanceof FeralWightZombie) {
            return FERAL_WIGHT_DETECTION_RANGE;
        }
        return BASE_DETECTION_RANGE;
    }
}
