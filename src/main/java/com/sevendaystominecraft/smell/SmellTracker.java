package com.sevendaystominecraft.smell;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SmellTracker {

    private static final int UPDATE_INTERVAL_TICKS = 100;
    private static final int RAW_MEAT_SMELL_PER_STACK = 10;
    private static final int BLEEDING_SMELL = 20;
    private static final int RECENTLY_COOKED_SMELL = 15;
    private static final long RECENTLY_COOKED_DURATION_TICKS = 1200;

    private static final Map<UUID, Integer> playerSmellValues = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastCookedTimestamps = new ConcurrentHashMap<>();

    public static void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % UPDATE_INTERVAL_TICKS != 0) return;

        int smell = 0;

        int rawMeatStacks = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isRawMeat(stack)) {
                rawMeatStacks++;
            }
        }
        smell += rawMeatStacks * RAW_MEAT_SMELL_PER_STACK;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        if (stats.hasDebuff(SevenDaysPlayerStats.DEBUFF_BLEEDING)) {
            smell += BLEEDING_SMELL;
        }

        Long lastCooked = lastCookedTimestamps.get(player.getUUID());
        if (lastCooked != null) {
            long elapsed = player.level().getGameTime() - lastCooked;
            if (elapsed < RECENTLY_COOKED_DURATION_TICKS) {
                smell += RECENTLY_COOKED_SMELL;
            } else {
                lastCookedTimestamps.remove(player.getUUID());
            }
        }

        if (smell > 0) {
            playerSmellValues.put(player.getUUID(), smell);
        } else {
            playerSmellValues.remove(player.getUUID());
        }
    }

    public static int getSmellValue(UUID playerUUID) {
        return playerSmellValues.getOrDefault(playerUUID, 0);
    }

    public static void markRecentlyCookedFor(ServerPlayer player) {
        lastCookedTimestamps.put(player.getUUID(), player.level().getGameTime());
    }

    public static void removePlayer(UUID playerUUID) {
        playerSmellValues.remove(playerUUID);
        lastCookedTimestamps.remove(playerUUID);
    }

    private static boolean isRawMeat(ItemStack stack) {
        return stack.is(Items.BEEF)
                || stack.is(Items.PORKCHOP)
                || stack.is(Items.CHICKEN)
                || stack.is(Items.RABBIT)
                || stack.is(Items.MUTTON)
                || stack.is(Items.COD)
                || stack.is(Items.SALMON);
    }
}
