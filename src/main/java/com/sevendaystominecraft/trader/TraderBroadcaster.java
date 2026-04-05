package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.TraderConfig;
import com.sevendaystominecraft.network.SyncTraderPayload;
import com.sevendaystominecraft.network.SyncTraderPayload.TraderEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class TraderBroadcaster {

    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 60;
    private static int restockTickCounter = 0;
    private static final int RESTOCK_CHECK_INTERVAL = 1200;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel().dimension() != Level.OVERWORLD) return;

        ServerLevel level = (ServerLevel) event.getLevel();

        restockTickCounter++;
        if (restockTickCounter >= RESTOCK_CHECK_INTERVAL) {
            restockTickCounter = 0;
            checkRestock(level);
        }

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        TraderData data = TraderData.getOrCreate(level);

        double syncRange = TraderConfig.INSTANCE.syncRangeBlocks.get();

        for (ServerPlayer player : level.players()) {
            BlockPos playerPos = player.blockPosition();
            List<TraderRecord> nearby = data.getNearby(playerPos, syncRange);

            List<TraderEntry> entries = new ArrayList<>(nearby.size());
            for (TraderRecord record : nearby) {
                entries.add(new TraderEntry(
                        record.getId(),
                        record.getOrigin().getX(),
                        record.getOrigin().getY(),
                        record.getOrigin().getZ(),
                        record.getTier(),
                        record.getName()
                ));
            }

            PacketDistributor.sendToPlayer(player, new SyncTraderPayload(entries));
        }
    }

    private static void checkRestock(ServerLevel level) {
        long currentDay = level.getDayTime() / 24000L;
        int restockInterval = TraderConfig.INSTANCE.restockIntervalDays.get();
        TraderData data = TraderData.getOrCreate(level);
        boolean changed = false;

        for (TraderRecord record : data.getAllTraders()) {
            long lastRestock = record.getLastRestockDay();
            if (lastRestock < 0 || (currentDay - lastRestock) >= restockInterval) {
                record.restock();
                record.setLastRestockDay(currentDay);
                changed = true;
                SevenDaysToMinecraft.LOGGER.debug("[BZHS Trader] Restocked {} (ID {})", record.getName(), record.getId());
            }
        }

        if (changed) {
            data.markDirtyRecord();
        }
    }
}
