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

public class ZombieParrotEntity extends BaseSevenDaysZombie {

    private boolean isDiving = false;
    private int diveCooldown = 0;
    private int shriekCooldown = 0;

    public ZombieParrotEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.ZOMBIE_PARROT);
        setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        removeBaseDetectionGoal();
        removeGroundOnlyGoals();
        goalSelector.addGoal(1, new SwoopAttackGoal(this));
        goalSelector.addGoal(2, new BirdPursueTargetGoal(this));
        goalSelector.addGoal(3, new HoverGoal(this));
        goalSelector.addGoal(4, new BirdScanForPlayerGoal(this));
        goalSelector.addGoal(5, new FlyWanderGoal(this));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.zombieParrotHP.get();
        double damage = cfg.zombieParrotDamage.get();
        double speed = convertSpeedToAttribute(cfg.zombieParrotSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (diveCooldown > 0) diveCooldown--;
        if (shriekCooldown > 0) shriekCooldown--;

        if (!level().isClientSide() && !isDiving && getTarget() == null) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, Math.max(motion.y, -0.01), motion.z);
        }

        if (!level().isClientSide() && shriekCooldown <= 0 && getTarget() != null) {
            shriekCooldown = 200 + getRandom().nextInt(200);
            com.sevendaystominecraft.heatmap.HeatmapData data =
                    com.sevendaystominecraft.heatmap.HeatmapData.getOrCreate((ServerLevel) level());
            data.addHeatSource(new net.minecraft.world.level.ChunkPos(blockPosition()), 2.0f, 0.5f, 0);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier,
                                    net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 3.5)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FLYING_SPEED, 0.5);
    }

    private static class SwoopAttackGoal extends Goal {
        private final ZombieParrotEntity parrot;
        private int swoopTicks = 0;

        SwoopAttackGoal(ZombieParrotEntity parrot) {
            this.parrot = parrot;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = parrot.getTarget();
            if (target == null || !target.isAlive()) return false;
            return parrot.diveCooldown <= 0 && parrot.distanceTo(target) <= 10.0
                    && parrot.getY() > target.getY() + 1.5;
        }

        @Override
        public void start() {
            parrot.isDiving = true;
            swoopTicks = 0;
        }

        @Override
        public void tick() {
            swoopTicks++;
            LivingEntity target = parrot.getTarget();
            if (target == null) return;

            Vec3 dir = target.position().subtract(parrot.position()).normalize().scale(0.65);
            parrot.setDeltaMovement(dir);

            if (parrot.distanceTo(target) < 1.5 && target.level() instanceof ServerLevel sl) {
                parrot.doHurtTarget(sl, target);
                stop();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return parrot.isDiving && swoopTicks < 50 && parrot.getTarget() != null;
        }

        @Override
        public void stop() {
            parrot.isDiving = false;
            parrot.diveCooldown = 80;
            parrot.setDeltaMovement(
                    (parrot.getRandom().nextDouble() - 0.5) * 0.3,
                    0.35,
                    (parrot.getRandom().nextDouble() - 0.5) * 0.3);
        }
    }

    private static class HoverGoal extends Goal {
        private final ZombieParrotEntity parrot;
        private double offsetX, offsetZ;

        HoverGoal(ZombieParrotEntity parrot) {
            this.parrot = parrot;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return parrot.getTarget() != null && !parrot.isDiving;
        }

        @Override
        public void start() {
            offsetX = (parrot.getRandom().nextDouble() - 0.5) * 6;
            offsetZ = (parrot.getRandom().nextDouble() - 0.5) * 6;
        }

        @Override
        public void tick() {
            LivingEntity target = parrot.getTarget();
            if (target == null) return;

            if (parrot.getRandom().nextInt(60) == 0) {
                offsetX = (parrot.getRandom().nextDouble() - 0.5) * 6;
                offsetZ = (parrot.getRandom().nextDouble() - 0.5) * 6;
            }

            double targetY = target.getY() + 4 + Math.sin(parrot.tickCount * 0.1) * 1.5;
            double targetX = target.getX() + offsetX;
            double targetZ = target.getZ() + offsetZ;

            Vec3 dir = new Vec3(targetX - parrot.getX(), targetY - parrot.getY(),
                    targetZ - parrot.getZ()).normalize().scale(0.18);
            parrot.setDeltaMovement(dir);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }
    }
}
