package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.farming.DewCollectorScreen;
import com.sevendaystominecraft.block.loot.LootContainerScreen;
import com.sevendaystominecraft.block.power.BatteryScreen;
import com.sevendaystominecraft.block.power.GeneratorScreen;
import com.sevendaystominecraft.client.gui.DeadBodyScreen;
import com.sevendaystominecraft.menu.ModMenuTypes;
import com.sevendaystominecraft.trader.TraderScreen;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ModScreens {

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LOOT_CONTAINER_MENU.get(), LootContainerScreen::new);
        event.register(ModMenuTypes.TRADER_MENU.get(), TraderScreen::new);
        event.register(ModMenuTypes.DEW_COLLECTOR_MENU.get(), DewCollectorScreen::new);
        event.register(ModMenuTypes.GENERATOR_MENU.get(), GeneratorScreen::new);
        event.register(ModMenuTypes.BATTERY_MENU.get(), BatteryScreen::new);
        event.register(ModMenuTypes.DEAD_BODY_MENU.get(), DeadBodyScreen::new);
        SevenDaysToMinecraft.LOGGER.info("BZHS: Registered loot container, trader, dew collector, generator, battery, and dead body screens");
    }
}
