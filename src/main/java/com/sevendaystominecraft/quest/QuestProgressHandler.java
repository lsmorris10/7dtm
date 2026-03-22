package com.sevendaystominecraft.quest;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.entity.zombie.BaseSevenDaysZombie;
import com.sevendaystominecraft.entity.zombie.ZombieVariant;
import com.sevendaystominecraft.territory.TerritoryData;
import com.sevendaystominecraft.territory.TerritoryRecord;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID)
public class QuestProgressHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        var source = event.getSource();

        ServerPlayer player = null;
        if (source.getEntity() instanceof ServerPlayer sp) {
            player = sp;
        } else if (source.getEntity() != null && source.getEntity().level() instanceof ServerLevel) {
            var owner = source.getEntity();
            if (owner instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
                if (projectile.getOwner() instanceof ServerPlayer sp) {
                    player = sp;
                }
            }
        }
        if (player == null) return;

        if (event.getEntity() instanceof BaseSevenDaysZombie zombie) {
            ZombieVariant variant = zombie.getVariant();
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            boolean changed = false;

            for (QuestInstance quest : stats.getActiveQuests()) {
                if (quest.getState() != QuestInstance.State.ACTIVE) continue;
                QuestDefinition def = quest.getDefinition();

                if (def.getType() == QuestType.KILL_COUNT) {
                    if (variant.name().equals(def.getTargetId())) {
                        quest.incrementProgress(1);
                        changed = true;
                    }
                }
            }

            if (changed) {
                QuestSyncHelper.syncQuests(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 40 != 0) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        List<QuestInstance> quests = stats.getActiveQuests();
        if (quests.isEmpty()) return;

        boolean changed = false;

        for (QuestInstance quest : quests) {
            if (quest.getState() != QuestInstance.State.ACTIVE) continue;
            QuestDefinition def = quest.getDefinition();

            if (def.getType() == QuestType.FETCH_DELIVER) {
                ItemStack target = QuestGenerator.getItemForFetchQuest(def.getTargetId());
                if (!target.isEmpty()) {
                    int count = countItem(player, target);
                    if (count != quest.getProgress()) {
                        quest.setProgress(count);
                        changed = true;
                    }
                }
            } else if (def.getType() == QuestType.CLEAR_TERRITORY) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    try {
                        int territoryId = Integer.parseInt(def.getTargetId());
                        TerritoryData territoryData = TerritoryData.getOrCreate(serverLevel);
                        TerritoryRecord record = territoryData.getTerritoryById(territoryId);
                        if (record != null && record.isCleared()) {
                            quest.incrementProgress(1);
                            changed = true;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (changed) {
            QuestSyncHelper.syncQuests(player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        BlockPos brokenPos = event.getPos();
        Block brokenBlock = event.getState().getBlock();

        if (brokenBlock != ModBlocks.SUPPLY_CRATE_BLOCK.get()) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        boolean changed = false;

        for (QuestInstance quest : stats.getActiveQuests()) {
            if (quest.getState() != QuestInstance.State.ACTIVE) continue;
            QuestDefinition def = quest.getDefinition();
            if (def.getType() != QuestType.BURIED_TREASURE) continue;
            if (def.getTargetLocation() == null) continue;

            if (brokenPos.equals(def.getTargetLocation())) {
                quest.incrementProgress(1);
                changed = true;
            }
        }

        if (changed) {
            QuestSyncHelper.syncQuests(player);
        }
    }

    public static void placeSupplyCache(ServerLevel level, BlockPos cachePos) {
        level.setBlock(cachePos, ModBlocks.SUPPLY_CRATE_BLOCK.get().defaultBlockState(), 3);
        SevenDaysToMinecraft.LOGGER.debug("[BZHS] Placed buried supply cache at {}", cachePos);
    }

    public static BlockPos getCachePos(ServerLevel level, BlockPos targetPos) {
        BlockPos surfacePos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, targetPos);
        BlockPos cachePos = surfacePos.below(3);
        if (cachePos.getY() < level.getMinY() + 1) {
            cachePos = new BlockPos(cachePos.getX(), level.getMinY() + 1, cachePos.getZ());
        }
        return cachePos;
    }

    private static int countItem(Player player, ItemStack target) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == target.getItem()) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
