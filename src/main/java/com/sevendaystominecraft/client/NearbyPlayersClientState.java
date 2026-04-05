package com.sevendaystominecraft.client;

import com.sevendaystominecraft.network.SyncNearbyPlayersPayload.NearbyPlayerEntry;

import java.util.Collections;
import java.util.List;

public class NearbyPlayersClientState {

    private static volatile List<NearbyPlayerEntry> nearbyPlayers = Collections.emptyList();

    public static List<NearbyPlayerEntry> getNearbyPlayers() {
        return nearbyPlayers;
    }

    public static void update(List<NearbyPlayerEntry> players) {
        nearbyPlayers = List.copyOf(players);
    }

    public static void reset() {
        nearbyPlayers = Collections.emptyList();
    }
}
