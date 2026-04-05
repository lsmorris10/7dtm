package com.sevendaystominecraft.loot;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.network.SyncLootStagePayload;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class LootStageHandler {

    private static final int SYNC_INTERVAL = 200;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.tickCount % SYNC_INTERVAL != 0) return;

        int lootStage = LootStageCalculator.calculate(serverPlayer);
        PacketDistributor.sendToPlayer(serverPlayer, new SyncLootStagePayload(lootStage));
    }
}
