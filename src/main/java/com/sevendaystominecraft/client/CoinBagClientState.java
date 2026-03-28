package com.sevendaystominecraft.client;

import net.minecraft.world.item.ItemStack;

public final class CoinBagClientState {

    private static ItemStack equippedCoinBag = ItemStack.EMPTY;

    private CoinBagClientState() {}

    public static void update(ItemStack coinBag) {
        equippedCoinBag = coinBag != null ? coinBag : ItemStack.EMPTY;
    }

    public static ItemStack getEquippedCoinBag() {
        return equippedCoinBag;
    }

    public static void reset() {
        equippedCoinBag = ItemStack.EMPTY;
    }
}
