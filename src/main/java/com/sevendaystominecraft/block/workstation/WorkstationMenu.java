package com.sevendaystominecraft.block.workstation;

import com.sevendaystominecraft.menu.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WorkstationMenu extends AbstractContainerMenu {

    private final WorkstationBlockEntity blockEntity;
    private final ContainerData data;
    private final int playerInvY;

    public WorkstationMenu(int containerId, Inventory playerInv, WorkstationBlockEntity blockEntity) {
        super(ModMenuTypes.WORKSTATION_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getBurnTime();
                    case 1 -> blockEntity.getBurnTimeTotal();
                    case 2 -> blockEntity.getCraftProgress();
                    case 3 -> blockEntity.getCraftTimeTotal();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {}

            @Override
            public int getCount() { return 4; }
        };
        addDataSlots(this.data);

        WorkstationType type = blockEntity.getWorkstationType();
        WorkstationSlotContainer container = new WorkstationSlotContainer(blockEntity);

        int slotIndex = 0;
        for (int i = 0; i < type.getInputSlots(); i++) {
            int col = i % 3;
            int row = i / 3;
            addSlot(new WorkstationSlot(container, slotIndex++, 26 + col * 18, 17 + row * 18, true));
        }

        int outputStartX = 116;
        for (int i = 0; i < type.getOutputSlots(); i++) {
            int col = i % 3;
            int row = i / 3;
            addSlot(new WorkstationSlot(container, slotIndex++, outputStartX + col * 18, 17 + row * 18, false));
        }

        int inputRows = (int) Math.ceil((double) type.getInputSlots() / 3.0);
        int outputRows = (int) Math.ceil((double) type.getOutputSlots() / 3.0);
        int maxSlotRows = Math.max(inputRows, outputRows);
        int workstationBottom = 17 + maxSlotRows * 18;

        if (type.usesFuel()) {
            int fuelY = workstationBottom + 4;
            for (int i = 0; i < type.getFuelSlots(); i++) {
                addSlot(new WorkstationSlot(container, slotIndex++, 26 + i * 18, fuelY, true));
            }
            workstationBottom = fuelY + 18;
        }

        this.playerInvY = workstationBottom + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public int getPlayerInvY() { return playerInvY; }

    public static WorkstationMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (be instanceof WorkstationBlockEntity wbe) {
            return new WorkstationMenu(containerId, playerInv, wbe);
        }
        return new WorkstationMenu(containerId, playerInv, new WorkstationBlockEntity(pos,
                playerInv.player.level().getBlockState(pos)));
    }

    public WorkstationBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ContainerData getData() {
        return data;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int stationSlots = blockEntity != null ? blockEntity.getWorkstationType().getTotalSlots() : 0;

        if (index < stationSlots) {
            if (!moveItemStackTo(stack, stationSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, stationSlots, false)) {
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

    private static class WorkstationSlot extends Slot {
        private final boolean isInput;

        public WorkstationSlot(WorkstationSlotContainer container, int index, int x, int y, boolean isInput) {
            super(container, index, x, y);
            this.isInput = isInput;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isInput;
        }
    }

    static class WorkstationSlotContainer implements net.minecraft.world.Container {
        private final WorkstationBlockEntity be;

        WorkstationSlotContainer(WorkstationBlockEntity be) {
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
