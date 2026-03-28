package com.sevendaystominecraft.entity;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AirdropPlaneEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Boolean> HAS_DROPPED = SynchedEntityData.defineId(AirdropPlaneEntity.class, EntityDataSerializers.BOOLEAN);

    private double targetX;
    private double targetZ;
    private double speed = 1.25;

    public AirdropPlaneEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setTargetPosition(double x, double z) {
        this.targetX = x;
        this.targetZ = z;
        
        // Calculate velocity
        double dx = x - this.getX();
        double dz = z - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        
        if (dist > 0) {
            this.setDeltaMovement((dx / dist) * speed, 0, (dz / dist) * speed);
            this.setYRot((float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F);
            this.yRotO = this.getYRot();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HAS_DROPPED, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.targetX = compound.getDouble("TargetX");
        this.targetZ = compound.getDouble("TargetZ");
        this.entityData.set(HAS_DROPPED, compound.getBoolean("HasDropped"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putDouble("TargetX", this.targetX);
        compound.putDouble("TargetZ", this.targetZ);
        compound.putBoolean("HasDropped", this.entityData.get(HAS_DROPPED));
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY(), this.getZ() + movement.z);

        if (this.level().isClientSide) {
            // Engine particles
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, 
                this.getX(), this.getY() - 0.5, this.getZ(), 
                0, 0, 0);
                
            // Ambient sound
            if (this.tickCount % 20 == 0) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), 
                    SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.MASTER, 2.0F, 0.5F, false);
            }
        } else {
            // Drop logic
            boolean dropped = this.entityData.get(HAS_DROPPED);
            if (!dropped) {
                double distToTarget = Math.sqrt(Math.pow(this.getX() - targetX, 2) + Math.pow(this.getZ() - targetZ, 2));
                if (distToTarget < 5.0) {
                    this.entityData.set(HAS_DROPPED, true);
                    dropPayload();
                }
            }

            // Despawn logic (fly far away)
            double originDist = Math.sqrt(Math.pow(this.getX() - targetX, 2) + Math.pow(this.getZ() - targetZ, 2));
            if (dropped && originDist > 1500) {
                this.discard();
            }
        }
    }

    private void dropPayload() {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Instantiate falling airdrop
            FallingAirdropEntity drop = new FallingAirdropEntity(ModEntities.FALLING_AIRDROP.get(), serverLevel);
            drop.setPos(this.getX(), this.getY() - 2.0, this.getZ());
            serverLevel.addFreshEntity(drop);
            SevenDaysToMinecraft.LOGGER.info("[BZHS] Airdrop plane dropped payload at {}, {}!", this.getX(), this.getZ());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
