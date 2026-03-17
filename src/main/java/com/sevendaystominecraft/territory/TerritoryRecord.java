package com.sevendaystominecraft.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class TerritoryRecord {

    private final int id;
    private final BlockPos origin;
    private final TerritoryTier tier;
    private final TerritoryType type;
    private boolean cleared;
    private int zombiesRemaining;

    public TerritoryRecord(int id, BlockPos origin, TerritoryTier tier, TerritoryType type) {
        this.id = id;
        this.origin = origin;
        this.tier = tier;
        this.type = type;
        this.cleared = false;
        this.zombiesRemaining = 0;
    }

    public int getId() { return id; }
    public BlockPos getOrigin() { return origin; }
    public TerritoryTier getTier() { return tier; }
    public TerritoryType getType() { return type; }
    public boolean isCleared() { return cleared; }
    public int getZombiesRemaining() { return zombiesRemaining; }

    public void setCleared(boolean cleared) { this.cleared = cleared; }
    public void setZombiesRemaining(int count) { this.zombiesRemaining = count; }
    public void decrementZombies() {
        zombiesRemaining = Math.max(0, zombiesRemaining - 1);
        if (zombiesRemaining == 0 && !cleared) {
            cleared = true;
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
        tag.putInt("zombiesRemaining", zombiesRemaining);
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
        record.zombiesRemaining = tag.getInt("zombiesRemaining");
        return record;
    }
}
