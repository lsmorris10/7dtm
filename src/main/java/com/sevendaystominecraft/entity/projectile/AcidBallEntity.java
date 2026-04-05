package com.sevendaystominecraft.entity.projectile;

import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AcidBallEntity extends ThrowableItemProjectile {

    public AcidBallEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public AcidBallEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.ACID_BALL.get(), shooter, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity living && level() instanceof ServerLevel sl) {
            float damage = ZombieConfig.INSTANCE.copBileDamage.get().floatValue();
            living.hurtServer(sl, damageSources().mobProjectile(this, (LivingEntity) getOwner()), damage);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                level().addParticle(ParticleTypes.ITEM_SLIME,
                        getX(), getY(), getZ(),
                        (random.nextDouble() - 0.5) * 0.1,
                        random.nextDouble() * 0.1,
                        (random.nextDouble() - 0.5) * 0.1);
            }
        }
    }
}
