package com.sevendaystominecraft.block.building;

import com.sevendaystominecraft.block.power.PowerGridManager;
import com.sevendaystominecraft.block.power.PoweredDeviceBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ElectricFencePostBlock extends Block implements PoweredDeviceBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final float DAMAGE = 5.0f;
    private static final int STUN_DURATION_TICKS = 40;

    public ElectricFencePostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && !(living instanceof Player)) {
            if (level instanceof ServerLevel serverLevel) {
                if (!PowerGridManager.get(serverLevel).isDevicePowered(serverLevel, pos)) {
                    return;
                }
            }
            living.hurt(level.damageSources().lightningBolt(), DAMAGE);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STUN_DURATION_TICKS, 5, false, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    0, 0.02, 0);
        } else {
            if (random.nextInt(5) == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5 + random.nextGaussian() * 0.2,
                        pos.getY() + 0.5 + random.nextGaussian() * 0.2,
                        pos.getZ() + 0.5 + random.nextGaussian() * 0.2,
                        0, 0, 0);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean powered = PowerGridManager.get(level).isDevicePowered(level, pos);
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel) {
                PowerGridManager.get(serverLevel).removeAllConnections(pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
