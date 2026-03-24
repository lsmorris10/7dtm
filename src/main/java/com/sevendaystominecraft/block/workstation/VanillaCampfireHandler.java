package com.sevendaystominecraft.block.workstation;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HeatmapConfig;
import com.sevendaystominecraft.heatmap.HeatmapManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class VanillaCampfireHandler {

    private static final Map<BlockPos, Integer> heatTickCounters = new HashMap<>();
    private static final Map<BlockPos, Float> campfireHeatContributed = new HashMap<>();
    private static final float HEAT_PER_MINUTE = 1.0f;
    private static final float HEAT_DECAY_PER_MINUTE = 0.1f;
    private static final int HEAT_RADIUS_CHUNKS = 2;
    private static final float MAX_CAMPFIRE_HEAT = 10.0f;

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);

        if (!(state.getBlock() instanceof CampfireBlock)) return;
        if (state.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)) return;
        if (!state.hasProperty(CampfireBlock.LIT) || !state.getValue(CampfireBlock.LIT)) return;

        ItemStack heldItem = serverPlayer.getItemInHand(event.getHand());
        if (heldItem.getItem() instanceof ShovelItem) return;
        if (heldItem.getItem() instanceof FlintAndSteelItem) return;
        if (heldItem.is(Items.FIRE_CHARGE)) return;

        ServerLevel serverLevel = (ServerLevel) event.getLevel();

        CampfireWorkstationSavedData savedData = CampfireWorkstationSavedData.get(serverLevel);
        CampfireWorkstationSavedData.CampfireData data = savedData.getOrCreate(pos);
        data.setLastInteractedPlayer(serverPlayer.getUUID());

        CampfireDataBlockEntity fakeBE = new CampfireDataBlockEntity(pos, state, data);

        serverPlayer.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal(WorkstationType.CAMPFIRE.getDisplayName());
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                return new WorkstationMenu(containerId, playerInv, fakeBE);
            }
        }, buf -> {
            buf.writeBlockPos(pos);
            buf.writeInt(WorkstationType.CAMPFIRE.ordinal());
        });

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        CampfireWorkstationSavedData savedData = CampfireWorkstationSavedData.get(serverLevel);
        List<BlockPos> toRemove = new ArrayList<>();

        for (Map.Entry<BlockPos, CampfireWorkstationSavedData.CampfireData> entry : savedData.allEntries()) {
            BlockPos pos = entry.getKey();
            CampfireWorkstationSavedData.CampfireData data = entry.getValue();

            if (!serverLevel.isLoaded(pos)) continue;

            BlockState state = serverLevel.getBlockState(pos);
            boolean isLitCampfire = state.getBlock() instanceof CampfireBlock
                    && !state.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)
                    && state.hasProperty(CampfireBlock.LIT)
                    && state.getValue(CampfireBlock.LIT);

            if (!isLitCampfire) {
                if (!(state.getBlock() instanceof CampfireBlock)) {
                    if (data.hasItems()) {
                        dropItems(serverLevel, pos, data);
                    }
                    toRemove.add(pos);
                }
                heatTickCounters.remove(pos);
                campfireHeatContributed.remove(pos);
                continue;
            }

            if (data.tick(serverLevel)) {
                savedData.setDirty();
            }

            if (HeatmapConfig.INSTANCE.enabled.get()) {
                int ticks = heatTickCounters.getOrDefault(pos, 0) + 1;
                if (ticks >= 1200) {
                    float contributed = campfireHeatContributed.getOrDefault(pos, 0f);
                    if (contributed < MAX_CAMPFIRE_HEAT) {
                        float effectiveHeat = Math.min(HEAT_PER_MINUTE, MAX_CAMPFIRE_HEAT - contributed);
                        ChunkPos chunkPos = new ChunkPos(pos);
                        HeatmapManager.addHeat(serverLevel, chunkPos, effectiveHeat, HEAT_RADIUS_CHUNKS, HEAT_DECAY_PER_MINUTE);
                        campfireHeatContributed.put(pos, contributed + effectiveHeat);
                    }
                    ticks = 0;
                }
                heatTickCounters.put(pos, ticks);
            }
        }

        for (BlockPos pos : toRemove) {
            savedData.remove(pos);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof CampfireBlock)) return;

        CampfireWorkstationSavedData savedData = CampfireWorkstationSavedData.get(serverLevel);
        CampfireWorkstationSavedData.CampfireData data = savedData.get(event.getPos());
        if (data != null) {
            dropItems(serverLevel, event.getPos(), data);
            savedData.remove(event.getPos());
        }
    }

    private static void dropItems(ServerLevel level, BlockPos pos, CampfireWorkstationSavedData.CampfireData data) {
        NonNullList<ItemStack> items = data.getItems();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                data.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
