package com.sevendaystominecraft.block.farming;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class FarmPlotInteractionHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack held = player.getMainHandItem();

        if (!(held.getItem() instanceof HoeItem)) return;

        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, ModBlocks.FARM_PLOT_BLOCK.get().defaultBlockState(), 3);
            if (!player.getAbilities().instabuild) {
                held.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.HOE_TILL, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }
}
