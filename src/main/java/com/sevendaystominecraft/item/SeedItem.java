package com.sevendaystominecraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.sevendaystominecraft.block.farming.FarmPlotBlock;

import java.util.function.Supplier;

public class SeedItem extends Item {

    private final Supplier<Block> cropBlock;

    public SeedItem(Properties properties, Supplier<Block> cropBlock) {
        super(properties);
        this.cropBlock = cropBlock;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof FarmPlotBlock) {
            BlockPos above = pos.above();
            if (level.getBlockState(above).isAir()) {
                if (!level.isClientSide) {
                    level.setBlock(above, cropBlock.get().defaultBlockState(), 3);
                    if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                        context.getItemInHand().shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
