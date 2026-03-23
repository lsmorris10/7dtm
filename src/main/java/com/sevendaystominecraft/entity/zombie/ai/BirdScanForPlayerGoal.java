package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.DetectionState;
import com.sevendaystominecraft.stealth.NoiseManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class BirdScanForPlayerGoal extends Goal {

    private static final double BASE_SIGHT_RANGE = 30.0;
    private static final double ALTITUDE_BONUS_PER_BLOCK = 0.5;
    private static final double MAX_ALTITUDE_BONUS = 20.0;
    private static final double CROUCH_DETECTION_MULTIPLIER = 0.5;
    private static final int SUSPICIOUS_DURATION_TICKS = 80;
    private static final int SCAN_INTERVAL_TICKS = 15;

    private final BaseSevenDaysZombie bird;
    private int suspiciousTicksRemaining;
    private Vec3 lastSeenPosition;
    private int scanCooldown;

    public BirdScanForPlayerGoal(BaseSevenDaysZombie bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (bird.level().isClientSide()) return false;
        if (!(bird.level() instanceof ServerLevel)) return false;

        DetectionState state = bird.getDetectionState();

        if (state == DetectionState.ALERT && bird.getTarget() != null) {
            return false;
        }

        if (state == DetectionState.ALERT && bird.getTarget() == null) {
            bird.setDetectionState(DetectionState.UNAWARE);
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
        if (!(bird.level() instanceof ServerLevel serverLevel)) return;

        scanCooldown = SCAN_INTERVAL_TICKS;

        DetectionState currentState = bird.getDetectionState();

        if (currentState == DetectionState.SUSPICIOUS) {
            return;
        }

        double maxRange = getSightRange();

        List<ServerPlayer> nearbyPlayers = serverLevel.getPlayers(
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative()
                        && bird.distanceToSqr(p) <= maxRange * maxRange
        );

        ServerPlayer bestTarget = null;
        double bestChance = 0;

        for (ServerPlayer player : nearbyPlayers) {
            if (!hasLineOfSight(player)) continue;
            double chance = calculateDetectionChance(serverLevel, player, maxRange);
            if (chance > bestChance) {
                bestChance = chance;
                bestTarget = player;
            }
        }

        if (bestChance >= 0.1) {
            bird.setDetectionState(DetectionState.SUSPICIOUS);
            suspiciousTicksRemaining = SUSPICIOUS_DURATION_TICKS;
            Vec3 noisePos = NoiseManager.getLoudestNoisePosition(bestTarget.getUUID());
            lastSeenPosition = noisePos != null ? noisePos : bestTarget.position();
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (bird.level().isClientSide()) return false;
        return bird.getDetectionState() == DetectionState.SUSPICIOUS && suspiciousTicksRemaining > 0;
    }

    @Override
    public void tick() {
        if (!(bird.level() instanceof ServerLevel serverLevel)) return;

        suspiciousTicksRemaining--;

        if (lastSeenPosition != null) {
            bird.getLookControl().setLookAt(
                    lastSeenPosition.x, lastSeenPosition.y, lastSeenPosition.z);

            Vec3 toTarget = new Vec3(
                    lastSeenPosition.x - bird.getX(),
                    lastSeenPosition.y + 5 - bird.getY(),
                    lastSeenPosition.z - bird.getZ());
            double dist = toTarget.length();
            if (dist > 3.0) {
                Vec3 flyDir = toTarget.normalize().scale(0.12);
                bird.setDeltaMovement(flyDir);
            }
        }

        if (suspiciousTicksRemaining <= 0) {
            double maxRange = getSightRange();
            List<ServerPlayer> nearbyPlayers = serverLevel.getPlayers(
                    p -> p.isAlive() && !p.isSpectator() && !p.isCreative()
                            && bird.distanceToSqr(p) <= maxRange * maxRange
            );

            ServerPlayer bestTarget = null;
            double bestChance = 0;

            for (ServerPlayer player : nearbyPlayers) {
                if (!hasLineOfSight(player)) continue;
                double chance = calculateDetectionChance(serverLevel, player, maxRange);
                if (chance > bestChance) {
                    bestChance = chance;
                    bestTarget = player;
                }
            }

            if (bestChance >= 1.0) {
                bird.forceAlertTarget(bestTarget);
                com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                        com.sevendaystominecraft.sound.ModSounds.ZOMBIE_ALERT, bird,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.2f,
                        0.8f + bird.getRandom().nextFloat() * 0.4f);
            } else {
                bird.setDetectionState(DetectionState.UNAWARE);
            }
            lastSeenPosition = null;
        }
    }

    @Override
    public void stop() {
        lastSeenPosition = null;
        suspiciousTicksRemaining = 0;
    }

    private double calculateDetectionChance(ServerLevel serverLevel, ServerPlayer player, double maxRange) {
        double distance = bird.distanceTo(player);
        if (distance < 0.5) distance = 0.5;

        double effectiveRange = maxRange;
        if (player.isCrouching()) {
            effectiveRange *= CROUCH_DETECTION_MULTIPLIER;
        }

        if (player.hasData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get())) {
            com.sevendaystominecraft.capability.SevenDaysPlayerStats stats =
                    player.getData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get());
            int nightstalkerRank = stats.getPerkRank("nightstalker");
            if (nightstalkerRank > 0) {
                effectiveRange *= Math.max(0.1, 1.0 - 0.20 * nightstalkerRank);
            }
        }

        if (distance > effectiveRange) return 0.0;

        float noise = NoiseManager.getCurrentNoise(player.getUUID());

        double visibilityFactor = 1.0;
        if (bird.getY() > player.getY() + 3) {
            visibilityFactor = 1.3;
        }

        double detectionChance = (noise * visibilityFactor + 5.0) / (distance * 0.8);

        return detectionChance;
    }

    private double getSightRange() {
        double altitudeAboveGround = bird.getY() - getApproxGroundY();
        double altitudeBonus = Math.min(altitudeAboveGround * ALTITUDE_BONUS_PER_BLOCK, MAX_ALTITUDE_BONUS);
        return BASE_SIGHT_RANGE + altitudeBonus;
    }

    private double getApproxGroundY() {
        net.minecraft.core.BlockPos pos = bird.blockPosition();
        net.minecraft.core.BlockPos.MutableBlockPos mutable =
                new net.minecraft.core.BlockPos.MutableBlockPos(pos.getX(), (int) bird.getY(), pos.getZ());
        while (mutable.getY() > bird.level().getMinY() && bird.level().getBlockState(mutable).isAir()) {
            mutable.move(0, -1, 0);
        }
        return mutable.getY() + 1;
    }

    private boolean hasLineOfSight(ServerPlayer player) {
        Vec3 eyePos = new Vec3(bird.getX(), bird.getEyeY(), bird.getZ());
        Vec3 targetPos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        ClipContext context = new ClipContext(
                eyePos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bird);
        HitResult result = bird.level().clip(context);
        return result.getType() == HitResult.Type.MISS
                || result.getLocation().distanceToSqr(targetPos) < 1.0;
    }
}
