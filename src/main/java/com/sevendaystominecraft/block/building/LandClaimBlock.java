package com.sevendaystominecraft.block.building;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class LandClaimBlock extends Block {

    public static final int PROTECTION_RADIUS = 41;

    public LandClaimBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel && placer instanceof Player player) {
            UUID playerId = player.getUUID();
            LandClaimData data = LandClaimData.getOrCreate(serverLevel);

            BlockPos existingClaim = data.getClaim(playerId);
            if (existingClaim != null && !existingClaim.equals(pos)) {
                if (serverLevel.getBlockState(existingClaim).getBlock() instanceof LandClaimBlock) {
                    serverLevel.destroyBlock(existingClaim, true);
                }
                player.displayClientMessage(Component.literal("Previous Land Claim removed. New claim placed."), true);
            }

            data.setClaim(playerId, pos);
            player.displayClientMessage(Component.literal("Land Claim placed! " + PROTECTION_RADIUS + "-block protection zone active."), true);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            LandClaimData data = LandClaimData.getOrCreate(serverLevel);
            data.removeClaimAtPos(pos);
            player.displayClientMessage(Component.literal("Land Claim removed."), true);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            LandClaimData data = LandClaimData.getOrCreate(serverLevel);
            data.removeClaimAtPos(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static boolean isWithinAnyClaimRadius(ServerLevel level, BlockPos pos) {
        LandClaimData data = LandClaimData.getOrCreate(level);
        return data.isWithinAnyClaimRadius(pos, PROTECTION_RADIUS);
    }

    public static boolean isWithinClaimRadius(BlockPos claimPos, BlockPos testPos) {
        double dx = testPos.getX() - claimPos.getX();
        double dy = testPos.getY() - claimPos.getY();
        double dz = testPos.getZ() - claimPos.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= PROTECTION_RADIUS;
    }
}
