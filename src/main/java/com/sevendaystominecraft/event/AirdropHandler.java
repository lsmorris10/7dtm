package com.sevendaystominecraft.event;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.worlddata.AirdropManager;
import com.sevendaystominecraft.entity.ModEntities;
import com.sevendaystominecraft.entity.AirdropPlaneEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class AirdropHandler {

    private static int tickCounter = 0;
    private static final long BASE_INTERVAL = 72000L; // 1 hour

    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide() || event.getLevel().dimension() != net.minecraft.world.level.Level.OVERWORLD) return;

        tickCounter++;
        if (tickCounter < 100) return; // Check every 5 seconds
        tickCounter = 0;

        ServerLevel level = (ServerLevel) event.getLevel();
        AirdropManager manager = AirdropManager.get(level);

        if (!manager.areAirdropsEnabled()) return;

        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        int playerCount = players.size();
        long currentInterval = (long) (BASE_INTERVAL / (1.0 + (playerCount - 1) * 0.25));

        manager.incrementTicks(100);

        if (manager.getTicksSinceLastAirdrop() >= currentInterval) {
            manager.resetTimer();
            triggerAirdrop(level, players);
        }
    }

    private static void triggerAirdrop(ServerLevel level, List<ServerPlayer> players) {
        Random random = new Random();
        ServerPlayer targetPlayer = players.get(random.nextInt(players.size()));

        int offsetX = (random.nextInt(400) - 200);
        int offsetZ = (random.nextInt(400) - 200);
        
        double targetX = targetPlayer.getX() + offsetX;
        double targetZ = targetPlayer.getZ() + offsetZ;

        for (ServerPlayer player : players) {
            player.sendSystemMessage(Component.literal("§e✈ An Airdrop is inbound near X: " + (int)targetX + ", Z: " + (int)targetZ + "!"));
        }

        AirdropPlaneEntity plane = new AirdropPlaneEntity(ModEntities.AIRDROP_PLANE.get(), level);
        plane.setTargetPosition(targetX, targetZ);
        
        // Spawn it 1000 blocks away to fly in
        double spawnX = targetX + (random.nextBoolean() ? 1000 : -1000);
        double spawnZ = targetZ + (random.nextBoolean() ? 1000 : -1000);
        plane.setPos(spawnX, 200.0, spawnZ);
        
        level.addFreshEntity(plane);
        
        SevenDaysToMinecraft.LOGGER.info("[BZHS] Airdrop triggered for target {}, {}!", targetX, targetZ);
    }
}
