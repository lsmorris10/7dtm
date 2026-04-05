package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class BehemothZombie extends BaseSevenDaysZombie {

    private int groundPoundCooldown = 0;

    public BehemothZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.BEHEMOTH);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new GroundPoundGoal(this));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.behemothHP.get();
        double damage = cfg.behemothDamage.get();
        double speed = convertSpeedToAttribute(cfg.behemothSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (groundPoundCooldown > 0) groundPoundCooldown--;
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    private void performGroundPound() {
        if (level().isClientSide()) return;

        ZombieConfig cfg = ZombieConfig.INSTANCE;
        int radius = cfg.behemothGroundPoundRadius.get();
        float damage = (float) getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 0.75f;

        AABB area = getBoundingBox().inflate(radius);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e.isAlive() && !(e instanceof BaseSevenDaysZombie));

        for (LivingEntity target : targets) {
            target.hurtServer((ServerLevel) level(), damageSources().mobAttack(this), damage);
            Vec3 knockDir = target.position().subtract(position()).normalize().scale(1.5);
            target.push(knockDir.x, 0.5, knockDir.z);
        }

        playSound(SoundEvents.GENERIC_EXPLODE.value(), 2.0f, 0.5f);
        groundPoundCooldown = 200;

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    getX(), getY(), getZ(),
                    8, radius * 0.5, 0.5, radius * 0.5, 0.0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ATTACK_DAMAGE, 18.8)
                .add(Attributes.MOVEMENT_SPEED, 0.08)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    private static class GroundPoundGoal extends Goal {
        private final BehemothZombie behemoth;

        GroundPoundGoal(BehemothZombie behemoth) {
            this.behemoth = behemoth;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = behemoth.getTarget();
            if (target == null || !target.isAlive()) return false;
            return behemoth.distanceTo(target) < 4.0 && behemoth.groundPoundCooldown <= 0;
        }

        @Override
        public void start() {
            behemoth.performGroundPound();
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}
