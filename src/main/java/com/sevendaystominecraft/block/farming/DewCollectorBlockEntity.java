package com.sevendaystominecraft.block.farming;

import com.sevendaystominecraft.block.ModBlockEntities;
import com.sevendaystominecraft.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DewCollectorBlockEntity extends BlockEntity {

    private static final int SLOT_COUNT = 4;
    private static final int GENERATION_INTERVAL = 6000;
    private static final int MAX_STACK_PER_SLOT = 1;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int tickCounter = 0;

    public DewCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DEW_COLLECTOR_BE.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public int getContainerSize() {
        return SLOT_COUNT;
    }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        return items.get(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) return;
        items.set(slot, stack);
        setChanged();
    }

    public ItemStack removeItem(int slot, int count) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        setChanged();
        return result;
    }

    public boolean stillValid(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    public void serverTick(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        if (!level.canSeeSky(pos.above())) return;

        tickCounter++;
        if (tickCounter >= GENERATION_INTERVAL) {
            tickCounter = 0;
            tryGenerateWater();
        }
    }

    private void tryGenerateWater() {
        ItemStack murkyWater = new ItemStack(ModItems.MURKY_WATER.get(), 1);

        for (int i = 0; i < items.size(); i++) {
            ItemStack existing = items.get(i);
            if (existing.isEmpty()) {
                items.set(i, murkyWater.copy());
                setChanged();
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("TickCounter", tickCounter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        tickCounter = tag.getInt("TickCounter");
    }
}
