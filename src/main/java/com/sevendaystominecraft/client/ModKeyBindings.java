package com.sevendaystominecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class ModKeyBindings {

    private static final String CATEGORY = "key.categories.sevendaystominecraft";

    public static final KeyMapping OPEN_MAP = new KeyMapping(
            "key.sevendaystominecraft.open_map",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_M,
            CATEGORY
    );

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MAP);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (OPEN_MAP.consumeClick()) {
            if (mc.screen instanceof BigMapScreen) {
                mc.setScreen(null);
            } else if (mc.screen == null || mc.screen instanceof InventoryScreen) {
                mc.setScreen(new BigMapScreen());
            }
        }
    }
}
