package com.sevendaystominecraft.block.building;

import com.sevendaystominecraft.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * A thin, stick-like fence post used as tarp anchor points.
 * Stackable — players can place multiple posts on top of each other
 * to create taller poles.
 * <p>
 * To place a tarp, hold a tarp item and right-click 4 posts (one at
 * each corner). After the 4th click, the tarp stretches between all
 * four corner posts. The tarp is placed at the Y-level of the
 * highest post's top.
 */
public class TarpPostBlock extends Block {

    // Thin 2×2 pixel centered column, full height
    private static final VoxelShape SHAPE = Block.box(7, 0, 7, 9, 16, 9);

    // NBT keys for storing corner selections on the player
    private static final String KEY_CORNERS = "sevendtm_tarp_corners";
    private static final String KEY_TIER = "sevendtm_tarp_tier";

    public TarpPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack mainHand = player.getMainHandItem();

        // Check if player is holding a tarp item
        int tarpTier = getTarpTier(mainHand);
        if (tarpTier <= 0) return InteractionResult.PASS;

        // Find the TOP of this post stack (walk upward to find the highest connected post)
        BlockPos topPost = findTopOfStack(level, pos);

        var persistentData = player.getPersistentData();

        // Load existing corners
        List<BlockPos> corners = loadCorners(persistentData);
        int storedTier = persistentData.getInt(KEY_TIER);

        // If switching tarp tier mid-selection, reset
        if (!corners.isEmpty() && storedTier != tarpTier) {
            corners.clear();
            player.displayClientMessage(
                    Component.literal("§cTarp tier changed — selection reset."), true);
        }

        // Check for duplicate corner (same X,Z column)
        for (BlockPos existing : corners) {
            if (existing.getX() == topPost.getX() && existing.getZ() == topPost.getZ()) {
                player.displayClientMessage(
                        Component.literal("§cThat post column is already selected! Pick a different corner."), true);
                return InteractionResult.SUCCESS;
            }
        }

        corners.add(topPost);

        if (corners.size() < 4) {
            // Not enough corners yet
            saveCorners(persistentData, corners, tarpTier);
            player.displayClientMessage(
                    Component.literal("§eCorner " + corners.size() + "/4 set. Right-click " +
                            (4 - corners.size()) + " more post(s)."), true);
        } else {
            // All 4 corners selected — try to place the tarp
            clearCorners(persistentData);

            boolean placed = TarpPlacer.tryPlaceTarp(level, corners, tarpTier);
            if (placed) {
                if (!player.isCreative()) {
                    mainHand.shrink(1);
                }
                player.displayClientMessage(Component.literal("§aTarp placed!"), true);
            } else {
                player.displayClientMessage(
                        Component.literal("§cCannot place tarp — area too large, " +
                                "corners don't form a rectangle, or area is obstructed!"), true);
            }
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Walks upward from the clicked pos to find the topmost continuous tarp post.
     * This is the anchor point for the tarp placement.
     */
    private BlockPos findTopOfStack(Level level, BlockPos pos) {
        BlockPos current = pos;
        while (level.getBlockState(current.above()).getBlock() instanceof TarpPostBlock) {
            current = current.above();
        }
        return current;
    }

    /**
     * Returns the tarp tier (1=small 3×3, 2=medium 6×6, 3=large 15×15) or 0 if not a tarp.
     */
    public static int getTarpTier(ItemStack stack) {
        if (stack.is(ModItems.TARP_SMALL.get())) return 1;
        if (stack.is(ModItems.TARP_MEDIUM.get())) return 2;
        if (stack.is(ModItems.TARP_LARGE.get())) return 3;
        return 0;
    }

    // =========================================================================
    // Persistent data helpers for storing 4-corner selection
    // =========================================================================

    private static List<BlockPos> loadCorners(CompoundTag persistentData) {
        List<BlockPos> corners = new ArrayList<>();
        if (persistentData.contains(KEY_CORNERS, Tag.TAG_LIST)) {
            ListTag list = persistentData.getList(KEY_CORNERS, Tag.TAG_LONG);
            for (int i = 0; i < list.size(); i++) {
                corners.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
            }
        }
        return corners;
    }

    private static void saveCorners(CompoundTag persistentData, List<BlockPos> corners, int tier) {
        ListTag list = new ListTag();
        for (BlockPos p : corners) {
            list.add(LongTag.valueOf(p.asLong()));
        }
        persistentData.put(KEY_CORNERS, list);
        persistentData.putInt(KEY_TIER, tier);
    }

    private static void clearCorners(CompoundTag persistentData) {
        persistentData.remove(KEY_CORNERS);
        persistentData.remove(KEY_TIER);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("§7Tarp anchor post — stackable for height."));
        tooltip.add(Component.literal("§7Hold a tarp and right-click 4 posts"));
        tooltip.add(Component.literal("§7to stretch a rain shelter between them."));
    }
}
