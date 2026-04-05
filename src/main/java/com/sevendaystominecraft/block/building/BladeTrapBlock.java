package com.sevendaystominecraft.block.building;

import com.sevendaystominecraft.block.power.PowerGridManager;
import com.sevendaystominecraft.block.power.PoweredDeviceBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BladeTrapBlock extends Block implements PoweredDeviceBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final float DAMAGE = 6.0f;
    private static final int TICK_INTERVAL = 20;

    public BladeTrapBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean powered = PowerGridManager.get(level).isDevicePowered(level, pos);
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }

        if (powered) {
            AABB area = new AABB(pos).inflate(1.0);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> !(e instanceof Player));
            for (LivingEntity entity : entities) {
                entity.hurt(level.damageSources().cactus(), DAMAGE);
            }
        }

        level.scheduleTick(pos, this, TICK_INTERVAL);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    0, 0.02, 0);
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
