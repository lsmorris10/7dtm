package com.sevendaystominecraft.entity.projectile;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GrenadeEntity extends ThrowableItemProjectile {

    private static final int FUSE_TICKS = 60;
    private static final float EXPLOSION_RADIUS = 5.0f;
    private static final float BASE_DAMAGE = 70.0f;
    private static final float DEMO_EXPERT_DAMAGE_BONUS_PER_RANK = 0.20f;
    private static final int DEMO_EXPERT_RADIUS_BONUS_RANK = 5;
    private static final float DEMO_EXPERT_RADIUS_BONUS = 1.0f;
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

            Entity throwerEntity = getOwner();
            int demoRank = 0;
            if (throwerEntity instanceof Player player
                    && player.hasData(ModAttachments.PLAYER_STATS.get())) {
                SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
                demoRank = stats.getPerkRank("demolitions_expert");
            }

            float effectiveRadius = EXPLOSION_RADIUS;
            if (demoRank >= DEMO_EXPERT_RADIUS_BONUS_RANK) {
                effectiveRadius += DEMO_EXPERT_RADIUS_BONUS;
            }

            float damageMultiplier = 1.0f + (DEMO_EXPERT_DAMAGE_BONUS_PER_RANK * demoRank);

            Vec3 center = new Vec3(getX(), getY(), getZ());
            AABB searchArea = new AABB(
                    center.x - effectiveRadius, center.y - effectiveRadius, center.z - effectiveRadius,
                    center.x + effectiveRadius, center.y + effectiveRadius, center.z + effectiveRadius
            );
            List<LivingEntity> entities = sl.getEntitiesOfClass(LivingEntity.class, searchArea);

            DamageSource explosionSource = throwerEntity instanceof LivingEntity livingThrower
                    ? sl.damageSources().explosion(this, livingThrower)
                    : sl.damageSources().explosion(this, null);

            for (LivingEntity target : entities) {
                double dist = target.position().distanceTo(center);
                if (dist > effectiveRadius) continue;

                double falloff = 1.0 - (dist / effectiveRadius);
                float damage = (float) (BASE_DAMAGE * falloff * falloff) * damageMultiplier;

                if (damage > 0) {
                    target.hurtServer(sl, explosionSource, damage);
                }
            }
        }
        discard();
    }
}
