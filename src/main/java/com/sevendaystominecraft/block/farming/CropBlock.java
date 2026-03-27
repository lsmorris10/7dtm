package com.sevendaystominecraft.block.farming;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class CropBlock extends Block {

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(0, 0, 0, 16, 10, 16),
            Block.box(0, 0, 0, 16, 14, 16)
    };

    private final Supplier<Item> seedItem;
    private final Supplier<Item> cropItem;
    private final int minDrop;
    private final int maxDrop;

    public CropBlock(Properties properties, Supplier<Item> seedItem, Supplier<Item> cropItem, int minDrop, int maxDrop) {
        super(properties);
        this.seedItem = seedItem;
        this.cropItem = cropItem;
        this.minDrop = minDrop;
        this.maxDrop = maxDrop;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof FarmBlock;
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                      net.minecraft.world.level.ScheduledTickAccess tickAccess,
                                      BlockPos pos, net.minecraft.core.Direction direction,
                                      BlockPos neighborPos, BlockState neighborState,
                                      net.minecraft.util.RandomSource random) {
        if (direction == net.minecraft.core.Direction.DOWN && !canSurvive(state, level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= MAX_AGE) return;

        int lightLevel = level.getMaxLocalRawBrightness(pos.above());
        if (lightLevel >= 9) {
            int threshold = 1;
            BlockState belowState = level.getBlockState(pos.below());
            if (belowState.getBlock() instanceof FarmBlock) {
                int moisture = belowState.getValue(FarmBlock.MOISTURE);
                if (moisture > 0) {
                    threshold = 2;
                }
            }

            if (random.nextInt(5) < threshold) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        int age = state.getValue(AGE);
        if (age < MAX_AGE) return InteractionResult.PASS;

        harvestCrop(level, pos, state, player);
        return InteractionResult.SUCCESS;
    }

    public boolean tryApplyFertilizer(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide) return false;

        int age = state.getValue(AGE);
        if (age >= MAX_AGE) return false;

        level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        return true;
    }

    private void harvestCrop(Level level, BlockPos pos, BlockState state, Player player) {
        RandomSource random = level.random;

        int greenThumbBonus = 0;
        if (player.hasData(ModAttachments.PLAYER_STATS.get())) {
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            greenThumbBonus = stats.getPerkRank("green_thumb");
        }

        int dropCount = minDrop + random.nextInt(maxDrop - minDrop + 1) + greenThumbBonus;
        Block.popResource(level, pos, new ItemStack(cropItem.get(), dropCount));

        if (random.nextFloat() < 0.65f) {
            int seedCount = 1 + (random.nextFloat() < 0.3f ? 1 : 0);
            Block.popResource(level, pos, new ItemStack(seedItem.get(), seedCount));
        }

        level.setBlock(pos, defaultBlockState().setValue(AGE, 0), 2);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && state.getValue(AGE) == MAX_AGE) {
            int greenThumbBonus = 0;
            if (player.hasData(ModAttachments.PLAYER_STATS.get())) {
                SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
                greenThumbBonus = stats.getPerkRank("green_thumb");
            }

            RandomSource random = level.random;
            int dropCount = minDrop + random.nextInt(maxDrop - minDrop + 1) + greenThumbBonus;
            Block.popResource(level, pos, new ItemStack(cropItem.get(), dropCount));

            if (random.nextFloat() < 0.65f) {
                Block.popResource(level, pos, new ItemStack(seedItem.get(), 1));
            }
        } else if (!level.isClientSide) {
            Block.popResource(level, pos, new ItemStack(seedItem.get(), 1));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
