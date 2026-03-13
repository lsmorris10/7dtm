package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.loot.LootContainerScreen;
import com.sevendaystominecraft.block.workstation.WorkstationScreen;
import com.sevendaystominecraft.menu.ModMenuTypes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModScreens {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.WORKSTATION_MENU.get(), WorkstationScreen::new);
        event.register(ModMenuTypes.LOOT_CONTAINER_MENU.get(), LootContainerScreen::new);
        SevenDaysToMinecraft.LOGGER.info("7DTM: Registered workstation and loot container screens");
    }
}
