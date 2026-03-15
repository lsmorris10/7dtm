package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class NurseZombie extends BaseSevenDaysZombie {

    public NurseZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.NURSE);
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.nurseHP.get();
        double damage = cfg.nurseDamage.get();
        double speed = convertSpeedToAttribute(cfg.nurseSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 20 == 0) {
            healNearbyZombies();
        }
    }

    private void healNearbyZombies() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        int radius = cfg.nurseHealRadius.get();
        float healAmount = cfg.nurseHealRate.get().floatValue();

        AABB healArea = getBoundingBox().inflate(radius);
        List<Zombie> nearbyZombies = level().getEntitiesOfClass(Zombie.class, healArea,
                z -> z != this && z.isAlive() && z.getHealth() < z.getMaxHealth());

        for (Zombie zombie : nearbyZombies) {
            zombie.heal(healAmount);
        }

        if (!nearbyZombies.isEmpty() && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    getX(), getEyeY() + 0.5, getZ(),
                    3, 0.3, 0.3, 0.3, 0.0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1);
    }
}
