package com.sevendaystominecraft.block.power;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class WireConnectionHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        ItemStack heldItem = serverPlayer.getMainHandItem();
        if (heldItem.isEmpty() || !heldItem.is(ModItems.ELECTRICAL_PARTS.get())) return;

        BlockPos clickedPos = event.getPos();
        Block clickedBlock = serverLevel.getBlockState(clickedPos).getBlock();
        BlockEntity clickedBE = serverLevel.getBlockEntity(clickedPos);
        PowerGridManager grid = PowerGridManager.get(serverLevel);

        boolean isSource = clickedBE instanceof PowerSourceBlockEntity;
        boolean isDevice = clickedBlock instanceof PoweredDeviceBlock;

        if (!isSource && !isDevice) return;

        event.setCanceled(true);

        BlockPos linkingSource = grid.getLinkingSource(serverPlayer.getUUID());

        if (linkingSource == null) {
            if (isSource) {
                grid.startLinking(serverPlayer.getUUID(), clickedPos);
                serverPlayer.displayClientMessage(
                        Component.literal("Wire started from power source. Right-click a device to connect."), true);
            } else {
                serverPlayer.displayClientMessage(
                        Component.literal("Right-click a power source first to start a wire connection."), true);
            }
            return;
        }

        if (clickedPos.equals(linkingSource)) {
            grid.cancelLinking(serverPlayer.getUUID());
            serverPlayer.displayClientMessage(Component.literal("Wire connection cancelled."), true);
            return;
        }

        double distSqr = linkingSource.distSqr(clickedPos);
        int maxRange = PowerGridManager.getMaxWireRange();
        if (distSqr > (long) maxRange * maxRange) {
            serverPlayer.displayClientMessage(
                    Component.literal("Too far! Maximum wire range is " + maxRange + " blocks."), true);
            grid.cancelLinking(serverPlayer.getUUID());
            return;
        }

        if (!isDevice) {
            serverPlayer.displayClientMessage(
                    Component.literal("Target must be a powered device (Electric Fence, Blade Trap, or Battery Bank)."), true);
            grid.cancelLinking(serverPlayer.getUUID());
            return;
        }

        BlockEntity sourceBE = serverLevel.getBlockEntity(linkingSource);
        if (!(sourceBE instanceof PowerSourceBlockEntity)) {
            serverPlayer.displayClientMessage(Component.literal("Source block no longer exists."), true);
            grid.cancelLinking(serverPlayer.getUUID());
            return;
        }

        if (grid.addConnection(linkingSource, clickedPos)) {
            if (!serverPlayer.isCreative()) {
                heldItem.shrink(1);
            }
            serverPlayer.displayClientMessage(Component.literal("Wire connected!"), true);
        } else {
            serverPlayer.displayClientMessage(Component.literal("Connection failed."), true);
        }

        grid.cancelLinking(serverPlayer.getUUID());
    }
}
