package com.sevendaystominecraft.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TerritoryRecord {

    private final int id;
    private final BlockPos origin;
    private final TerritoryTier tier;
    private final TerritoryType type;
    private boolean cleared;
    private boolean awakened;
    private int zombiesRemaining;
    private final List<BlockPos> buildingCenters = new ArrayList<>();
    private final List<String> buildingTypeNames = new ArrayList<>();
    private final Set<Integer> awakenedBuildings = new HashSet<>();

    public TerritoryRecord(int id, BlockPos origin, TerritoryTier tier, TerritoryType type) {
        this.id = id;
        this.origin = origin;
        this.tier = tier;
        this.type = type;
        this.cleared = false;
        this.awakened = false;
        this.zombiesRemaining = 0;
    }

    public int getId() { return id; }
    public BlockPos getOrigin() { return origin; }
    public TerritoryTier getTier() { return tier; }
    public TerritoryType getType() { return type; }
    public boolean isCleared() { return cleared; }
    public boolean isAwakened() { return awakened; }
    public int getZombiesRemaining() { return zombiesRemaining; }
    public List<BlockPos> getBuildingCenters() { return buildingCenters; }
    public List<String> getBuildingTypeNames() { return buildingTypeNames; }

    public void setCleared(boolean cleared) { this.cleared = cleared; }
    public void setAwakened(boolean awakened) { this.awakened = awakened; }
    public void setZombiesRemaining(int count) { this.zombiesRemaining = count; }
    public void decrementZombies() {
        zombiesRemaining = Math.max(0, zombiesRemaining - 1);
        if (zombiesRemaining == 0 && !cleared) {
            cleared = true;
        }
    }

    public void setBuildingCenters(List<BlockPos> centers) {
        this.buildingCenters.clear();
        this.buildingCenters.addAll(centers);
    }

    public void setBuildingTypeNames(List<String> names) {
        this.buildingTypeNames.clear();
        this.buildingTypeNames.addAll(names);
    }

    public boolean isBuildingAwakened(int buildingIndex) {
        return awakenedBuildings.contains(buildingIndex);
    }

    public void setBuildingAwakened(int buildingIndex) {
        awakenedBuildings.add(buildingIndex);
        if (awakenedBuildings.size() >= buildingCenters.size()) {
            this.awakened = true;
        }
    }

    public String getLabel() {
        return type.getDisplayName() + " " + tier.getStars();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", id);
        tag.putInt("x", origin.getX());
        tag.putInt("y", origin.getY());
        tag.putInt("z", origin.getZ());
        tag.putInt("tier", tier.getTier());
        tag.putString("type", type.name());
        tag.putBoolean("cleared", cleared);
        tag.putBoolean("awakened", awakened);
        tag.putInt("zombiesRemaining", zombiesRemaining);

        ListTag buildingsTag = new ListTag();
        for (BlockPos pos : buildingCenters) {
            CompoundTag bTag = new CompoundTag();
            bTag.putInt("bx", pos.getX());
            bTag.putInt("by", pos.getY());
            bTag.putInt("bz", pos.getZ());
            buildingsTag.add(bTag);
        }
        tag.put("buildings", buildingsTag);

        ListTag buildingTypesTag = new ListTag();
        for (String name : buildingTypeNames) {
            buildingTypesTag.add(StringTag.valueOf(name));
        }
        tag.put("buildingTypeNames", buildingTypesTag);

        int[] awakenedArr = awakenedBuildings.stream().mapToInt(Integer::intValue).toArray();
        tag.putIntArray("awakenedBuildings", awakenedArr);

        return tag;
    }

    public static TerritoryRecord load(CompoundTag tag) {
        int id = tag.getInt("id");
        BlockPos origin = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        TerritoryTier tier = TerritoryTier.fromNumber(tag.getInt("tier"));
        TerritoryType type;
        try {
            type = TerritoryType.valueOf(tag.getString("type"));
        } catch (IllegalArgumentException e) {
            type = TerritoryType.RESIDENTIAL;
        }
        TerritoryRecord record = new TerritoryRecord(id, origin, tier, type);
        record.cleared = tag.getBoolean("cleared");
        record.awakened = tag.getBoolean("awakened");
        record.zombiesRemaining = tag.getInt("zombiesRemaining");

        if (tag.contains("buildings", Tag.TAG_LIST)) {
            ListTag buildingsTag = tag.getList("buildings", Tag.TAG_COMPOUND);
            for (int i = 0; i < buildingsTag.size(); i++) {
                CompoundTag bTag = buildingsTag.getCompound(i);
                record.buildingCenters.add(new BlockPos(
                        bTag.getInt("bx"), bTag.getInt("by"), bTag.getInt("bz")));
            }
        }

        if (tag.contains("buildingTypeNames", Tag.TAG_LIST)) {
            ListTag typesTag = tag.getList("buildingTypeNames", Tag.TAG_STRING);
            for (int i = 0; i < typesTag.size(); i++) {
                record.buildingTypeNames.add(typesTag.getString(i));
            }
        }

        if (tag.contains("awakenedBuildings")) {
            int[] arr = tag.getIntArray("awakenedBuildings");
            for (int idx : arr) {
                record.awakenedBuildings.add(idx);
            }
        }

        return record;
    }
}
