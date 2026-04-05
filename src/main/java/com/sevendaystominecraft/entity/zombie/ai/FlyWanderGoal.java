package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.DetectionState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FlyWanderGoal extends Goal {

    private static final double MIN_ALTITUDE = 5.0;
    private static final double MAX_ALTITUDE = 40.0;
    private static final double WANDER_SPEED = 0.15;
    private static final int DIRECTION_CHANGE_MIN_TICKS = 60;
    private static final int DIRECTION_CHANGE_MAX_TICKS = 200;

    private final BaseSevenDaysZombie bird;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int ticksUntilDirectionChange;

    public FlyWanderGoal(BaseSevenDaysZombie bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return bird.getTarget() == null
                && bird.getDetectionState() == DetectionState.UNAWARE
                && !bird.level().isClientSide();
    }

    @Override
    public void start() {
        pickNewTarget();
    }

    @Override
    public void tick() {
        ticksUntilDirectionChange--;

        if (ticksUntilDirectionChange <= 0 || reachedTarget()) {
            pickNewTarget();
        }

        double groundY = getGroundY();
        if (bird.getY() < groundY + MIN_ALTITUDE) {
            targetY = groundY + MIN_ALTITUDE + bird.getRandom().nextDouble() * 10;
        }

        if (bird.getY() > groundY + MAX_ALTITUDE) {
            targetY = groundY + MAX_ALTITUDE - bird.getRandom().nextDouble() * 5;
        }

        Vec3 toTarget = new Vec3(targetX - bird.getX(), targetY - bird.getY(), targetZ - bird.getZ());
        double dist = toTarget.length();
        if (dist > 0.5) {
            Vec3 dir = toTarget.normalize().scale(WANDER_SPEED);
            bird.setDeltaMovement(dir);
        } else {
            bird.setDeltaMovement(bird.getDeltaMovement().scale(0.8));
        }

        bird.getLookControl().setLookAt(targetX, targetY, targetZ);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void stop() {
        bird.setDeltaMovement(Vec3.ZERO);
    }

    private void pickNewTarget() {
        double angle = bird.getRandom().nextDouble() * Math.PI * 2;
        double radius = 10.0 + bird.getRandom().nextDouble() * 20.0;
        targetX = bird.getX() + Math.cos(angle) * radius;
        targetZ = bird.getZ() + Math.sin(angle) * radius;

        double groundY = getGroundY();
        double altitudeRange = MAX_ALTITUDE - MIN_ALTITUDE;
        targetY = groundY + MIN_ALTITUDE + bird.getRandom().nextDouble() * altitudeRange;

        ticksUntilDirectionChange = DIRECTION_CHANGE_MIN_TICKS
                + bird.getRandom().nextInt(DIRECTION_CHANGE_MAX_TICKS - DIRECTION_CHANGE_MIN_TICKS);
    }

    private boolean reachedTarget() {
        double dx = targetX - bird.getX();
        double dy = targetY - bird.getY();
        double dz = targetZ - bird.getZ();
        return dx * dx + dy * dy + dz * dz < 4.0;
    }

    private double getGroundY() {
        Level level = bird.level();
        BlockPos pos = bird.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), (int) bird.getY(), pos.getZ());
        while (mutable.getY() > level.getMinY() && level.getBlockState(mutable).isAir()) {
            mutable.move(0, -1, 0);
        }
        return mutable.getY() + 1;
    }
}
