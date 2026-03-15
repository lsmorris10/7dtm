package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DemolisherZombie extends BaseSevenDaysZombie {

    private boolean hasExploded = false;

    public DemolisherZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.DEMOLISHER);
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.demolisherHP.get();
        double damage = cfg.demolisherDamage.get();
        double speed = convertSpeedToAttribute(cfg.demolisherSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float amount) {
        if (!hasExploded && source.getEntity() != null) {
            Vec3 hitVec = source.getSourcePosition();
            if (hitVec != null) {
                double hitY = hitVec.y;
                double chestY = getY() + getBbHeight() * 0.5;
                double headY = getY() + getBbHeight() * 0.85;

                if (hitY < headY && hitY >= chestY - 0.3) {
                    triggerExplosion();
                    return;
                }
            }
        }
        super.actuallyHurt(level, source, amount);
    }

    private void triggerExplosion() {
        if (hasExploded) return;
        hasExploded = true;
        int radius = ZombieConfig.INSTANCE.demolisherExplosionRadius.get();
        level().explode(this, getX(), getY(), getZ(), radius, Level.ExplosionInteraction.MOB);
        discard();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 160.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }
}
