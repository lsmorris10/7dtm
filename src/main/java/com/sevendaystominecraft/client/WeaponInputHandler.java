package com.sevendaystominecraft.client;

import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
import com.sevendaystominecraft.network.FireWeaponPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class WeaponInputHandler {

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Player player = mc.player;
        ItemStack held = player.getMainHandItem();

        if (!(held.getItem() instanceof GeoRangedWeaponItem)) return;

        if (event.isAttack()) {
            event.setSwingHand(false);
            event.setCanceled(true);

            PacketDistributor.sendToServer(new FireWeaponPayload(ADSHandler.isAiming()));
        }

        if (event.isUseItem()) {
            event.setSwingHand(false);
            event.setCanceled(true);

            ADSHandler.setAiming(true);
        }
    }
}
