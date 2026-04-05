package com.sevendaystominecraft.menu;

import com.sevendaystominecraft.entity.DeadBodyEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

public class DeadBodyMenu extends AbstractContainerMenu {

    private static final int BODY_SLOT_COUNT = DeadBodyEntity.SLOT_COUNT;
    private static final int ARMOR_SLOTS = 4;
    private static final int MAIN_INV_SLOTS = 27;
    private static final int HOTBAR_SLOTS = 9;
    private static final int OFFHAND_SLOTS = 1;

    private final DeadBodyEntity deadBody;
    private final SimpleContainer bodyInventory;

    public DeadBodyMenu(int containerId, Inventory playerInv, DeadBodyEntity deadBody) {
        super(ModMenuTypes.DEAD_BODY_MENU.get(), containerId);
        this.deadBody = deadBody;
        this.bodyInventory = deadBody != null ? deadBody.getInventory() : new SimpleContainer(BODY_SLOT_COUNT);

        int yOffset = 18;

        for (int i = 0; i < ARMOR_SLOTS; i++) {
            addSlot(new Slot(bodyInventory, i, 8 + i * 18, yOffset));
        }
        addSlot(new Slot(bodyInventory, ARMOR_SLOTS + MAIN_INV_SLOTS + HOTBAR_SLOTS, 8 + 5 * 18, yOffset));
        addSlot(new Slot(bodyInventory, ARMOR_SLOTS + MAIN_INV_SLOTS + HOTBAR_SLOTS + OFFHAND_SLOTS, 8 + 7 * 18, yOffset));

        yOffset += 24;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int idx = ARMOR_SLOTS + row * 9 + col;
                addSlot(new Slot(bodyInventory, idx, 8 + col * 18, yOffset + row * 18));
            }
        }

        yOffset += 3 * 18 + 4;

        for (int col = 0; col < 9; col++) {
            int idx = ARMOR_SLOTS + MAIN_INV_SLOTS + col;
            addSlot(new Slot(bodyInventory, idx, 8 + col * 18, yOffset));
        }

        yOffset += 18 + 14;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, yOffset + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, yOffset + 58));
        }
    }

    public static DeadBodyMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        Entity entity = playerInv.player.level().getEntity(entityId);
        DeadBodyEntity body = entity instanceof DeadBodyEntity db ? db : null;
        return new DeadBodyMenu(containerId, playerInv, body);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < BODY_SLOT_COUNT) {
            if (!moveItemStackTo(stack, BODY_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, BODY_SLOT_COUNT, false)) {
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
        return deadBody != null && deadBody.isAlive() && player.distanceToSqr(deadBody) < 64.0;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (deadBody != null) {
            deadBody.onMenuClosed();
        }
    }
}
