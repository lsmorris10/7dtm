package com.sevendaystominecraft.quest;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.network.SyncProgressionPayload;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Server-side handler that detects when progression nodes should be completed.
 * Checks conditions each player tick (throttled) and grants rewards.
 */
@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class ProgressionTracker {

    // Only check every 40 ticks (~2 seconds) to reduce overhead
    private static final int CHECK_INTERVAL = 40;

    /**
     * Maps node IDs to detection lambdas. Each function returns true if the
     * condition for that node is currently satisfied.
     */
    private static final Map<String, NodeCondition> CONDITIONS = new LinkedHashMap<>();

    @FunctionalInterface
    interface NodeCondition {
        boolean isMet(ServerPlayer player, SevenDaysPlayerStats stats);
    }

    static {
        // ── Stage 1: Scavenger ──────────────────────────────────────────
        CONDITIONS.put("s1_gather_wood", (p, s) ->
                countItemInInventory(p, "minecraft:oak_planks") >= 16
                        || countItemInInventory(p, "minecraft:spruce_planks") >= 16
                        || countItemInInventory(p, "minecraft:birch_planks") >= 16
                        || countItemInInventory(p, "minecraft:jungle_planks") >= 16
                        || countItemInInventory(p, "minecraft:acacia_planks") >= 16
                        || countItemInInventory(p, "minecraft:dark_oak_planks") >= 16
                        || countItemInInventory(p, "minecraft:cherry_planks") >= 16
                        || countItemInInventory(p, "minecraft:mangrove_planks") >= 16
                        || countItemInInventory(p, "minecraft:bamboo_planks") >= 16
                        || countItemInInventory(p, "minecraft:crimson_planks") >= 16
                        || countItemInInventory(p, "minecraft:warped_planks") >= 16
                        || countItemInInventory(p, "minecraft:pale_oak_planks") >= 16);

        CONDITIONS.put("s1_gather_stone", (p, s) ->
                countItemInInventory(p, "minecraft:cobblestone") >= 8);

        CONDITIONS.put("s1_get_water", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:murky_water")
                        || hasItemInInventory(p, "sevendaystominecraft:boiled_water"));

        CONDITIONS.put("s1_hunt_meat", (p, s) ->
                hasItemInInventory(p, "minecraft:beef")
                        || hasItemInInventory(p, "minecraft:porkchop")
                        || hasItemInInventory(p, "minecraft:mutton")
                        || hasItemInInventory(p, "minecraft:chicken")
                        || hasItemInInventory(p, "minecraft:rabbit")
                        || hasItemInInventory(p, "minecraft:cooked_rabbit"));

        CONDITIONS.put("s1_stone_tools", (p, s) ->
                hasItemInInventory(p, "minecraft:stone_pickaxe"));

        CONDITIONS.put("s1_stone_club", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:stone_club"));

        // ── Stage 2: Survivor ───────────────────────────────────────────
        CONDITIONS.put("s2_campfire", (p, s) ->
                // Check if player has placed a campfire (we track this by looking for the stat)
                p.getStats().getValue(net.minecraft.stats.Stats.ITEM_USED,
                        net.minecraft.world.item.Items.CAMPFIRE) > 0);

        CONDITIONS.put("s2_boil_water", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:boiled_water"));

        CONDITIONS.put("s2_cook_meat", (p, s) ->
                hasItemInInventory(p, "minecraft:cooked_rabbit")
                        || hasItemInInventory(p, "minecraft:cooked_beef")
                        || hasItemInInventory(p, "minecraft:cooked_porkchop")
                        || hasItemInInventory(p, "minecraft:cooked_mutton")
                        || hasItemInInventory(p, "minecraft:cooked_chicken"));

        CONDITIONS.put("s2_bandage", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:bandage"));

        CONDITIONS.put("s2_wooden_shelter", (p, s) ->
                // Check if player has placed at least 32 planks (any type)
                hasPlacedBlocks(p, "minecraft:oak_planks", 32));

        CONDITIONS.put("s2_baseball_bat", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:baseball_bat"));

        // ── Stage 3: Established ────────────────────────────────────────
        CONDITIONS.put("s3_workbench", (p, s) ->
                hasItemInInventory(p, "minecraft:crafting_table")
                        || hasUsedItem(p, "minecraft:crafting_table"));

        CONDITIONS.put("s3_forge", (p, s) ->
                hasItemInInventory(p, "minecraft:blast_furnace")
                        || hasUsedItem(p, "minecraft:blast_furnace"));

        CONDITIONS.put("s3_forged_iron", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:forged_iron"));

        CONDITIONS.put("s3_iron_tools", (p, s) ->
                hasItemInInventory(p, "minecraft:iron_pickaxe"));

        CONDITIONS.put("s3_scrap_armor", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:scrap_iron_helmet")
                        || hasItemInInventory(p, "sevendaystominecraft:scrap_iron_chestplate")
                        || hasItemInInventory(p, "sevendaystominecraft:scrap_iron_leggings")
                        || hasItemInInventory(p, "sevendaystominecraft:scrap_iron_boots"));

        CONDITIONS.put("s3_pistol", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:pistol_9mm"));

        CONDITIONS.put("s3_goldenrod_tea", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:goldenrod_tea"));

        // ── Stage 4: Fortified ──────────────────────────────────────────
        CONDITIONS.put("s4_adv_workbench", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:forged_steel"));

        CONDITIONS.put("s4_forged_steel", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:forged_steel"));

        CONDITIONS.put("s4_military_armor", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:military_helmet")
                        || hasItemInInventory(p, "sevendaystominecraft:military_chestplate")
                        || hasItemInInventory(p, "sevendaystominecraft:military_leggings")
                        || hasItemInInventory(p, "sevendaystominecraft:military_boots"));

        CONDITIONS.put("s4_ak47", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:ak47"));

        CONDITIONS.put("s4_shotgun", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:shotgun"));

        CONDITIONS.put("s4_farming", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:corn")
                        || hasItemInInventory(p, "sevendaystominecraft:blueberry")
                        || hasItemInInventory(p, "minecraft:potato"));

        CONDITIONS.put("s4_first_aid", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:first_aid_kit"));

        // ── Stage 5: Self-Sufficient ────────────────────────────────────
        CONDITIONS.put("s5_chem_station", (p, s) ->
                hasItemInInventory(p, "minecraft:gunpowder"));

        CONDITIONS.put("s5_cement_mixer", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:concrete_mix"));

        CONDITIONS.put("s5_antibiotics", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:antibiotics"));

        CONDITIONS.put("s5_sniper", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:sniper_rifle"));

        CONDITIONS.put("s5_m60", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:m60"));

        CONDITIONS.put("s5_concrete", (p, s) ->
                hasItemInInventory(p, "sevendaystominecraft:concrete_mix"));

        CONDITIONS.put("s5_clear_territory", (p, s) -> {
            // Detect if any territory was cleared — rely on quest completion or territory data
            // For now, check if the player has completed any CLEAR_TERRITORY quest
            for (QuestInstance quest : s.getActiveQuests()) {
                if (quest.getDefinition().getType() == QuestType.CLEAR_TERRITORY
                        && quest.getState() == QuestInstance.State.COMPLETED) {
                    return true;
                }
            }
            // Also check consumed quests as a fallback
            return !s.getConsumedQuests().isEmpty();
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL != 0) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        Set<String> completed = stats.getCompletedProgressionNodes();
        boolean changed = false;

        for (Map.Entry<String, NodeCondition> entry : CONDITIONS.entrySet()) {
            String nodeId = entry.getKey();
            if (completed.contains(nodeId)) continue;

            ProgressionNode node = ProgressionTreeRegistry.getNode(nodeId);
            if (node == null) continue;

            // Check prerequisites
            if (!ProgressionTreeRegistry.arePrerequisitesMet(node, completed)) continue;

            // Check condition
            try {
                if (entry.getValue().isMet(player, stats)) {
                    completed.add(nodeId);
                    changed = true;

                    // Award rewards
                    if (node.getRewardXp() > 0) {
                        stats.addXp(node.getRewardXp());
                    }
                    if (node.getRewardTokens() > 0) {
                        // Add coins to player inventory
                        ItemStack coins = new ItemStack(ModItems.SURVIVORS_COIN.get(), node.getRewardTokens());
                        if (!player.getInventory().add(coins)) {
                            player.drop(coins, false);
                        }
                    }

                    SevenDaysToMinecraft.LOGGER.info("BZHS: Player {} completed progression node: {} (+{} XP, +{} Coins)",
                            player.getScoreboardName(), nodeId, node.getRewardXp(), node.getRewardTokens());

                    // Send chat notification
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§a§l✦ Progression: §r§f" + node.getDisplayName() + " §7— " + node.getRewardText()));
                }
            } catch (Exception e) {
                SevenDaysToMinecraft.LOGGER.debug("BZHS: Error checking progression node {}: {}", nodeId, e.getMessage());
            }
        }

        if (changed) {
            syncProgressionToClient(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Sync on login
        syncProgressionToClient(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncProgressionToClient(player);
    }

    public static void syncProgressionToClient(ServerPlayer player) {
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        Set<String> completed = stats.getCompletedProgressionNodes();
        int highest = ProgressionTreeRegistry.getHighestCompletedStage(completed);

        PacketDistributor.sendToPlayer(player, new SyncProgressionPayload(
                new ArrayList<>(completed), highest));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static boolean hasItemInInventory(ServerPlayer player, String itemId) {
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(itemId);
        java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item>> opt =
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl);
        if (opt.isEmpty()) return false;
        net.minecraft.world.item.Item item = opt.get().value();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) return true;
        }
        return false;
    }

    private static int countItemInInventory(ServerPlayer player, String itemId) {
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(itemId);
        java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item>> opt =
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl);
        if (opt.isEmpty()) return 0;
        net.minecraft.world.item.Item item = opt.get().value();
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean hasPlacedBlocks(ServerPlayer player, String blockItemId, int minCount) {
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(blockItemId);
        java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item>> opt =
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl);
        if (opt.isEmpty()) return false;
        net.minecraft.world.item.Item item = opt.get().value();
        return player.getStats().getValue(net.minecraft.stats.Stats.ITEM_USED, item) >= minCount;
    }

    private static boolean hasUsedItem(ServerPlayer player, String itemId) {
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(itemId);
        java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item>> opt =
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl);
        if (opt.isEmpty()) return false;
        net.minecraft.world.item.Item item = opt.get().value();
        return player.getStats().getValue(net.minecraft.stats.Stats.ITEM_USED, item) > 0;
    }
}
