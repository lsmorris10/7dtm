package com.sevendaystominecraft.heatmap;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HeatmapConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HeatmapData extends SavedData {

    private static final String DATA_NAME = SevenDaysToMinecraft.MOD_ID + "_heatmap";
    private static final float MAX_HEAT = 100.0f;

    private final Map<Long, List<HeatSource>> chunkSources = new HashMap<>();

    public HeatmapData() {
    }

    public static HeatmapData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(HeatmapData::new, HeatmapData::load),
                DATA_NAME
        );
    }

    public float getHeat(ChunkPos pos) {
        List<HeatSource> sources = chunkSources.get(pos.toLong());
        if (sources == null || sources.isEmpty()) return 0;
        float total = 0;
        for (HeatSource source : sources) {
            total += source.getAmount();
        }
        return total;
    }

    public void addHeatSource(ChunkPos pos, float amount, float decayPerMinute, int radiusChunks) {
        float currentHeat = getHeat(pos);
        if (currentHeat >= MAX_HEAT) return;

        float effectiveAmount = Math.min(amount, MAX_HEAT - currentHeat);
        chunkSources.computeIfAbsent(pos.toLong(), k -> new ArrayList<>())
                .add(new HeatSource(effectiveAmount, decayPerMinute, radiusChunks));
        setDirty();
    }

    public void addHeatWithRadius(ChunkPos center, float amount, int radiusChunks, float decayPerMinute) {
        addHeatSource(center, amount, decayPerMinute, radiusChunks);

        if (radiusChunks > 0) {
            for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
                for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > radiusChunks) continue;
                    float falloff = 1.0f - (float) (dist / (radiusChunks + 1));
                    float spreadAmount = amount * falloff * 0.5f;
                    if (spreadAmount > 0.01f) {
                        ChunkPos neighbor = new ChunkPos(center.x + dx, center.z + dz);
                        addHeatSource(neighbor, spreadAmount, decayPerMinute, 0);
                    }
                }
            }
        }
    }

    public void tickDecay() {
        if (chunkSources.isEmpty()) return;

        float decayMult = HeatmapConfig.INSTANCE.decayMultiplier.get().floatValue();
        Iterator<Map.Entry<Long, List<HeatSource>>> chunkIter = chunkSources.entrySet().iterator();

        while (chunkIter.hasNext()) {
            Map.Entry<Long, List<HeatSource>> entry = chunkIter.next();
            List<HeatSource> sources = entry.getValue();
            Iterator<HeatSource> sourceIter = sources.iterator();

            while (sourceIter.hasNext()) {
                HeatSource source = sourceIter.next();
                source.decay(decayMult);
                if (source.isDepleted()) {
                    sourceIter.remove();
                }
            }

            if (sources.isEmpty()) {
                chunkIter.remove();
            }
        }

        setDirty();
    }

    public Map<Long, List<HeatSource>> getAllChunkSources() {
        return chunkSources;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag chunkList = new ListTag();

        for (Map.Entry<Long, List<HeatSource>> entry : chunkSources.entrySet()) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putLong("pos", entry.getKey());

            ListTag sourceList = new ListTag();
            for (HeatSource source : entry.getValue()) {
                sourceList.add(source.save());
            }
            chunkTag.put("sources", sourceList);
            chunkList.add(chunkTag);
        }

        tag.put("chunks", chunkList);
        return tag;
    }

    public static HeatmapData load(CompoundTag tag, HolderLookup.Provider registries) {
        HeatmapData data = new HeatmapData();
        ListTag chunkList = tag.getList("chunks", Tag.TAG_COMPOUND);

        for (int i = 0; i < chunkList.size(); i++) {
            CompoundTag chunkTag = chunkList.getCompound(i);
            long pos = chunkTag.getLong("pos");
            ListTag sourceList = chunkTag.getList("sources", Tag.TAG_COMPOUND);

            List<HeatSource> sources = new ArrayList<>();
            for (int j = 0; j < sourceList.size(); j++) {
                sources.add(HeatSource.load(sourceList.getCompound(j)));
            }

            if (!sources.isEmpty()) {
                data.chunkSources.put(pos, sources);
            }
        }

        return data;
    }
}
