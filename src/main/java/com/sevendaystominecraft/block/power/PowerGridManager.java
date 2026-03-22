package com.sevendaystominecraft.block.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class PowerGridManager extends SavedData {

    private static final String DATA_NAME = "sevendaystominecraft_power_grid";
    private static final int MAX_WIRE_RANGE = 16;

    private final Map<BlockPos, Set<BlockPos>> sourceToDevices = new HashMap<>();
    private final Map<BlockPos, Set<BlockPos>> deviceToSources = new HashMap<>();
    private final Map<UUID, BlockPos> linkingInProgress = new HashMap<>();

    public static PowerGridManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PowerGridManager::new, PowerGridManager::load),
                DATA_NAME
        );
    }

    public PowerGridManager() {
    }

    public static PowerGridManager load(CompoundTag tag, HolderLookup.Provider registries) {
        PowerGridManager manager = new PowerGridManager();
        ListTag connections = tag.getList("Connections", Tag.TAG_COMPOUND);
        for (int i = 0; i < connections.size(); i++) {
            CompoundTag entry = connections.getCompound(i);
            BlockPos source = BlockPos.of(entry.getLong("Source"));
            ListTag deviceList = entry.getList("Devices", Tag.TAG_COMPOUND);
            for (int j = 0; j < deviceList.size(); j++) {
                BlockPos device = BlockPos.of(deviceList.getCompound(j).getLong("Pos"));
                manager.sourceToDevices.computeIfAbsent(source, k -> new HashSet<>()).add(device);
                manager.deviceToSources.computeIfAbsent(device, k -> new HashSet<>()).add(source);
            }
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag connections = new ListTag();
        for (Map.Entry<BlockPos, Set<BlockPos>> entry : sourceToDevices.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("Source", entry.getKey().asLong());
            ListTag deviceList = new ListTag();
            for (BlockPos device : entry.getValue()) {
                CompoundTag deviceTag = new CompoundTag();
                deviceTag.putLong("Pos", device.asLong());
                deviceList.add(deviceTag);
            }
            entryTag.put("Devices", deviceList);
            connections.add(entryTag);
        }
        tag.put("Connections", connections);
        return tag;
    }

    public boolean addConnection(BlockPos source, BlockPos device) {
        if (source.equals(device)) return false;
        if (source.distSqr(device) > MAX_WIRE_RANGE * MAX_WIRE_RANGE) return false;

        sourceToDevices.computeIfAbsent(source, k -> new HashSet<>()).add(device);
        deviceToSources.computeIfAbsent(device, k -> new HashSet<>()).add(source);
        setDirty();
        return true;
    }

    public void removeConnection(BlockPos source, BlockPos device) {
        Set<BlockPos> devices = sourceToDevices.get(source);
        if (devices != null) {
            devices.remove(device);
            if (devices.isEmpty()) sourceToDevices.remove(source);
        }
        Set<BlockPos> sources = deviceToSources.get(device);
        if (sources != null) {
            sources.remove(source);
            if (sources.isEmpty()) deviceToSources.remove(device);
        }
        setDirty();
    }

    public void removeAllConnections(BlockPos pos) {
        Set<BlockPos> devices = sourceToDevices.remove(pos);
        if (devices != null) {
            for (BlockPos device : devices) {
                Set<BlockPos> sources = deviceToSources.get(device);
                if (sources != null) {
                    sources.remove(pos);
                    if (sources.isEmpty()) deviceToSources.remove(device);
                }
            }
        }

        Set<BlockPos> sources = deviceToSources.remove(pos);
        if (sources != null) {
            for (BlockPos source : sources) {
                Set<BlockPos> devs = sourceToDevices.get(source);
                if (devs != null) {
                    devs.remove(pos);
                    if (devs.isEmpty()) sourceToDevices.remove(source);
                }
            }
        }
        setDirty();
    }

    public Set<BlockPos> getConnectedDevices(BlockPos source) {
        return Collections.unmodifiableSet(sourceToDevices.getOrDefault(source, Collections.emptySet()));
    }

    public Set<BlockPos> getConnectedSources(BlockPos device) {
        return Collections.unmodifiableSet(deviceToSources.getOrDefault(device, Collections.emptySet()));
    }

    public boolean isDevicePowered(ServerLevel level, BlockPos devicePos) {
        Set<BlockPos> sources = deviceToSources.get(devicePos);
        if (sources == null || sources.isEmpty()) return false;

        for (BlockPos sourcePos : sources) {
            var be = level.getBlockEntity(sourcePos);
            if (be instanceof PowerSourceBlockEntity source && source.isProducingPower()) {
                return true;
            }
        }
        return false;
    }

    public void startLinking(UUID playerId, BlockPos sourcePos) {
        linkingInProgress.put(playerId, sourcePos);
    }

    public BlockPos getLinkingSource(UUID playerId) {
        return linkingInProgress.get(playerId);
    }

    public void cancelLinking(UUID playerId) {
        linkingInProgress.remove(playerId);
    }

    public static int getMaxWireRange() {
        return MAX_WIRE_RANGE;
    }

    public Map<BlockPos, Set<BlockPos>> getAllConnections() {
        return Collections.unmodifiableMap(sourceToDevices);
    }
}
