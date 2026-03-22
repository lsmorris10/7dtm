package com.sevendaystominecraft.capability;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class WaterBottleConversionHandler {

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        if (!isVanillaWaterBottle(stack)) return;

        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        stats.setWater(stats.getWater() + 10f);
        stats.addDebuff(SevenDaysPlayerStats.DEBUFF_DYSENTERY, 72000);

        if (player instanceof ServerPlayer serverPlayer) {
            PlayerStatsHandler.sendStatsToClient(serverPlayer, stats);
        }
    }

    public static boolean isVanillaWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        var potionContents = stack.get(DataComponents.POTION_CONTENTS);
        return potionContents != null
                && potionContents.potion().isPresent()
                && potionContents.potion().get().is(Potions.WATER);
    }
}
