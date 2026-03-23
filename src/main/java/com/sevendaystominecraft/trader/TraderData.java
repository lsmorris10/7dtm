package com.sevendaystominecraft.trader;

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

public class TraderData extends SavedData {

    private static final String DATA_NAME = SevenDaysToMinecraft.MOD_ID + "_traders";

    private final Map<Integer, TraderRecord> tradersById = new HashMap<>();
    private int nextId = 1;
    private boolean guaranteedSpawnPlaced = false;

    public TraderData() {}

    public static TraderData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TraderData::new, TraderData::load),
                DATA_NAME
        );
    }

    public TraderRecord addTrader(BlockPos origin, String name, int tier) {
        int id = nextId++;
        TraderRecord record = new TraderRecord(id, origin, name, tier);
        tradersById.put(id, record);
        setDirty();
        return record;
    }

    public boolean hasNearby(BlockPos pos, int minChunkSpacing) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        for (TraderRecord record : tradersById.values()) {
            int rx = record.getOrigin().getX() >> 4;
            int rz = record.getOrigin().getZ() >> 4;
            int dx = Math.abs(chunkX - rx);
            int dz = Math.abs(chunkZ - rz);
            if (dx <= minChunkSpacing && dz <= minChunkSpacing) return true;
        }
        return false;
    }

    public int countNearby(BlockPos pos, int chunkRadius) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        int count = 0;
        for (TraderRecord record : tradersById.values()) {
            int rx = record.getOrigin().getX() >> 4;
            int rz = record.getOrigin().getZ() >> 4;
            int dx = Math.abs(chunkX - rx);
            int dz = Math.abs(chunkZ - rz);
            if (dx <= chunkRadius && dz <= chunkRadius) count++;
        }
        return count;
    }

    public TraderRecord getTraderById(int id) {
        return tradersById.get(id);
    }

    public Collection<TraderRecord> getAllTraders() {
        return tradersById.values();
    }

    public List<TraderRecord> getNearby(BlockPos pos, double maxDistance) {
        List<TraderRecord> result = new ArrayList<>();
        for (TraderRecord record : tradersById.values()) {
            double dx = record.getOrigin().getX() - pos.getX();
            double dz = record.getOrigin().getZ() - pos.getZ();
            if (Math.sqrt(dx * dx + dz * dz) <= maxDistance) {
                result.add(record);
            }
        }
        return result;
    }

    public boolean isGuaranteedSpawnPlaced() {
        return guaranteedSpawnPlaced;
    }

    public void setGuaranteedSpawnPlaced(boolean placed) {
        this.guaranteedSpawnPlaced = placed;
        setDirty();
    }

    public boolean isInProtectionZone(BlockPos pos, int protectionRadius, int compoundProtectionRadius) {
        for (TraderRecord record : tradersById.values()) {
            double dxTrader = record.getOrigin().getX() - pos.getX();
            double dzTrader = record.getOrigin().getZ() - pos.getZ();
            if (Math.sqrt(dxTrader * dxTrader + dzTrader * dzTrader) <= protectionRadius) {
                return true;
            }

            BlockPos compound = record.getCompoundCenter();
            if (compound != null) {
                double dxCompound = compound.getX() - pos.getX();
                double dzCompound = compound.getZ() - pos.getZ();
                if (Math.sqrt(dxCompound * dxCompound + dzCompound * dzCompound) <= compoundProtectionRadius) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBlockDirectlyBelowTrader(BlockPos pos) {
        for (TraderRecord record : tradersById.values()) {
            BlockPos below = record.getOrigin().below();
            if (below.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public void markDirtyRecord() {
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("nextId", nextId);
        tag.putBoolean("guaranteedSpawnPlaced", guaranteedSpawnPlaced);
        ListTag list = new ListTag();
        for (TraderRecord record : tradersById.values()) {
            list.add(record.save());
        }
        tag.put("traders", list);
        return tag;
    }

    public static TraderData load(CompoundTag tag, HolderLookup.Provider registries) {
        TraderData data = new TraderData();
        data.nextId = tag.getInt("nextId");
        if (data.nextId < 1) data.nextId = 1;
        data.guaranteedSpawnPlaced = tag.getBoolean("guaranteedSpawnPlaced");
        ListTag list = tag.getList("traders", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            TraderRecord record = TraderRecord.load(list.getCompound(i));
            data.tradersById.put(record.getId(), record);
        }
        return data;
    }
}
