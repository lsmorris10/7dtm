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
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class WeaponInputHandler {

    private static boolean attackHeld = false;

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Player player = mc.player;
        ItemStack held = player.getMainHandItem();

        if (!(held.getItem() instanceof GeoRangedWeaponItem weapon)) return;

        if (event.isAttack()) {
            event.setSwingHand(false);
            event.setCanceled(true);

            if (weapon.isFullAuto()) {
                attackHeld = true;
            }

            PacketDistributor.sendToServer(new FireWeaponPayload(ADSHandler.isAiming()));
        }

        if (event.isUseItem()) {
            event.setSwingHand(false);
            event.setCanceled(true);

            ADSHandler.setAiming(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) {
            attackHeld = false;
            return;
        }

        if (!attackHeld) return;

        if (!mc.options.keyAttack.isDown()) {
            attackHeld = false;
            return;
        }

        Player player = mc.player;
        ItemStack held = player.getMainHandItem();

        if (!(held.getItem() instanceof GeoRangedWeaponItem weapon) || !weapon.isFullAuto()) {
            attackHeld = false;
            return;
        }

        if (GeoRangedWeaponItem.getCurrentAmmo(held) == 0) {
            attackHeld = false;
            return;
        }

        if (!player.getCooldowns().isOnCooldown(held)) {
            PacketDistributor.sendToServer(new FireWeaponPayload(ADSHandler.isAiming()));
        }
    }
}
