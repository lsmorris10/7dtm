package com.sevendaystominecraft.quest;

import com.sevendaystominecraft.config.QuestConfig;
import com.sevendaystominecraft.entity.zombie.ZombieVariant;
import com.sevendaystominecraft.territory.TerritoryData;
import com.sevendaystominecraft.territory.TerritoryRecord;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class QuestGenerator {

    private static final String[][] FETCH_ITEMS_BY_TIER = {
            {"Iron Ingot", "Leather", "Bone", "String", "Coal"},
            {"Iron Ingot", "Gold Ingot", "Gunpowder", "Redstone", "Leather"},
            {"Diamond", "Emerald", "Gold Ingot", "Iron Ingot", "Obsidian"}
    };

    private static final int[][] FETCH_COUNTS_BY_TIER = {
            {10, 8, 15, 12, 10},
            {15, 8, 12, 16, 10},
            {5, 8, 12, 20, 10}
    };

    private static final String[][] KILL_TARGETS_BY_TIER = {
            {"Walker", "Crawler"},
            {"Walker", "Bloated Walker", "Spider Zombie", "Crawler"},
            {"Feral Wight", "Cop", "Spider Zombie", "Soldier", "Nurse"}
    };

    private static final ZombieVariant[][] KILL_VARIANTS_BY_TIER = {
            {ZombieVariant.WALKER, ZombieVariant.CRAWLER},
            {ZombieVariant.WALKER, ZombieVariant.BLOATED_WALKER, ZombieVariant.SPIDER_ZOMBIE, ZombieVariant.CRAWLER},
            {ZombieVariant.FERAL_WIGHT, ZombieVariant.COP, ZombieVariant.SPIDER_ZOMBIE, ZombieVariant.SOLDIER, ZombieVariant.NURSE}
    };

    private static final int[][] KILL_COUNTS_BY_TIER = {
            {8, 10},
            {6, 5, 4, 8},
            {4, 3, 5, 3, 5}
    };

    public static List<QuestDefinition> generateQuests(ServerLevel level, int traderId, int traderTier, BlockPos traderPos) {
        RandomSource random = level.getRandom();
        int questCount = QuestConfig.INSTANCE.questsPerTrader.get();
        List<QuestDefinition> quests = new ArrayList<>();

        List<QuestType> types = new ArrayList<>(List.of(QuestType.values()));

        for (int i = 0; i < questCount && !types.isEmpty(); i++) {
            int typeIdx = random.nextInt(types.size());
            QuestType type = types.get(typeIdx);

            QuestDefinition quest = generateQuestOfType(level, type, traderId, traderTier, traderPos, random);
            if (quest != null) {
                quests.add(quest);
            } else {
                types.remove(typeIdx);
                i--;
            }

            if (quests.size() >= 2) {
                types = new ArrayList<>(List.of(QuestType.values()));
            }
        }

        return quests;
    }

    private static QuestDefinition generateQuestOfType(ServerLevel level, QuestType type,
                                                        int traderId, int traderTier,
                                                        BlockPos traderPos, RandomSource random) {
        int tierIndex = Math.max(0, Math.min(2, traderTier - 1));
        int baseXp = 200 + tierIndex * 150;
        int baseTokens = 50 + tierIndex * 50;

        return switch (type) {
            case CLEAR_TERRITORY -> generateClearTerritory(level, traderId, traderTier, traderPos, random, baseXp, baseTokens);
            case FETCH_DELIVER -> generateFetchDeliver(traderId, traderTier, random, baseXp, baseTokens, tierIndex);
            case KILL_COUNT -> generateKillCount(traderId, traderTier, random, baseXp, baseTokens, tierIndex);
            case BURIED_TREASURE -> generateBuriedTreasure(traderId, traderTier, traderPos, random, baseXp, baseTokens);
        };
    }

    private static QuestDefinition generateClearTerritory(ServerLevel level, int traderId, int traderTier,
                                                           BlockPos traderPos, RandomSource random,
                                                           int baseXp, int baseTokens) {
        TerritoryData territoryData = TerritoryData.getOrCreate(level);
        List<TerritoryRecord> nearby = territoryData.getNearby(traderPos, 500);

        List<TerritoryRecord> uncleared = new ArrayList<>();
        for (TerritoryRecord record : nearby) {
            if (!record.isCleared()) {
                uncleared.add(record);
            }
        }

        if (uncleared.isEmpty()) return null;

        TerritoryRecord target = uncleared.get(random.nextInt(uncleared.size()));
        int xpReward = (int) ((baseXp + target.getTier().getTier() * 100) * QuestConfig.INSTANCE.xpRewardMultiplier.get());
        int tokenReward = (int) ((baseTokens + target.getTier().getTier() * 30) * QuestConfig.INSTANCE.tokenRewardMultiplier.get());

        return new QuestDefinition(
                QuestType.CLEAR_TERRITORY,
                "Clear " + target.getType().getDisplayName()
                        + " " + target.getTier().getStars()
                        + " [X:" + target.getOrigin().getX() + " Z:" + target.getOrigin().getZ() + "]",
                1,
                xpReward,
                tokenReward,
                String.valueOf(target.getId()),
                target.getOrigin(),
                traderId,
                traderTier
        );
    }

    private static QuestDefinition generateFetchDeliver(int traderId, int traderTier, RandomSource random,
                                                         int baseXp, int baseTokens, int tierIndex) {
        String[] items = FETCH_ITEMS_BY_TIER[tierIndex];
        int[] counts = FETCH_COUNTS_BY_TIER[tierIndex];
        int idx = random.nextInt(items.length);
        String itemName = items[idx];
        int count = counts[idx];

        int xpReward = (int) ((baseXp) * QuestConfig.INSTANCE.xpRewardMultiplier.get());
        int tokenReward = (int) ((baseTokens) * QuestConfig.INSTANCE.tokenRewardMultiplier.get());

        return new QuestDefinition(
                QuestType.FETCH_DELIVER,
                "Bring " + count + " " + itemName + " to the trader",
                count,
                xpReward,
                tokenReward,
                itemName,
                null,
                traderId,
                traderTier
        );
    }

    private static QuestDefinition generateKillCount(int traderId, int traderTier, RandomSource random,
                                                      int baseXp, int baseTokens, int tierIndex) {
        String[] targets = KILL_TARGETS_BY_TIER[tierIndex];
        int[] counts = KILL_COUNTS_BY_TIER[tierIndex];
        int idx = random.nextInt(targets.length);
        String targetName = targets[idx];
        int count = counts[idx];

        ZombieVariant variant = KILL_VARIANTS_BY_TIER[tierIndex][idx];
        int xpReward = (int) ((baseXp + variant.getXpReward()) * QuestConfig.INSTANCE.xpRewardMultiplier.get());
        int tokenReward = (int) ((baseTokens + count * 5) * QuestConfig.INSTANCE.tokenRewardMultiplier.get());

        return new QuestDefinition(
                QuestType.KILL_COUNT,
                "Kill " + count + " " + targetName + (count > 1 ? "s" : ""),
                count,
                xpReward,
                tokenReward,
                variant.name(),
                null,
                traderId,
                traderTier
        );
    }

    private static QuestDefinition generateBuriedTreasure(int traderId, int traderTier,
                                                           BlockPos traderPos, RandomSource random,
                                                           int baseXp, int baseTokens) {
        int maxRange = QuestConfig.INSTANCE.buriedTreasureMaxRange.get();
        int offsetX = random.nextInt(maxRange * 2) - maxRange;
        int offsetZ = random.nextInt(maxRange * 2) - maxRange;
        if (Math.abs(offsetX) < 30) offsetX = offsetX >= 0 ? 30 : -30;
        if (Math.abs(offsetZ) < 30) offsetZ = offsetZ >= 0 ? 30 : -30;

        BlockPos target = new BlockPos(traderPos.getX() + offsetX, 64, traderPos.getZ() + offsetZ);

        int xpReward = (int) ((baseXp + 100) * QuestConfig.INSTANCE.xpRewardMultiplier.get());
        int tokenReward = (int) ((baseTokens + 40) * QuestConfig.INSTANCE.tokenRewardMultiplier.get());

        return new QuestDefinition(
                QuestType.BURIED_TREASURE,
                "Dig up the supply cache at " + formatPos(target),
                1,
                xpReward,
                tokenReward,
                "treasure",
                target,
                traderId,
                traderTier
        );
    }

    private static String formatPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getZ() + ")";
    }

    public static ItemStack getItemForFetchQuest(String itemName) {
        return switch (itemName) {
            case "Iron Ingot" -> new ItemStack(Items.IRON_INGOT);
            case "Leather" -> new ItemStack(Items.LEATHER);
            case "Bone" -> new ItemStack(Items.BONE);
            case "String" -> new ItemStack(Items.STRING);
            case "Coal" -> new ItemStack(Items.COAL);
            case "Gold Ingot" -> new ItemStack(Items.GOLD_INGOT);
            case "Gunpowder" -> new ItemStack(Items.GUNPOWDER);
            case "Redstone" -> new ItemStack(Items.REDSTONE);
            case "Diamond" -> new ItemStack(Items.DIAMOND);
            case "Emerald" -> new ItemStack(Items.EMERALD);
            case "Obsidian" -> new ItemStack(Items.OBSIDIAN);
            default -> ItemStack.EMPTY;
        };
    }
}
