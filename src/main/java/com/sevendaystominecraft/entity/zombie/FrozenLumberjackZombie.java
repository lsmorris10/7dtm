package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class FrozenLumberjackZombie extends BaseSevenDaysZombie {

    public FrozenLumberjackZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.FROZEN_LUMBERJACK);
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.frozenLumberjackHP.get();
        double damage = cfg.frozenLumberjackDamage.get();
        double speed = convertSpeedToAttribute(cfg.frozenLumberjackSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 2.4)
                .add(Attributes.MOVEMENT_SPEED, 0.09);
    }
}
