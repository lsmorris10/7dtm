package com.sevendaystominecraft.stealth;

import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NoiseManager {

    public static final float NOISE_GUNSHOT = 80.0f;
    public static final float NOISE_EXPLOSION = 100.0f;
    public static final float NOISE_SPRINTING = 15.0f;
    public static final float NOISE_WALKING = 5.0f;
    public static final float NOISE_CROUCHING = 1.0f;
    public static final float NOISE_BLOCK_BREAK = 10.0f;

    private static final Map<UUID, List<NoiseEvent>> NOISE_EVENTS = new ConcurrentHashMap<>();

    public static void addNoise(UUID playerId, Vec3 position, float noiseLevel) {
        NOISE_EVENTS.computeIfAbsent(playerId, k -> new CopyOnWriteArrayList<>())
                .add(new NoiseEvent(position, noiseLevel, System.currentTimeMillis()));
    }

    public static float getCurrentNoise(UUID playerId) {
        List<NoiseEvent> events = NOISE_EVENTS.get(playerId);
        if (events == null || events.isEmpty()) return 0.0f;

        long now = System.currentTimeMillis();
        float totalNoise = 0.0f;

        Iterator<NoiseEvent> it = events.iterator();
        while (it.hasNext()) {
            NoiseEvent event = it.next();
            long age = now - event.timestamp();
            if (age > 5000) {
                events.remove(event);
                continue;
            }
            float decay = 1.0f - (age / 5000.0f);
            totalNoise += event.noiseLevel() * decay;
        }

        return totalNoise;
    }

    public static Vec3 getLoudestNoisePosition(UUID playerId) {
        List<NoiseEvent> events = NOISE_EVENTS.get(playerId);
        if (events == null || events.isEmpty()) return null;

        long now = System.currentTimeMillis();
        float loudest = 0.0f;
        Vec3 loudestPos = null;

        for (NoiseEvent event : events) {
            long age = now - event.timestamp();
            if (age > 5000) continue;
            float decay = 1.0f - (age / 5000.0f);
            float effective = event.noiseLevel() * decay;
            if (effective > loudest) {
                loudest = effective;
                loudestPos = event.position();
            }
        }

        return loudestPos;
    }

    public static void clearPlayer(UUID playerId) {
        NOISE_EVENTS.remove(playerId);
    }

    public static void clearAll() {
        NOISE_EVENTS.clear();
    }

    public record NoiseEvent(Vec3 position, float noiseLevel, long timestamp) {}
}
