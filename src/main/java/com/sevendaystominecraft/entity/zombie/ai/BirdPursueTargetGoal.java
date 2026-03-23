package com.sevendaystominecraft.entity.zombie.ai;

import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.DetectionState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BirdPursueTargetGoal extends Goal {

    private static final double PURSUIT_SPEED = 0.3;
    private static final double ENGAGE_DISTANCE = 10.0;
    private static final double PURSUIT_ALTITUDE_OFFSET = 4.0;

    private final BaseSevenDaysZombie bird;

    public BirdPursueTargetGoal(BaseSevenDaysZombie bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (bird.level().isClientSide()) return false;
        LivingEntity target = bird.getTarget();
        if (target == null || !target.isAlive()) return false;
        return bird.getDetectionState() == DetectionState.ALERT
                && bird.distanceTo(target) > ENGAGE_DISTANCE;
    }

    @Override
    public void tick() {
        LivingEntity target = bird.getTarget();
        if (target == null) return;

        double desiredY = target.getY() + PURSUIT_ALTITUDE_OFFSET;

        Vec3 toTarget = new Vec3(
                target.getX() - bird.getX(),
                desiredY - bird.getY(),
                target.getZ() - bird.getZ());

        Vec3 dir = toTarget.normalize().scale(PURSUIT_SPEED);
        bird.setDeltaMovement(dir);

        bird.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
    }

    @Override
    public boolean canContinueToUse() {
        if (bird.level().isClientSide()) return false;
        LivingEntity target = bird.getTarget();
        if (target == null || !target.isAlive()) return false;
        return bird.getDetectionState() == DetectionState.ALERT
                && bird.distanceTo(target) > ENGAGE_DISTANCE;
    }
}
