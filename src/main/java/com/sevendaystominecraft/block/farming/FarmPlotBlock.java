package com.sevendaystominecraft.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FarmPlotBlock extends Block {

    public static final BooleanProperty HYDRATED = BooleanProperty.create("hydrated");

    private static final int HYDRATION_DECAY_TICKS = 2400;

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

    public FarmPlotBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HYDRATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HYDRATED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public void setHydrated(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(HYDRATED, true), 3);
        level.scheduleTick(pos, this, HYDRATION_DECAY_TICKS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HYDRATED)) {
            level.setBlock(pos, state.setValue(HYDRATED, false), 3);
        }
    }

    public boolean isHydrated(BlockState state) {
        return state.getValue(HYDRATED);
    }
}
