package com.sevendaystominecraft.heatmap;

import net.minecraft.nbt.CompoundTag;

public class HeatSource {

    private float amount;
    private final float decayPerMinute;
    private final int radiusChunks;

    public HeatSource(float amount, float decayPerMinute, int radiusChunks) {
        this.amount = amount;
        this.decayPerMinute = decayPerMinute;
        this.radiusChunks = radiusChunks;
    }

    public float getAmount() {
        return amount;
    }

    public float getDecayPerMinute() {
        return decayPerMinute;
    }

    public int getRadiusChunks() {
        return radiusChunks;
    }

    public void decay(float decayMultiplier) {
        float decayPerSecond = (decayPerMinute * decayMultiplier) / 60.0f;
        amount = Math.max(0, amount - decayPerSecond);
    }

    public boolean isDepleted() {
        return amount <= 0.001f;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("amount", amount);
        tag.putFloat("decayPerMinute", decayPerMinute);
        tag.putInt("radiusChunks", radiusChunks);
        return tag;
    }

    public static HeatSource load(CompoundTag tag) {
        return new HeatSource(
                tag.getFloat("amount"),
                tag.getFloat("decayPerMinute"),
                tag.getInt("radiusChunks")
        );
    }
}
