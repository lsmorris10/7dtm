package com.sevendaystominecraft.entity.projectile;

import com.sevendaystominecraft.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class GrenadeEntity extends ThrowableItemProjectile {

    private static final int FUSE_TICKS = 60;
    private static final float EXPLOSION_RADIUS = 5.0f;
    private int fuseTicks = FUSE_TICKS;

    public GrenadeEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public GrenadeEntity(Level level, LivingEntity thrower) {
        super(ModEntities.GRENADE.get(), thrower, level, new ItemStack(Items.FIRE_CHARGE));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            fuseTicks--;
            if (fuseTicks <= 0) {
                explode();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
    }

    private void explode() {
        if (level() instanceof ServerLevel sl) {
            sl.explode(this, getX(), getY(), getZ(), EXPLOSION_RADIUS,
                    Level.ExplosionInteraction.NONE);
        }
        discard();
    }
}
