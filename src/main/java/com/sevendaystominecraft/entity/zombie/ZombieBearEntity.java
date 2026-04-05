package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class ZombieBearEntity extends BaseSevenDaysZombie {

    private int chargeCooldown = 0;
    private boolean isCharging = false;

    public ZombieBearEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.ZOMBIE_BEAR);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new ChargeGoal(this));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.zombieBearHP.get();
        double damage = cfg.zombieBearDamage.get();
        double speed = convertSpeedToAttribute(cfg.zombieBearSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (chargeCooldown > 0) chargeCooldown--;
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        boolean result = super.doHurtTarget(serverLevel, target);
        if (result) {
            AABB swipeArea = getBoundingBox().inflate(2.0);
            List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, swipeArea,
                    e -> e != this && e.isAlive() && !(e instanceof BaseSevenDaysZombie));
            for (LivingEntity entity : nearby) {
                if (entity == target) continue;
                entity.hurtServer(serverLevel, damageSources().mobAttack(this),
                        (float) getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 0.5f);
            }
        }
        return result;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 120.0)
                .add(Attributes.ATTACK_DAMAGE, 13.1)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    private static class ChargeGoal extends Goal {
        private final ZombieBearEntity bear;
        private int chargeTicks = 0;

        ChargeGoal(ZombieBearEntity bear) {
            this.bear = bear;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = bear.getTarget();
            if (target == null || !target.isAlive()) return false;
            double dist = bear.distanceTo(target);
            return dist > 5.0 && dist < 20.0 && bear.chargeCooldown <= 0;
        }

        @Override
        public void start() {
            bear.isCharging = true;
            chargeTicks = 0;
            bear.playSound(SoundEvents.RAVAGER_ROAR, 2.0f, 0.8f);
        }

        @Override
        public void tick() {
            chargeTicks++;
            LivingEntity target = bear.getTarget();
            if (target == null) return;

            bear.getNavigation().moveTo(target, 2.0);

            if (bear.distanceTo(target) < 2.0 && target.level() instanceof ServerLevel sl) {
                bear.doHurtTarget(sl, target);
                stop();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return bear.isCharging && chargeTicks < 40 && bear.getTarget() != null;
        }

        @Override
        public void stop() {
            bear.isCharging = false;
            bear.chargeCooldown = 200;
        }
    }
}
