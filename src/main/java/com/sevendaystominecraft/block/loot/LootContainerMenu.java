package com.sevendaystominecraft.block.loot;

import com.sevendaystominecraft.menu.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LootContainerMenu extends AbstractContainerMenu {

    private final LootContainerBlockEntity blockEntity;
    private final int containerSlotCount;

    public LootContainerMenu(int containerId, Inventory playerInv, LootContainerBlockEntity blockEntity) {
        super(ModMenuTypes.LOOT_CONTAINER_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        int slotCount = blockEntity.getContainerSize();
        this.containerSlotCount = slotCount;
        int cols = Math.min(slotCount, 9);
        int rows = (int) Math.ceil((double) slotCount / cols);

        LootContainerSlotContainer container = new LootContainerSlotContainer(blockEntity);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = row * cols + col;
                if (idx < slotCount) {
                    addSlot(new Slot(container, idx, 8 + col * 18, 18 + row * 18));
                }
            }
        }

        int playerInvY = 18 + rows * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public static LootContainerMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (be instanceof LootContainerBlockEntity lcbe) {
            return new LootContainerMenu(containerId, playerInv, lcbe);
        }
        return new LootContainerMenu(containerId, playerInv, new LootContainerBlockEntity(pos,
                playerInv.player.level().getBlockState(pos)));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < containerSlotCount) {
            if (!moveItemStackTo(stack, containerSlotCount, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, containerSlotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.stillValid(player);
    }

    private static class LootContainerSlotContainer implements net.minecraft.world.Container {
        private final LootContainerBlockEntity be;

        LootContainerSlotContainer(LootContainerBlockEntity be) {
            this.be = be;
        }

        @Override public int getContainerSize() { return be.getContainerSize(); }
        @Override public boolean isEmpty() {
            for (int i = 0; i < be.getContainerSize(); i++) {
                if (!be.getItem(i).isEmpty()) return false;
            }
            return true;
        }
        @Override public ItemStack getItem(int slot) { return be.getItem(slot); }
        @Override public ItemStack removeItem(int slot, int count) { return be.removeItem(slot, count); }
        @Override public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = be.getItem(slot);
            be.setItem(slot, ItemStack.EMPTY);
            return stack;
        }
        @Override public void setItem(int slot, ItemStack stack) { be.setItem(slot, stack); }
        @Override public void setChanged() { be.setChanged(); }
        @Override public boolean stillValid(Player player) { return be.stillValid(player); }
        @Override public void clearContent() {
            for (int i = 0; i < be.getContainerSize(); i++) {
                be.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
