package com.sevendaystominecraft.client;

import com.sevendaystominecraft.network.SyncTraderPayload.TraderEntry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TraderClientState {

    private static final List<TraderEntry> traders = new CopyOnWriteArrayList<>();

    public static void update(List<TraderEntry> entries) {
        traders.clear();
        traders.addAll(entries);
    }

    public static List<TraderEntry> getTraders() {
        return Collections.unmodifiableList(traders);
    }

    public static void clear() {
        traders.clear();
    }

    public static void reset() {
        traders.clear();
    }
}
