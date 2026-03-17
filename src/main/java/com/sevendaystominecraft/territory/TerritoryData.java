package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerritoryData extends SavedData {

    private static final String DATA_NAME = SevenDaysToMinecraft.MOD_ID + "_territories";

    private final Map<Integer, TerritoryRecord> territoriesById = new HashMap<>();
    private final Map<Long, Integer> chunkToTerritoryId = new HashMap<>();
    private int nextId = 1;

    public TerritoryData() {}

    public static TerritoryData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TerritoryData::new, TerritoryData::load),
                DATA_NAME
        );
    }

    public TerritoryRecord addTerritory(BlockPos origin, TerritoryTier tier, TerritoryType type) {
        int id = nextId++;
        TerritoryRecord record = new TerritoryRecord(id, origin, tier, type);
        territoriesById.put(id, record);

        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        long chunkKey = ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
        chunkToTerritoryId.put(chunkKey, id);

        setDirty();
        return record;
    }

    public boolean hasNearby(BlockPos pos, int minChunkSpacing) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        for (TerritoryRecord record : territoriesById.values()) {
            int rx = record.getOrigin().getX() >> 4;
            int rz = record.getOrigin().getZ() >> 4;
            int dx = Math.abs(chunkX - rx);
            int dz = Math.abs(chunkZ - rz);
            if (dx <= minChunkSpacing && dz <= minChunkSpacing) return true;
        }
        return false;
    }

    public TerritoryRecord getTerritoryById(int id) {
        return territoriesById.get(id);
    }

    public Collection<TerritoryRecord> getAllTerritories() {
        return territoriesById.values();
    }

    public List<TerritoryRecord> getNearby(BlockPos pos, double maxDistance) {
        List<TerritoryRecord> result = new ArrayList<>();
        for (TerritoryRecord record : territoriesById.values()) {
            double dx = record.getOrigin().getX() - pos.getX();
            double dz = record.getOrigin().getZ() - pos.getZ();
            if (Math.sqrt(dx * dx + dz * dz) <= maxDistance) {
                result.add(record);
            }
        }
        return result;
    }

    public void markDirtyRecord() {
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("nextId", nextId);
        ListTag list = new ListTag();
        for (TerritoryRecord record : territoriesById.values()) {
            list.add(record.save());
        }
        tag.put("territories", list);
        return tag;
    }

    public static TerritoryData load(CompoundTag tag, HolderLookup.Provider registries) {
        TerritoryData data = new TerritoryData();
        data.nextId = tag.getInt("nextId");
        if (data.nextId < 1) data.nextId = 1;
        ListTag list = tag.getList("territories", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            TerritoryRecord record = TerritoryRecord.load(list.getCompound(i));
            data.territoriesById.put(record.getId(), record);
            int chunkX = record.getOrigin().getX() >> 4;
            int chunkZ = record.getOrigin().getZ() >> 4;
            long chunkKey = ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
            data.chunkToTerritoryId.put(chunkKey, record.getId());
        }
        return data;
    }

}
