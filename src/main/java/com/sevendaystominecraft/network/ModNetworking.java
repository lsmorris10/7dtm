package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.client.BloodMoonClientState;
import com.sevendaystominecraft.client.ChunkHeatClientState;
import com.sevendaystominecraft.client.NearbyPlayersClientState;
import com.sevendaystominecraft.client.QuestClientState;
import com.sevendaystominecraft.client.TerritoryClientState;
import com.sevendaystominecraft.client.TraderClientState;
import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
import com.sevendaystominecraft.perk.Attribute;
import com.sevendaystominecraft.quest.QuestActionHandler;
import com.sevendaystominecraft.trader.TraderMenu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SevenDaysToMinecraft.MOD_ID)
                .versioned("1");

        registrar.playToClient(
                SyncPlayerStatsPayload.TYPE,
                SyncPlayerStatsPayload.STREAM_CODEC,
                ModNetworking::handleStatsSync
        );

        registrar.playToClient(
                BloodMoonSyncPayload.TYPE,
                BloodMoonSyncPayload.STREAM_CODEC,
                ModNetworking::handleBloodMoonSync
        );

        registrar.playToClient(
                SyncNearbyPlayersPayload.TYPE,
                SyncNearbyPlayersPayload.STREAM_CODEC,
                ModNetworking::handleNearbyPlayersSync
        );

        registrar.playToClient(
                SyncChunkHeatPayload.TYPE,
                SyncChunkHeatPayload.STREAM_CODEC,
                ModNetworking::handleChunkHeatSync
        );

        registrar.playToClient(
                SyncLootStagePayload.TYPE,
                SyncLootStagePayload.STREAM_CODEC,
                ModNetworking::handleLootStageSync
        );

        registrar.playToClient(
                SyncTerritoryPayload.TYPE,
                SyncTerritoryPayload.STREAM_CODEC,
                ModNetworking::handleTerritorySync
        );

        registrar.playToClient(
                SyncTraderPayload.TYPE,
                SyncTraderPayload.STREAM_CODEC,
                ModNetworking::handleTraderSync
        );

        registrar.playToServer(
                FireWeaponPayload.TYPE,
                FireWeaponPayload.STREAM_CODEC,
                ModNetworking::handleFireWeapon
        );

        registrar.playToServer(
                TraderActionPayload.TYPE,
                TraderActionPayload.STREAM_CODEC,
                ModNetworking::handleTraderAction
        );

        registrar.playToClient(
                SyncQuestPayload.TYPE,
                SyncQuestPayload.STREAM_CODEC,
                ModNetworking::handleQuestSync
        );

        registrar.playToClient(
                SyncTraderQuestsPayload.TYPE,
                SyncTraderQuestsPayload.STREAM_CODEC,
                ModNetworking::handleTraderQuestsSync
        );

        registrar.playToServer(
                QuestActionPayload.TYPE,
                QuestActionPayload.STREAM_CODEC,
                ModNetworking::handleQuestAction
        );

        registrar.playToServer(
                WorkstationRecipeSelectPayload.TYPE,
                WorkstationRecipeSelectPayload.STREAM_CODEC,
                ModNetworking::handleWorkstationRecipeSelect
        );

        registrar.playToServer(
                WorkstationRecipeRequestPayload.TYPE,
                WorkstationRecipeRequestPayload.STREAM_CODEC,
                ModNetworking::handleWorkstationRecipeRequest
        );

        registrar.playToClient(
                WorkstationRecipeListPayload.TYPE,
                WorkstationRecipeListPayload.STREAM_CODEC,
                ModNetworking::handleWorkstationRecipeList
        );

        SevenDaysToMinecraft.LOGGER.debug("BZHS: Registered network payloads");
    }

    private static void handleFireWeapon(FireWeaponPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;

            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (held.getItem() instanceof GeoRangedWeaponItem weapon) {
                weapon.fireWeapon(player.level(), player, payload.aiming());
            }
        });
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

    private static void handleLootStageSync(SyncLootStagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            SevenDaysToMinecraft.LOGGER.debug("BZHS: Received loot stage sync: {}", payload.lootStage());
        });
    }

    private static void handleTerritorySync(SyncTerritoryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            TerritoryClientState.update(payload.territories());
        });
    }

    private static void handleTraderSync(SyncTraderPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            TraderClientState.update(payload.traders());
        });
    }

    private static void handleTraderAction(TraderActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;
            if (!(player.containerMenu instanceof TraderMenu traderMenu)) return;
            if (traderMenu.getTraderId() != payload.traderId()) return;
            if (!traderMenu.stillValid(player)) return;

            if (payload.isBuy()) {
                traderMenu.tryBuy(player, payload.actionIndex());
            } else {
                traderMenu.trySellSlots(player);
            }
        });
    }

    private static void handleQuestSync(SyncQuestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            QuestClientState.updateActiveQuests(payload.quests(), payload.trackedQuestId());
        });
    }

    private static void handleTraderQuestsSync(SyncTraderQuestsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            QuestClientState.updateTraderQuests(payload.traderId(), payload.quests());
        });
    }

    private static void handleQuestAction(QuestActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            QuestActionHandler.handleAction(serverPlayer, payload);
        });
    }

    private static void handleWorkstationRecipeRequest(WorkstationRecipeRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            if (!(player.containerMenu instanceof com.sevendaystominecraft.block.workstation.WorkstationMenu wsMenu)) return;
            if (wsMenu.getBlockEntity() == null) return;
            if (player.getServer() == null) return;

            com.sevendaystominecraft.block.workstation.WorkstationType type = wsMenu.getBlockEntity().getWorkstationType();
            var recipeManager = player.getServer().getRecipeManager();

            java.util.List<WorkstationRecipeListPayload.Entry> entries = new java.util.ArrayList<>();
            for (var holder : recipeManager.getRecipes()) {
                if (holder.value().getType().equals(type.getRecipeType())) {
                    if (holder.value() instanceof com.sevendaystominecraft.block.workstation.recipe.WorkstationCraftingRecipe recipe) {
                        entries.add(new WorkstationRecipeListPayload.Entry(
                                holder.id().location(),
                                recipe.getResult().copy(),
                                recipe.getProcessingTicks(),
                                recipe.getIngredients()
                        ));
                    }
                }
            }
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new WorkstationRecipeListPayload(entries));
        });
    }

    private static void handleWorkstationRecipeList(WorkstationRecipeListPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;
            if (!(player.containerMenu instanceof com.sevendaystominecraft.block.workstation.WorkstationMenu wsMenu)) return;

            wsMenu.acceptRecipeList(payload.entries());
        });
    }

    private static void handleWorkstationRecipeSelect(WorkstationRecipeSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            if (!(player.containerMenu instanceof com.sevendaystominecraft.block.workstation.WorkstationMenu wsMenu)) return;
            if (!wsMenu.stillValid(player)) return;

            wsMenu.handleRecipeSelect(serverPlayer, payload.recipeId());
        });
    }

    private static void handleStatsSync(SyncPlayerStatsPayload payload, IPayloadContext context) {
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

            for (String id : SevenDaysPlayerStats.KNOWN_DEBUFF_IDS) {
                stats.removeDebuff(id);
            }
            for (var entry : payload.debuffs().entrySet()) {
                stats.addDebuff(entry.getKey(), entry.getValue());
            }

            stats.setXp(payload.xp());
            stats.setLevel(payload.level());
            stats.setPerkPoints(payload.perkPoints());
            stats.setAttributePoints(payload.attributePoints());

            Attribute[] attrs = Attribute.values();
            for (int i = 0; i < payload.attributeLevels().length && i < attrs.length; i++) {
                stats.setAttributeLevel(attrs[i], payload.attributeLevels()[i]);
            }

            for (String key : new java.util.ArrayList<>(stats.getActivePerks().keySet())) {
                stats.setPerkRank(key, 0);
            }
            for (var entry : payload.activePerks().entrySet()) {
                stats.setPerkRank(entry.getKey(), entry.getValue());
            }

            stats.setUnkillableCooldownEnd(payload.unkillableCooldownEnd());
        });
    }
}
