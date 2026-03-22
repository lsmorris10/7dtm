package com.sevendaystominecraft.block.farming;

import com.sevendaystominecraft.menu.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DewCollectorMenu extends AbstractContainerMenu {

    private final DewCollectorBlockEntity blockEntity;
    private static final int CONTAINER_SLOTS = 4;

    public DewCollectorMenu(int containerId, Inventory playerInv, DewCollectorBlockEntity blockEntity) {
        super(ModMenuTypes.DEW_COLLECTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        DewCollectorSlotContainer container = new DewCollectorSlotContainer(blockEntity);

        for (int i = 0; i < CONTAINER_SLOTS; i++) {
            addSlot(new Slot(container, i, 53 + i * 18, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        int playerInvY = 51;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public static DewCollectorMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (be instanceof DewCollectorBlockEntity dewBE) {
            return new DewCollectorMenu(containerId, playerInv, dewBE);
        }
        return new DewCollectorMenu(containerId, playerInv, new DewCollectorBlockEntity(pos,
                playerInv.player.level().getBlockState(pos)));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < CONTAINER_SLOTS) {
            if (!moveItemStackTo(stack, CONTAINER_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
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

    private static class DewCollectorSlotContainer implements net.minecraft.world.Container {
        private final DewCollectorBlockEntity be;

        DewCollectorSlotContainer(DewCollectorBlockEntity be) {
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
