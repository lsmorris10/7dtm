package com.sevendaystominecraft.entity.zombie.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class BlockDamageTracker {

    private static final BlockDamageTracker INSTANCE = new BlockDamageTracker();
    private static final long IDLE_TIMEOUT_TICKS = 200;

    private final Map<DimensionBlockPos, BlockDamageEntry> trackedBlocks = new HashMap<>();

    private BlockDamageTracker() {}

    public static BlockDamageTracker getInstance() {
        return INSTANCE;
    }

    public float addDamage(ResourceKey<Level> dimension, BlockPos pos, UUID zombieId, float damage, long gameTick) {
        DimensionBlockPos key = new DimensionBlockPos(dimension, pos.immutable());
        BlockDamageEntry entry = trackedBlocks.computeIfAbsent(key, k -> new BlockDamageEntry());
        entry.accumulatedDamage += damage;
        entry.contributors.add(zombieId);
        entry.lastDamageGameTick = gameTick;
        return entry.accumulatedDamage;
    }

    public float getDamage(ResourceKey<Level> dimension, BlockPos pos) {
        BlockDamageEntry entry = trackedBlocks.get(new DimensionBlockPos(dimension, pos.immutable()));
        return entry != null ? entry.accumulatedDamage : 0f;
    }

    public void removeBlock(ResourceKey<Level> dimension, BlockPos pos) {
        trackedBlocks.remove(new DimensionBlockPos(dimension, pos.immutable()));
    }

    public void removeContributor(ResourceKey<Level> dimension, BlockPos pos, UUID zombieId) {
        DimensionBlockPos key = new DimensionBlockPos(dimension, pos.immutable());
        BlockDamageEntry entry = trackedBlocks.get(key);
        if (entry != null) {
            entry.contributors.remove(zombieId);
            if (entry.contributors.isEmpty()) {
                trackedBlocks.remove(key);
            }
        }
    }

    public int getContributorCount(ResourceKey<Level> dimension, BlockPos pos) {
        BlockDamageEntry entry = trackedBlocks.get(new DimensionBlockPos(dimension, pos.immutable()));
        return entry != null ? entry.contributors.size() : 0;
    }

    public void cleanupIdleEntries(long currentGameTick) {
        Iterator<Map.Entry<DimensionBlockPos, BlockDamageEntry>> it = trackedBlocks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<DimensionBlockPos, BlockDamageEntry> mapEntry = it.next();
            BlockDamageEntry entry = mapEntry.getValue();
            if (currentGameTick - entry.lastDamageGameTick > IDLE_TIMEOUT_TICKS) {
                it.remove();
            }
        }
    }

    public void clearDimension(ResourceKey<Level> dimension) {
        trackedBlocks.keySet().removeIf(key -> key.dimension.equals(dimension));
    }

    public void clear() {
        trackedBlocks.clear();
    }

    private record DimensionBlockPos(ResourceKey<Level> dimension, BlockPos pos) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DimensionBlockPos other)) return false;
            return dimension.equals(other.dimension) && pos.equals(other.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, pos);
        }
    }

    private static class BlockDamageEntry {
        float accumulatedDamage;
        final Set<UUID> contributors = new HashSet<>();
        long lastDamageGameTick;

        BlockDamageEntry() {
            this.accumulatedDamage = 0f;
            this.lastDamageGameTick = 0;
        }
    }
}
