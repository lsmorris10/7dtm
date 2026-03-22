package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.quest.QuestDefinition;
import com.sevendaystominecraft.quest.QuestGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraderRecord {

    private final int id;
    private final BlockPos origin;
    private final String name;
    private final int tier;
    private long lastRestockDay;
    private final Map<Integer, Integer> stock = new HashMap<>();
    private boolean stockInitialized = false;
    private final List<QuestDefinition> availableQuests = new ArrayList<>();
    private long lastQuestRefreshDay = -1;

    public TraderRecord(int id, BlockPos origin, String name, int tier) {
        this.id = id;
        this.origin = origin;
        this.name = name;
        this.tier = tier;
        this.lastRestockDay = -1;
        initializeStock();
    }

    private void initializeStock() {
        List<TraderInventory.TraderOffer> offers = TraderInventory.getOffersForTier(tier);
        for (int i = 0; i < offers.size(); i++) {
            stock.put(i, offers.get(i).maxStock());
        }
        stockInitialized = true;
    }

    public int getId() { return id; }
    public BlockPos getOrigin() { return origin; }
    public String getName() { return name; }
    public int getTier() { return tier; }
    public long getLastRestockDay() { return lastRestockDay; }
    public void setLastRestockDay(long day) { this.lastRestockDay = day; }

    public int getStock(int offerIndex) {
        return stock.getOrDefault(offerIndex, 0);
    }

    public void decrementStock(int offerIndex) {
        int current = stock.getOrDefault(offerIndex, 0);
        if (current > 0) {
            stock.put(offerIndex, current - 1);
        }
    }

    public void restock() {
        List<TraderInventory.TraderOffer> offers = TraderInventory.getOffersForTier(tier);
        for (int i = 0; i < offers.size(); i++) {
            stock.put(i, offers.get(i).maxStock());
        }
    }

    public List<QuestDefinition> getAvailableQuests(ServerLevel level) {
        if (availableQuests.isEmpty() || lastQuestRefreshDay != lastRestockDay) {
            refreshQuests(level);
            lastQuestRefreshDay = lastRestockDay;
        }
        return availableQuests;
    }

    private void refreshQuests(ServerLevel level) {
        availableQuests.clear();
        availableQuests.addAll(QuestGenerator.generateQuests(level, id, tier, origin));
    }

    public long getLastQuestRefreshDay() { return lastQuestRefreshDay; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", id);
        tag.putInt("x", origin.getX());
        tag.putInt("y", origin.getY());
        tag.putInt("z", origin.getZ());
        tag.putString("name", name);
        tag.putInt("tier", tier);
        tag.putLong("lastRestockDay", lastRestockDay);

        ListTag stockList = new ListTag();
        for (Map.Entry<Integer, Integer> entry : stock.entrySet()) {
            CompoundTag stockEntry = new CompoundTag();
            stockEntry.putInt("idx", entry.getKey());
            stockEntry.putInt("qty", entry.getValue());
            stockList.add(stockEntry);
        }
        tag.put("stock", stockList);

        if (!availableQuests.isEmpty()) {
            ListTag questList = new ListTag();
            for (QuestDefinition def : availableQuests) {
                questList.add(def.save());
            }
            tag.put("quests", questList);
        }
        tag.putLong("lastQuestRefreshDay", lastQuestRefreshDay);

        return tag;
    }

    public static TraderRecord load(CompoundTag tag) {
        int id = tag.getInt("id");
        BlockPos origin = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        String name = tag.getString("name");
        int tier = tag.getInt("tier");
        TraderRecord record = new TraderRecord(id, origin, name, tier);
        record.lastRestockDay = tag.getLong("lastRestockDay");

        if (tag.contains("stock")) {
            record.stock.clear();
            ListTag stockList = tag.getList("stock", Tag.TAG_COMPOUND);
            for (int i = 0; i < stockList.size(); i++) {
                CompoundTag stockEntry = stockList.getCompound(i);
                record.stock.put(stockEntry.getInt("idx"), stockEntry.getInt("qty"));
            }
            record.stockInitialized = true;
        }

        if (tag.contains("quests")) {
            ListTag questList = tag.getList("quests", Tag.TAG_COMPOUND);
            for (int i = 0; i < questList.size(); i++) {
                record.availableQuests.add(QuestDefinition.load(questList.getCompound(i)));
            }
        }
        record.lastQuestRefreshDay = tag.contains("lastQuestRefreshDay") ? tag.getLong("lastQuestRefreshDay") : -1;

        return record;
    }
}
