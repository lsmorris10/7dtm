package com.sevendaystominecraft.block;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GasCanBlock extends Block {

    private static final float EXPLOSION_RADIUS = 4.0f;
    private static final float BASE_DAMAGE = 60.0f;
    private static final float DEMO_EXPERT_DAMAGE_BONUS_PER_RANK = 0.20f;
    private static final int DEMO_EXPERT_RADIUS_BONUS_RANK = 5;
    private static final float DEMO_EXPERT_RADIUS_BONUS = 1.0f;

    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);

    private static final ThreadLocal<Set<BlockPos>> DETONATING = ThreadLocal.withInitial(HashSet::new);

    public static boolean isGasCanExplosion = false;

    public GasCanBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide()) {
            BlockPos pos = hit.getBlockPos();
            Entity owner = projectile.getOwner();
            DETONATING.get().add(pos);
            level.removeBlock(pos, false);
            DETONATING.get().remove(pos);
            triggerExplosion(level, pos, owner);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide() && !state.is(newState.getBlock()) && !DETONATING.get().contains(pos)) {
            triggerExplosion(level, pos, null);
        }
    }

    private void triggerExplosion(Level level, BlockPos pos, Entity sourceEntity) {
        if (!(level instanceof ServerLevel sl)) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        isGasCanExplosion = true;
        sl.explode(null, x, y, z, EXPLOSION_RADIUS, true, Level.ExplosionInteraction.NONE);
        isGasCanExplosion = false;

        int fireRadius = (int) EXPLOSION_RADIUS;
        for (int dx = -fireRadius; dx <= fireRadius; dx++) {
            for (int dz = -fireRadius; dz <= fireRadius; dz++) {
                if (dx * dx + dz * dz > fireRadius * fireRadius) continue;
                BlockPos firePos = pos.offset(dx, 0, dz);
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos adjusted = firePos.above(dy);
                    if (sl.getBlockState(adjusted).isAir()
                            && sl.getBlockState(adjusted.below()).isSolidRender()) {
                        if (sl.random.nextFloat() < 0.3f) {
                            sl.setBlock(adjusted, Blocks.FIRE.defaultBlockState(), 3);
                        }
                        break;
                    }
                }
            }
        }

        int demoRank = 0;
        if (sourceEntity instanceof Player player
                && player.hasData(ModAttachments.PLAYER_STATS.get())) {
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            demoRank = stats.getPerkRank("demolitions_expert");
        }

        float effectiveRadius = EXPLOSION_RADIUS;
        if (demoRank >= DEMO_EXPERT_RADIUS_BONUS_RANK) {
            effectiveRadius += DEMO_EXPERT_RADIUS_BONUS;
        }

        float damageMultiplier = 1.0f + (DEMO_EXPERT_DAMAGE_BONUS_PER_RANK * demoRank);

        Vec3 center = new Vec3(x, y, z);
        AABB searchArea = new AABB(
                center.x - effectiveRadius, center.y - effectiveRadius, center.z - effectiveRadius,
                center.x + effectiveRadius, center.y + effectiveRadius, center.z + effectiveRadius
        );
        List<LivingEntity> entities = sl.getEntitiesOfClass(LivingEntity.class, searchArea);

        DamageSource explosionSource = sourceEntity instanceof LivingEntity livingSource
                ? sl.damageSources().explosion(null, livingSource)
                : sl.damageSources().explosion(null, null);

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
}
