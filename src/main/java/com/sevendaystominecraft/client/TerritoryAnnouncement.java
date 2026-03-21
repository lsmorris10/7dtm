package com.sevendaystominecraft.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashSet;
import java.util.Set;

public class TerritoryAnnouncement {

    public enum Phase { NONE, HOLD, ANIMATE, DONE }

    private static final int HOLD_TICKS = 60;
    private static final int ANIMATE_TICKS = 20;

    private static Phase phase = Phase.NONE;
    private static int ticksInPhase = 0;
    private static String territoryName = "";
    private static int territoryTier = 1;
    private static final Set<Integer> announcedTerritoryIds = new HashSet<>();

    public static void trigger(String name, int tier, int territoryEntityId) {
        if (announcedTerritoryIds.contains(territoryEntityId)) return;
        announcedTerritoryIds.add(territoryEntityId);
        territoryName = name;
        territoryTier = tier;
        phase = Phase.HOLD;
        ticksInPhase = 0;
    }

    public static void tick() {
        if (phase == Phase.NONE || phase == Phase.DONE) return;

        ticksInPhase++;

        if (phase == Phase.HOLD && ticksInPhase >= HOLD_TICKS) {
            phase = Phase.ANIMATE;
            ticksInPhase = 0;
        } else if (phase == Phase.ANIMATE && ticksInPhase >= ANIMATE_TICKS) {
            phase = Phase.DONE;
            ticksInPhase = 0;
        }
    }

    public static boolean isActive() {
        return phase == Phase.HOLD || phase == Phase.ANIMATE;
    }

    public static Phase getPhase() {
        return phase;
    }

    public static int getTicksInPhase() {
        return ticksInPhase;
    }

    public static String getTerritoryName() {
        return territoryName;
    }

    public static int getTerritoryTier() {
        return territoryTier;
    }

    public static float getAnimateProgress() {
        if (phase != Phase.ANIMATE) return 0f;
        return (float) ticksInPhase / ANIMATE_TICKS;
    }

    public static int getHoldTicks() {
        return HOLD_TICKS;
    }

    public static int getAnimateTicks() {
        return ANIMATE_TICKS;
    }

    public static boolean hasBeenAnnounced(int territoryEntityId) {
        return announcedTerritoryIds.contains(territoryEntityId);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        tick();
    }

    public static void reset() {
        phase = Phase.NONE;
        ticksInPhase = 0;
        territoryName = "";
        territoryTier = 1;
        announcedTerritoryIds.clear();
    }
}
