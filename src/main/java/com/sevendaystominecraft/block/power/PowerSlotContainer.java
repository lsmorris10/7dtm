package com.sevendaystominecraft.block.power;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PowerSlotContainer implements Container {
    private final GeneratorBankBlockEntity be;

    public PowerSlotContainer(GeneratorBankBlockEntity be) {
        this.be = be;
    }

    @Override
    public int getContainerSize() {
        return be.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < be.getContainerSize(); i++) {
            if (!be.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return be.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return be.removeItem(slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = be.getItem(slot);
        be.setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        be.setItem(slot, stack);
    }

    @Override
    public void setChanged() {
        be.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return be.stillValid(player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < be.getContainerSize(); i++) {
            be.setItem(i, ItemStack.EMPTY);
        }
    }
}
