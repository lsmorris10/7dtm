package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, value = Dist.CLIENT)
public class VanillaHudHider {

    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (VanillaGuiLayers.FOOD_LEVEL.equals(event.getName())) {
            event.setCanceled(true);
        }
    }
}
