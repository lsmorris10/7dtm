package com.sevendaystominecraft.block.loot;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class LootContainerBlock extends BaseEntityBlock {

    public static final MapCodec<LootContainerBlock> CODEC = simpleCodec(p -> new LootContainerBlock(p, LootContainerType.CARDBOARD_BOX));

    private final LootContainerType containerType;

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
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LootContainerBlockEntity(pos, state, containerType);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LootContainerBlockEntity lootBE) {
                lootBE.tryGenerateLoot(serverPlayer);
                com.sevendaystominecraft.sound.ModSounds.playAtBlock(
                        com.sevendaystominecraft.sound.ModSounds.LOOT_OPEN, level, pos,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
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
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
