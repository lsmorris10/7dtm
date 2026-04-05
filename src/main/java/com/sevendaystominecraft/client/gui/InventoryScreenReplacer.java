package com.sevendaystominecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class InventoryScreenReplacer {

    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getScreen() instanceof InventoryScreen)) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (player.isCreative()) return;

        BzhsInventoryScreen customScreen = new BzhsInventoryScreen(
                player.inventoryMenu,
                player.getInventory(),
                Component.translatable("container.crafting")
        );
        event.setNewScreen(customScreen);
    }
}
