package com.sevendaystominecraft.smell;

import net.minecraft.world.phys.Vec3;

public class ScentTrail {

    private final Vec3[] positions;
    private int head;
    private int count;

    public ScentTrail(int capacity) {
        this.positions = new Vec3[capacity];
        this.head = 0;
        this.count = 0;
    }

    public void record(Vec3 pos) {
        positions[head] = pos;
        head = (head + 1) % positions.length;
        if (count < positions.length) count++;
    }

    public int size() {
        return count;
    }

    public Vec3 get(int index) {
        if (index < 0 || index >= count) return null;
        int actualIndex = (head - count + index + positions.length) % positions.length;
        return positions[actualIndex];
    }

    public int getNearestIndex(Vec3 from) {
        int nearestIdx = -1;
        double nearestDist = Double.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            Vec3 pos = get(i);
            if (pos == null) continue;
            double dist = from.distanceToSqr(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearestIdx = i;
            }
        }
        return nearestIdx;
    }

    public Vec3 getLatest() {
        if (count == 0) return null;
        return get(count - 1);
    }
}
