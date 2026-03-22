package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpiderZombie extends BaseSevenDaysZombie {

    private int wallClimbTicks = 0;

    public SpiderZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.SPIDER_ZOMBIE);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.6f));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.spiderZombieHP.get();
        double damage = cfg.spiderZombieDamage.get();
        double speed = convertSpeedToAttribute(cfg.spiderZombieSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && horizontalCollision && getTarget() != null) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, 0.2, motion.z);
            wallClimbTicks++;
        } else {
            wallClimbTicks = 0;
        }

        if (level().isClientSide() && tickCount % 5 == 0) {
            DustParticleOptions redDust = new DustParticleOptions(0xFFFF1744, 0.4f);
            for (int i = 0; i < 2; i++) {
                level().addParticle(redDust,
                        getX() + (random.nextFloat() - 0.5) * 0.3,
                        getEyeY() + (random.nextFloat() - 0.5) * 0.15,
                        getZ() + (random.nextFloat() - 0.5) * 0.3,
                        0, 0, 0);
            }
        }
    }

    @Override
    public boolean onClimbable() {
        return horizontalCollision || super.onClimbable();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 5.3)
                .add(Attributes.MOVEMENT_SPEED, 0.18);
    }
}
