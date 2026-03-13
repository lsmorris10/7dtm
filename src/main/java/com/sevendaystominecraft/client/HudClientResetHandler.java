package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, value = Dist.CLIENT)
public class HudClientResetHandler {

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        NearbyPlayersClientState.reset();
        ChunkHeatClientState.reset();
    }
}
