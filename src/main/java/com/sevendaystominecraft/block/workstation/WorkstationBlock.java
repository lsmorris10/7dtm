package com.sevendaystominecraft.block.workstation;

import com.mojang.serialization.MapCodec;
import com.sevendaystominecraft.block.ModBlockEntities;

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

public class WorkstationBlock extends BaseEntityBlock {

    public static final MapCodec<WorkstationBlock> CODEC = simpleCodec(p -> new WorkstationBlock(p, WorkstationType.WORKBENCH));

    private final WorkstationType workstationType;

    public WorkstationBlock(Properties properties, WorkstationType workstationType) {
        super(properties);
        this.workstationType = workstationType;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public WorkstationType getWorkstationType() {
        return workstationType;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorkstationBlockEntity(pos, state, workstationType);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WorkstationBlockEntity workstationBE) {
                serverPlayer.openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.literal(workstationType.getDisplayName());
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                        return new WorkstationMenu(containerId, playerInv, workstationBE);
                    }
                }, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.WORKSTATION_BE.get(),
                (lvl, p, st, be) -> be.serverTick(lvl, p, st));
    }
}
