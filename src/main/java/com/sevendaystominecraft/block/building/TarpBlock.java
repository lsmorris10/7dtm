package com.sevendaystominecraft.block.building;

import com.sevendaystominecraft.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A thin, flat canopy block that forms the stretched tarp surface.
 * Has a SAG_LEVEL property (0-3) that visually offsets the block downward
 * to create a realistic sagging tarp effect.
 * <p>
 * sag=0 → flush with post top (corners/edges near posts)
 * sag=1 → slight droop
 * sag=2 → moderate droop
 * sag=3 → maximum sag (center of tarp)
 */
public class TarpBlock extends BaseEntityBlock {

    public static final MapCodec<TarpBlock> CODEC = simpleCodec(TarpBlock::new);

    /** Sag level 0 (flush) to 3 (maximum droop). */
    public static final IntegerProperty SAG_LEVEL = IntegerProperty.create("sag", 0, 3);

    // Each sag level offsets the thin carpet shape downward by 3 pixels
    private static final VoxelShape SHAPE_SAG_0 = Block.box(0, 15, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_SAG_1 = Block.box(0, 12, 0, 16, 13, 16);
    private static final VoxelShape SHAPE_SAG_2 = Block.box(0, 9, 0, 16, 10, 16);
    private static final VoxelShape SHAPE_SAG_3 = Block.box(0, 6, 0, 16, 7, 16);
    private static final VoxelShape[] SAG_SHAPES = {SHAPE_SAG_0, SHAPE_SAG_1, SHAPE_SAG_2, SHAPE_SAG_3};

    public TarpBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SAG_LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SAG_LEVEL);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SAG_SHAPES[state.getValue(SAG_LEVEL)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SAG_SHAPES[state.getValue(SAG_LEVEL)];
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TarpBlockEntity(pos, state);
    }

    /**
     * When any tarp block is broken, remove ALL connected tarp blocks and drop ONE tarp item.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TarpBlockEntity tarpBE) {
                List<BlockPos> siblings = tarpBE.getSiblingPositions();
                int tier = tarpBE.getTarpTier();

                // Remove all sibling tarp blocks (without triggering cascade)
                for (BlockPos sibling : siblings) {
                    if (!sibling.equals(pos) && level.getBlockState(sibling).getBlock() instanceof TarpBlock) {
                        // Set to air without triggering onRemove cascade
                        level.removeBlock(sibling, false);
                    }
                }

                // Drop the tarp item
                ItemStack drop = TarpPlacer.getTarpItemForTier(tier);
                if (!drop.isEmpty()) {
                    Block.popResource(level, pos, drop);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("§7Part of a tarp shelter."));
        tooltip.add(Component.literal("§7Blocks rain from reaching below."));
    }

    // =========================================================================
    // Block Entity for tracking tarp connections
    // =========================================================================

    public static class TarpBlockEntity extends BlockEntity {
        private final List<BlockPos> siblingPositions = new ArrayList<>();
        private int tarpTier = 1;

        public TarpBlockEntity(BlockPos pos, BlockState state) {
            super(ModBlockEntities.TARP_BE.get(), pos, state);
        }

        public List<BlockPos> getSiblingPositions() {
            return siblingPositions;
        }

        public int getTarpTier() {
            return tarpTier;
        }

        public void setTarpTier(int tier) {
            this.tarpTier = tier;
            setChanged();
        }

        public void setSiblingPositions(List<BlockPos> positions) {
            this.siblingPositions.clear();
            this.siblingPositions.addAll(positions);
            setChanged();
        }

        @Override
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            ListTag list = new ListTag();
            for (BlockPos p : siblingPositions) {
                list.add(LongTag.valueOf(p.asLong()));
            }
            tag.put("Siblings", list);
            tag.putInt("TarpTier", tarpTier);
        }

        @Override
        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            siblingPositions.clear();
            if (tag.contains("Siblings", Tag.TAG_LIST)) {
                ListTag list = tag.getList("Siblings", Tag.TAG_LONG);
                for (int i = 0; i < list.size(); i++) {
                    siblingPositions.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
                }
            }
            tarpTier = tag.getInt("TarpTier");
        }
    }
}
