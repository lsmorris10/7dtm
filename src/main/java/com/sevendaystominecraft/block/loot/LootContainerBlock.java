package com.sevendaystominecraft.block.loot;

import com.mojang.serialization.MapCodec;
import com.sevendaystominecraft.territory.TerritoryData;
import com.sevendaystominecraft.territory.TerritoryRecord;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class LootContainerBlock extends BaseEntityBlock {

    public static final MapCodec<LootContainerBlock> CODEC = simpleCodec(p -> new LootContainerBlock(p, LootContainerType.CARDBOARD_BOX));

    private final LootContainerType containerType;
    private static final VoxelShape TRASH_PILE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D);
    private static final VoxelShape MUNITIONS_BOX_SHAPE = Block.box(2.0D, 0.0D, 4.0D, 14.0D, 10.0D, 12.0D);

    public LootContainerBlock(Properties properties, LootContainerType containerType) {
        super(properties);
        this.containerType = containerType;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public LootContainerType getContainerType() {
        return containerType;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (this.containerType) {
            case TRASH_PILE -> TRASH_PILE_SHAPE;
            case MUNITIONS_BOX -> MUNITIONS_BOX_SHAPE;
            default -> super.getShape(state, level, pos, context);
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (this.containerType) {
            case TRASH_PILE -> TRASH_PILE_SHAPE;
            case MUNITIONS_BOX -> MUNITIONS_BOX_SHAPE;
            default -> super.getCollisionShape(state, level, pos, context);
        };
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LootContainerBlockEntity(pos, state, containerType);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LootContainerBlockEntity lootBE) {
                int tId = lootBE.getTerritoryId();
                if (tId > 0 && level instanceof ServerLevel serverLevel) {
                    TerritoryData data = TerritoryData.getOrCreate(serverLevel);
                    TerritoryRecord record = data.getTerritoryById(tId);
                    if (record != null && record.getZombiesRemaining() > 0) {
                        serverPlayer.sendSystemMessage(
                                Component.literal("Defeat all zombies first! (" + record.getZombiesRemaining() + " remaining)")
                                        .withStyle(ChatFormatting.RED));
                        return InteractionResult.CONSUME;
                    }
                }
                lootBE.tryGenerateLoot(serverPlayer);
                serverPlayer.openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.literal(containerType.getDisplayName());
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                        return new LootContainerMenu(containerId, playerInv, lootBE);
                    }
                }, pos);
                // Play sound AFTER opening menu so it doesn't block
                com.sevendaystominecraft.sound.ModSounds.playAtBlock(
                        com.sevendaystominecraft.sound.ModSounds.LOOT_OPEN, level, pos,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LootContainerBlockEntity lootBE) {
                Containers.dropContents(level, pos, lootBE.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
