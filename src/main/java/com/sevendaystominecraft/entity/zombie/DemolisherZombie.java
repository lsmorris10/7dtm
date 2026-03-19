package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DemolisherZombie extends BaseSevenDaysZombie {

    private boolean hasExploded = false;
    private boolean fuseActive = false;
    private int fuseTicks = 0;
    private static final int FUSE_DURATION = 60;

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
    public void tick() {
        super.tick();

        if (!level().isClientSide() && !hasExploded) {
            float hpPercent = getHealth() / getMaxHealth();
            if (hpPercent <= 0.25f && !fuseActive) {
                fuseActive = true;
                fuseTicks = 0;
            }

            if (fuseActive) {
                fuseTicks++;
                if (fuseTicks % 10 == 0) {
                    playSound(SoundEvents.TNT_PRIMED, 2.0f, 1.0f + (fuseTicks / (float) FUSE_DURATION));
                }
                if (fuseTicks >= FUSE_DURATION) {
                    triggerExplosion();
                }
            }
        }

        if (fuseActive && level().isClientSide() && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.FLAME,
                    getRandomX(0.5), getRandomY(), getRandomZ(0.5),
                    0, 0.05, 0);
        }
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
        Level.ExplosionInteraction interaction = Level.ExplosionInteraction.NONE;
        if (level() instanceof ServerLevel sl) {
            interaction = sl.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                    ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;
        }
        level().explode(this, getX(), getY(), getZ(), radius, interaction);
        discard();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 160.0)
                .add(Attributes.ATTACK_DAMAGE, 11.3)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }
}
