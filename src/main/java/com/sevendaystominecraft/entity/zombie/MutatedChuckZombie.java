package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.config.ZombieConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MutatedChuckZombie extends BaseSevenDaysZombie {

    private int vomitCooldown = 0;

    public MutatedChuckZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level, ZombieVariant.MUTATED_CHUCK);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new VomitAttackGoal(this));
    }

    @Override
    protected void applyVariantStats() {
        ZombieConfig cfg = ZombieConfig.INSTANCE;
        double hp = cfg.mutatedChuckHP.get();
        double damage = cfg.mutatedChuckDamage.get();
        double speed = convertSpeedToAttribute(cfg.mutatedChuckSpeed.get());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (vomitCooldown > 0) vomitCooldown--;
    }

    private void shootVomit(LivingEntity target) {
        Vec3 direction = target.position().subtract(position()).normalize();
        SmallFireball projectile = new SmallFireball(
                level(), this,
                direction.scale(1.2)
        );
        projectile.setPos(getX(), getEyeY() - 0.1, getZ());
        level().addFreshEntity(projectile);

        playSound(SoundEvents.LLAMA_SPIT, 1.5f, 0.4f);
        vomitCooldown = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseZombieAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.ATTACK_DAMAGE, 3.6)
                .add(Attributes.MOVEMENT_SPEED, 0.13);
    }

    private static class VomitAttackGoal extends Goal {
        private final MutatedChuckZombie chuck;

        VomitAttackGoal(MutatedChuckZombie chuck) {
            this.chuck = chuck;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = chuck.getTarget();
            if (target == null || !target.isAlive()) return false;
            double dist = chuck.distanceTo(target);
            int range = ZombieConfig.INSTANCE.mutatedChuckVomitRange.get();
            return dist > 3.0 && dist <= range && chuck.vomitCooldown <= 0;
        }

        @Override
        public void start() {
            LivingEntity target = chuck.getTarget();
            if (target != null) chuck.shootVomit(target);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}
