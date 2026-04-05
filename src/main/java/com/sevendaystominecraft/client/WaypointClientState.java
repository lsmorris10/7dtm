package com.sevendaystominecraft.client;

import com.sevendaystominecraft.network.SyncWaypointsPayload.WaypointData;

import java.util.Collections;
import java.util.List;

public class WaypointClientState {

    private static volatile List<WaypointData> waypoints = Collections.emptyList();

    public static List<WaypointData> getWaypoints() {
        return waypoints;
    }

    public static void update(List<WaypointData> newWaypoints) {
        waypoints = List.copyOf(newWaypoints);
    }

    public static void reset() {
        waypoints = Collections.emptyList();
    }
}
