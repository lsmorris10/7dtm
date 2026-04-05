package com.sevendaystominecraft.client;

import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@OnlyIn(Dist.CLIENT)
public class ADSHandler {

    private static boolean aiming = false;

    public static boolean isAiming() {
        return aiming;
    }

    public static void setAiming(boolean value) {
        aiming = value;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;

        if (!aiming) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof GeoRangedWeaponItem)) {
            aiming = false;
            return;
        }

        if (!mc.options.keyUse.isDown()) {
            aiming = false;
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (!aiming) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (held.getItem() instanceof GeoRangedWeaponItem) {
            event.setFOV(event.getFOV() * GeoRangedWeaponItem.ADS_FOV_MULTIPLIER);
        }
    }
}
