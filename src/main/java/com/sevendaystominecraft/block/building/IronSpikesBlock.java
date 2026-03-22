package com.sevendaystominecraft.block.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class IronSpikesBlock extends Block {

    public static final IntegerProperty DURABILITY = IntegerProperty.create("durability", 0, 20);
    private static final float DAMAGE = 8.0f;

    public IronSpikesBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(DURABILITY, 20));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DURABILITY);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            int durability = state.getValue(DURABILITY);
            if (durability > 0) {
                living.hurt(level.damageSources().cactus(), DAMAGE);
                int newDurability = durability - 1;
                if (newDurability <= 0) {
                    level.destroyBlock(pos, false);
                } else {
                    level.setBlock(pos, state.setValue(DURABILITY, newDurability), 3);
                }
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
