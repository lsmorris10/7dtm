package com.sevendaystominecraft.block.farming;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

        if (state.getBlock() instanceof FarmPlotBlock farmPlot) {
            if (isWateringItem(held)) {
                if (level instanceof ServerLevel serverLevel) {
                    farmPlot.setHydrated(serverLevel, pos, state);
                    if (!player.getAbilities().instabuild) {
                        boolean wasWaterBucket = held.getItem() == Items.WATER_BUCKET;
                        held.shrink(1);
                        if (wasWaterBucket) {
                            player.getInventory().add(new ItemStack(Items.BUCKET));
                        }
                    }
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    event.setCanceled(true);
                    event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                    return;
                }
            }
        }

        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (held.getItem() == ModItems.FERTILIZER.get()) {
                if (cropBlock.tryApplyFertilizer(level, pos, state, player)) {
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    event.setCanceled(true);
                    event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                    return;
                }
            }
        }

        if (!(held.getItem() instanceof HoeItem)) return;

        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, ModBlocks.FARM_PLOT_BLOCK.get().defaultBlockState(), 3);
            if (!player.getAbilities().instabuild) {
                held.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }

    private static boolean isWateringItem(ItemStack stack) {
        return stack.getItem() == Items.WATER_BUCKET || stack.getItem() == ModItems.MURKY_WATER.get();
    }
}
