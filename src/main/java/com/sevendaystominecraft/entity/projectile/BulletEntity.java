package com.sevendaystominecraft.entity.projectile;

import com.sevendaystominecraft.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BulletEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Float> DATA_GRAVITY =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_MAX_LIFETIME =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);

    private float bulletDamage = 8.0f;
    private int lifeTicks = 0;

    public BulletEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public BulletEntity(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.BULLET.get(), shooter, level, new ItemStack(Items.IRON_NUGGET));
        this.bulletDamage = damage;
    }

    public BulletEntity(Level level, LivingEntity shooter, float damage, double gravity, int maxLifetime) {
        super(ModEntities.BULLET.get(), shooter, level, new ItemStack(Items.IRON_NUGGET));
        this.bulletDamage = damage;
        this.entityData.set(DATA_GRAVITY, (float) gravity);
        this.entityData.set(DATA_MAX_LIFETIME, maxLifetime);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GRAVITY, 0.01f);
        builder.define(DATA_MAX_LIFETIME, 60);
    }

    public void setBulletDamage(float damage) {
        this.bulletDamage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.IRON_NUGGET;
    }

    @Override
    protected double getDefaultGravity() {
        return this.entityData.get(DATA_GRAVITY);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        if (lifeTicks >= this.entityData.get(DATA_MAX_LIFETIME) && !level().isClientSide()) {
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("BulletGravity", this.entityData.get(DATA_GRAVITY));
        tag.putInt("BulletMaxLifetime", this.entityData.get(DATA_MAX_LIFETIME));
        tag.putFloat("BulletDamage", this.bulletDamage);
        tag.putInt("BulletLifeTicks", this.lifeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BulletGravity")) {
            this.entityData.set(DATA_GRAVITY, tag.getFloat("BulletGravity"));
        }
        if (tag.contains("BulletMaxLifetime")) {
            this.entityData.set(DATA_MAX_LIFETIME, tag.getInt("BulletMaxLifetime"));
        }
        if (tag.contains("BulletDamage")) {
            this.bulletDamage = tag.getFloat("BulletDamage");
        }
        if (tag.contains("BulletLifeTicks")) {
            this.lifeTicks = tag.getInt("BulletLifeTicks");
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity living && level() instanceof ServerLevel sl) {
            LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;
            living.hurtServer(sl, damageSources().mobProjectile(this, owner), bulletDamage);
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
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.CRIT,
                        getX(), getY(), getZ(),
                        (random.nextDouble() - 0.5) * 0.1,
                        random.nextDouble() * 0.1,
                        (random.nextDouble() - 0.5) * 0.1);
            }
        }
    }
}
