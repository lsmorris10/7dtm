package com.sevendaystominecraft.group;

import net.minecraft.nbt.CompoundTag;

public record WaypointEntry(String name, int x, int z) {

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", name);
        tag.putInt("X", x);
        tag.putInt("Z", z);
        return tag;
    }

    public static WaypointEntry load(CompoundTag tag) {
        return new WaypointEntry(
                tag.getString("Name"),
                tag.getInt("X"),
                tag.getInt("Z")
        );
    }
}
