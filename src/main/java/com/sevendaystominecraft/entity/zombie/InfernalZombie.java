package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class InfernalZombie extends BaseSevenDaysZombie {

    private int fireTrailTicks = 0;

    public InfernalZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.INFERNAL);
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = variant.getBaseHP() > 0 ? variant.getBaseHP() : 20.0;
        hp *= cfg.infernalHPMult.get();
        double damage = variant.getBaseDamage() > 0 ? variant.getBaseDamage() : 1.6;
        damage *= cfg.infernalDamageMult.get();
        double speed = convertSpeedToAttribute(
                (variant.getBaseSpeed() > 0 ? variant.getBaseSpeed() : 1.0) * cfg.infernalSpeedMult.get()
        );

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            fireTrailTicks++;
            int interval = ZombieConfig.INSTANCE.infernalFireTrailInterval.get();
            if (fireTrailTicks >= interval && getDeltaMovement().horizontalDistanceSqr() > 0.001) {
                fireTrailTicks = 0;
                BlockPos pos = blockPosition();
                if (level().getBlockState(pos).isAir()) {
                    level().setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                }
            }
        }

        if (level().isClientSide() && tickCount % 3 == 0) {
            level().addParticle(ParticleTypes.FLAME,
                    getRandomX(0.5), getRandomY(), getRandomZ(0.5),
                    0, 0.02, 0);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        boolean result = super.doHurtTarget(serverLevel, target);
        if (result && target instanceof LivingEntity living) {
            living.setRemainingFireTicks(100);
        }
        return result;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.ATTACK_DAMAGE, 2.24)
                .add(Attributes.MOVEMENT_SPEED, 0.11);
    }
}
