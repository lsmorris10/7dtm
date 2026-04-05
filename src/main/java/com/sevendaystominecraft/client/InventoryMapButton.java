package com.sevendaystominecraft.client;

import com.sevendaystominecraft.client.gui.BzhsInventoryScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class InventoryMapButton {

    private static final int BUTTON_WIDTH = 50;
    private static final int BUTTON_HEIGHT = 20;

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)
                && !(event.getScreen() instanceof BzhsInventoryScreen)) return;

        int x = event.getScreen().width / 2 + 60;
        int y = event.getScreen().height / 2 - 100;

        Button mapButton = Button.builder(Component.translatable("gui.sevendaystominecraft.map_tab"), btn -> {
            Minecraft.getInstance().setScreen(new BigMapScreen());
        }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();

        event.addListener(mapButton);
    }
}
