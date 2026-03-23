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

public class ZombieBirdEntity extends BaseSevenDaysZombie {

    private boolean isDiving = false;
    private int diveCooldown = 0;

    public ZombieBirdEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.ZOMBIE_BIRD);
        setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        removeBaseDetectionGoal();
        removeGroundOnlyGoals();
        goalSelector.addGoal(1, new SwarmDiveGoal(this));
        goalSelector.addGoal(2, new BirdPursueTargetGoal(this));
        goalSelector.addGoal(3, new FlockGoal(this));
        goalSelector.addGoal(4, new BirdScanForPlayerGoal(this));
        goalSelector.addGoal(5, new FlyWanderGoal(this));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.zombieBirdHP.get();
        double damage = cfg.zombieBirdDamage.get();
        double speed = convertSpeedToAttribute(cfg.zombieBirdSpeed.get());

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
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.45)
                .add(Attributes.FLYING_SPEED, 0.45);
    }

    private static class SwarmDiveGoal extends Goal {
        private final ZombieBirdEntity bird;
        private int diveTicks = 0;

        SwarmDiveGoal(ZombieBirdEntity bird) {
            this.bird = bird;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = bird.getTarget();
            if (target == null || !target.isAlive()) return false;
            return bird.diveCooldown <= 0 && bird.distanceTo(target) <= 10.0
                    && bird.getY() > target.getY() + 2;
        }

        @Override
        public void start() {
            bird.isDiving = true;
            diveTicks = 0;
        }

        @Override
        public void tick() {
            diveTicks++;
            LivingEntity target = bird.getTarget();
            if (target == null) return;

            Vec3 dir = target.position().subtract(bird.position()).normalize().scale(0.7);
            bird.setDeltaMovement(dir);

            if (bird.distanceTo(target) < 1.5 && target.level() instanceof ServerLevel sl) {
                bird.doHurtTarget(sl, target);
                stop();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return bird.isDiving && diveTicks < 40 && bird.getTarget() != null;
        }

        @Override
        public void stop() {
            bird.isDiving = false;
            bird.diveCooldown = 60;
            bird.setDeltaMovement(bird.getDeltaMovement().x, 0.4, bird.getDeltaMovement().z);
        }
    }

    private static class FlockGoal extends Goal {
        private final ZombieBirdEntity bird;
        private double angle;

        FlockGoal(ZombieBirdEntity bird) {
            this.bird = bird;
            this.angle = bird.getRandom().nextDouble() * Math.PI * 2;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return bird.getTarget() != null && !bird.isDiving;
        }

        @Override
        public void tick() {
            LivingEntity target = bird.getTarget();
            if (target == null) return;

            angle += 0.08;
            double radius = 5.0;
            double targetY = target.getY() + 6;
            double targetX = target.getX() + Math.cos(angle) * radius;
            double targetZ = target.getZ() + Math.sin(angle) * radius;

            Vec3 dir = new Vec3(targetX - bird.getX(), targetY - bird.getY(),
                    targetZ - bird.getZ()).normalize().scale(0.2);
            bird.setDeltaMovement(dir);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }
    }
}
