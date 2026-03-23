package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.zombie.ai.BirdPursueTargetGoal;
import com.sevendaystominecraft.entity.zombie.ai.BirdScanForPlayerGoal;
import com.sevendaystominecraft.entity.zombie.ai.FlyWanderGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class VultureEntity extends BaseSevenDaysZombie {

    private boolean isDiving = false;
    private int diveCooldown = 0;

    public VultureEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.VULTURE);
        setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        removeBaseDetectionGoal();
        removeGroundOnlyGoals();
        goalSelector.addGoal(1, new DiveAttackGoal(this));
        goalSelector.addGoal(2, new BirdPursueTargetGoal(this));
        goalSelector.addGoal(3, new CircleGoal(this));
        goalSelector.addGoal(4, new BirdScanForPlayerGoal(this));
        goalSelector.addGoal(5, new FlyWanderGoal(this));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.vultureHP.get();
        double damage = cfg.vultureDamage.get();
        double speed = convertSpeedToAttribute(cfg.vultureSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (diveCooldown > 0) diveCooldown--;

        if (!level().isClientSide() && !isDiving && getTarget() == null) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, Math.max(motion.y, -0.01), motion.z);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier,
                                    net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 4.5)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FLYING_SPEED, 0.4);
    }

    private static class DiveAttackGoal extends Goal {
        private final VultureEntity vulture;
        private int diveTicks = 0;

        DiveAttackGoal(VultureEntity vulture) {
            this.vulture = vulture;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = vulture.getTarget();
            if (target == null || !target.isAlive()) return false;
            return vulture.diveCooldown <= 0 && vulture.distanceTo(target) <= 10.0
                    && vulture.getY() > target.getY() + 3;
        }

        @Override
        public void start() {
            vulture.isDiving = true;
            diveTicks = 0;
        }

        @Override
        public void tick() {
            diveTicks++;
            LivingEntity target = vulture.getTarget();
            if (target == null) return;

            Vec3 dir = target.position().subtract(vulture.position()).normalize().scale(0.6);
            vulture.setDeltaMovement(dir);

            if (vulture.distanceTo(target) < 2.0 && target.level() instanceof ServerLevel sl) {
                vulture.doHurtTarget(sl, target);
                stop();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return vulture.isDiving && diveTicks < 60 && vulture.getTarget() != null;
        }

        @Override
        public void stop() {
            vulture.isDiving = false;
            vulture.diveCooldown = 100;
            vulture.setDeltaMovement(vulture.getDeltaMovement().x, 0.3, vulture.getDeltaMovement().z);
        }
    }

    private static class CircleGoal extends Goal {
        private final VultureEntity vulture;
        private double angle = 0;

        CircleGoal(VultureEntity vulture) {
            this.vulture = vulture;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return vulture.getTarget() != null && !vulture.isDiving;
        }

        @Override
        public void tick() {
            LivingEntity target = vulture.getTarget();
            if (target == null) return;

            angle += 0.05;
            double radius = 8.0;
            double targetY = target.getY() + 8;
            double targetX = target.getX() + Math.cos(angle) * radius;
            double targetZ = target.getZ() + Math.sin(angle) * radius;

            Vec3 dir = new Vec3(targetX - vulture.getX(), targetY - vulture.getY(),
                    targetZ - vulture.getZ()).normalize().scale(0.15);
            vulture.setDeltaMovement(dir);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }
    }
}
