package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.projectile.AcidBallEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CopZombie extends BaseSevenDaysZombie {

    private int bileCooldown = 0;
    private boolean hasExploded = false;

    public CopZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.COP);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new BileSpitGoal(this));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.copHP.get();
        double damage = cfg.copDamage.get();
        double speed = convertSpeedToAttribute(cfg.copSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (bileCooldown > 0) bileCooldown--;

        if (level().isClientSide() && tickCount % 4 == 0) {
            level().addParticle(ParticleTypes.FALLING_WATER,
                    getX() + (random.nextFloat() - 0.5) * 0.2,
                    getEyeY() - 0.1,
                    getZ() + (random.nextFloat() - 0.5) * 0.2,
                    0, -0.05, 0);
            level().addParticle(ParticleTypes.ITEM_SLIME,
                    getX() + (random.nextFloat() - 0.5) * 0.3,
                    getEyeY() - 0.2,
                    getZ() + (random.nextFloat() - 0.5) * 0.3,
                    0, -0.03, 0);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide() && !hasExploded) {
            hasExploded = true;
            Level.ExplosionInteraction interaction = Level.ExplosionInteraction.NONE;
            if (level() instanceof ServerLevel sl) {
                interaction = sl.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                        ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;
            }
            level().explode(this, getX(), getY(), getZ(), 3, interaction);
        }
        super.die(source);
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float amount) {
        super.actuallyHurt(level, source, amount);
        if (!hasExploded && isAlive()) {
            float hpPercent = getHealth() / getMaxHealth();
            if (hpPercent <= 0.2f) {
                hasExploded = true;
                Level.ExplosionInteraction interaction = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                        ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;
                level().explode(this, getX(), getY(), getZ(), 3, interaction);
                discard();
            }
        }
    }

    private void shootBile(LivingEntity target) {
        AcidBallEntity acidBall = new AcidBallEntity(level(), this, new ItemStack(Items.SLIME_BALL));
        Vec3 direction = target.getEyePosition().subtract(getEyePosition()).normalize();

        acidBall.setPos(getX(), getEyeY() - 0.1, getZ());
        acidBall.shoot(direction.x, direction.y, direction.z, 1.2f, 2.0f);
        level().addFreshEntity(acidBall);

        playSound(SoundEvents.LLAMA_SPIT, 1.5f, 0.5f);
        bileCooldown = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.ATTACK_DAMAGE, 5.6)
                .add(Attributes.MOVEMENT_SPEED, 0.12);
    }

    private static class BileSpitGoal extends Goal {
        private final CopZombie cop;

        BileSpitGoal(CopZombie cop) {
            this.cop = cop;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = cop.getTarget();
            if (target == null || !target.isAlive()) return false;
            double dist = cop.distanceTo(target);
            int range = ZombieConfig.INSTANCE.copBileRange.get();
            return dist > 4.0 && dist <= range && cop.bileCooldown <= 0;
        }

        @Override
        public void start() {
            LivingEntity target = cop.getTarget();
            if (target != null) {
                cop.shootBile(target);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}
