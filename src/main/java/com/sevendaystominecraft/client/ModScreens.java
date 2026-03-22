package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.loot.LootContainerScreen;
import com.sevendaystominecraft.block.workstation.WorkstationScreen;
import com.sevendaystominecraft.menu.ModMenuTypes;
import com.sevendaystominecraft.trader.TraderScreen;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ModScreens {

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.WORKSTATION_MENU.get(), WorkstationScreen::new);
        event.register(ModMenuTypes.LOOT_CONTAINER_MENU.get(), LootContainerScreen::new);
        event.register(ModMenuTypes.TRADER_MENU.get(), TraderScreen::new);
        SevenDaysToMinecraft.LOGGER.info("BZHS: Registered workstation, loot container, and trader screens");
    }
}
