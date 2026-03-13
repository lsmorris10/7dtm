package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.client.BloodMoonClientState;
import com.sevendaystominecraft.client.ChunkHeatClientState;
import com.sevendaystominecraft.client.NearbyPlayersClientState;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers and handles custom network payloads for the mod.
 *
 * <h3>Current payloads:</h3>
 * <ul>
 *   <li>{@link SyncPlayerStatsPayload} — server → client stats sync (fallback)</li>
 * </ul>
 *
 * <h3>Note on dual sync:</h3>
 * The primary sync mechanism is the {@link com.sevendaystominecraft.capability.ModAttachments.PlayerStatsSyncHandler}
 * built into the attachment system. This manual payload exists as a fallback
 * for cases where explicit sync is needed (e.g., forced full refresh).
 *
 * Registered on the <b>mod event bus</b> via {@code @EventBusSubscriber(bus = Bus.MOD)}.
 */
@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    /**
     * Register all custom payloads with NeoForge networking.
     */
    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SevenDaysToMinecraft.MOD_ID)
                .versioned("1");

        // Server → Client: player stats sync
        registrar.playToClient(
                SyncPlayerStatsPayload.TYPE,
                SyncPlayerStatsPayload.STREAM_CODEC,
                ModNetworking::handleStatsSync
        );

        // Server → Client: blood moon state sync
        registrar.playToClient(
                BloodMoonSyncPayload.TYPE,
                BloodMoonSyncPayload.STREAM_CODEC,
                ModNetworking::handleBloodMoonSync
        );

        // Server → Client: nearby players sync
        registrar.playToClient(
                SyncNearbyPlayersPayload.TYPE,
                SyncNearbyPlayersPayload.STREAM_CODEC,
                ModNetworking::handleNearbyPlayersSync
        );

        // Server → Client: chunk heat sync
        registrar.playToClient(
                SyncChunkHeatPayload.TYPE,
                SyncChunkHeatPayload.STREAM_CODEC,
                ModNetworking::handleChunkHeatSync
        );

        SevenDaysToMinecraft.LOGGER.debug("7DTM: Registered network payloads");
    }

    private static void handleBloodMoonSync(BloodMoonSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BloodMoonClientState.update(
                    payload.active(),
                    payload.currentWave(),
                    payload.totalWaves(),
                    payload.dayNumber()
            );
        });
    }

    private static void handleNearbyPlayersSync(SyncNearbyPlayersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            NearbyPlayersClientState.update(payload.players());
        });
    }

    private static void handleChunkHeatSync(SyncChunkHeatPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ChunkHeatClientState.update(payload.chunkHeat());
        });
    }

    /**
     * Client-side handler for receiving player stats sync.
     *
     * Applies received stats values to the local player's data attachment.
     * This runs on the main client thread (default behavior in NeoForge 1.21).
     */
    private static void handleStatsSync(SyncPlayerStatsPayload payload, IPayloadContext context) {
        // This runs on the main thread by default in NeoForge 1.21
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;

            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            stats.setFood(payload.food());
            stats.setMaxFood(payload.maxFood());
            stats.setWater(payload.water());
            stats.setMaxWater(payload.maxWater());
            stats.setStamina(payload.stamina());
            stats.setMaxStamina(payload.maxStamina());
            stats.setStaminaExhausted(payload.staminaExhausted());
            stats.setCoreTemperature(payload.coreTemp());
            
            if (payload.staminaExhausted() && player.isSprinting()) {
                player.setSprinting(false);
            }

            // Clear and repopulate debuffs
            for (String id : SevenDaysPlayerStats.KNOWN_DEBUFF_IDS) {
                stats.removeDebuff(id);
            }
            for (var entry : payload.debuffs().entrySet()) {
                stats.addDebuff(entry.getKey(), entry.getValue());
            }
        });
    }
}
