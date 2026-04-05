package com.sevendaystominecraft.client;

import com.sevendaystominecraft.network.SyncTerritoryPayload.TerritoryEntry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TerritoryClientState {

    private static final List<TerritoryEntry> territories = new CopyOnWriteArrayList<>();

    public static void update(List<TerritoryEntry> entries) {
        territories.clear();
        territories.addAll(entries);
    }

    public static List<TerritoryEntry> getTerritories() {
        return Collections.unmodifiableList(territories);
    }

    public static void clear() {
        territories.clear();
    }

    public static void reset() {
        territories.clear();
    }
}
