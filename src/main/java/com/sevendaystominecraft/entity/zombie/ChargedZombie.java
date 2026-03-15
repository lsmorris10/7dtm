package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class ChargedZombie extends BaseSevenDaysZombie {

    public ChargedZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.CHARGED);
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = variant.getBaseHP() > 0 ? variant.getBaseHP() : 20.0;
        hp *= cfg.chargedHPMult.get();
        double damage = variant.getBaseDamage() > 0 ? variant.getBaseDamage() : 1.6;
        damage *= cfg.chargedDamageMult.get();
        double speed = convertSpeedToAttribute(
                (variant.getBaseSpeed() > 0 ? variant.getBaseSpeed() : 1.0) * cfg.chargedSpeedMult.get()
        );

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        boolean result = super.doHurtTarget(serverLevel, target);
        if (result && target instanceof LivingEntity livingTarget) {
            triggerChainLightning(serverLevel, livingTarget);
        }
        return result;
    }

    private void triggerChainLightning(ServerLevel serverLevel, LivingEntity initialTarget) {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        int maxChains = cfg.chargedChainTargets.get();
        float chainDamage = cfg.chargedChainDamage.get().floatValue();

        AABB searchArea = initialTarget.getBoundingBox().inflate(3.0);
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != this && e != initialTarget && e.isAlive() && !(e instanceof BaseSevenDaysZombie));

        nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr(initialTarget)));

        int chained = 0;
        LivingEntity lastTarget = initialTarget;

        for (LivingEntity chainTarget : nearby) {
            if (chained >= maxChains) break;

            Vec3 start = lastTarget.getEyePosition();
            Vec3 end = chainTarget.getEyePosition();
            HitResult clip = level().clip(new ClipContext(start, end,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (clip.getType() == HitResult.Type.BLOCK) continue;

            DamageSource lightningDmg = level().damageSources().lightningBolt();
            chainTarget.hurt(lightningDmg, chainDamage);

            chainTarget.setTicksFrozen(30);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    chainTarget.getX(), chainTarget.getEyeY(), chainTarget.getZ(),
                    15, 0.3, 0.3, 0.3, 0.1);

            lastTarget = chainTarget;
            chained++;
        }

        if (chained > 0) {
            playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && tickCount % 5 == 0) {
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getRandomX(0.5), getRandomY(), getRandomZ(0.5),
                    0, 0.05, 0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.ATTACK_DAMAGE, 2.08)
                .add(Attributes.MOVEMENT_SPEED, 0.12);
    }
}
