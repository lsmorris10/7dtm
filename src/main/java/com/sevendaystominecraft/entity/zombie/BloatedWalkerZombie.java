package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class BloatedWalkerZombie extends BaseSevenDaysZombie {

    public BloatedWalkerZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.BLOATED_WALKER);
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.bloatedWalkerHP.get();
        double damage = cfg.bloatedWalkerDamage.get();
        double speed = convertSpeedToAttribute(cfg.bloatedWalkerSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide()) {
            int radius = ZombieConfig.INSTANCE.bloatedExplosionRadius.get();
            level().explode(this, getX(), getY(), getZ(), radius, Level.ExplosionInteraction.MOB);
        }
        super.die(source);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.07);
    }
}
