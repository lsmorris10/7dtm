package com.sevendaystominecraft.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Client-side state for the progression tree, populated by SyncProgressionPayload.
 */
public class ProgressionClientState {

    private static final Set<String> completedNodes = new HashSet<>();
    private static int highestCompletedStage = 0;

    public static void update(Set<String> ids, int stage) {
        completedNodes.clear();
        completedNodes.addAll(ids);
        highestCompletedStage = stage;
    }

    public static Set<String> getCompletedNodes() {
        return Collections.unmodifiableSet(completedNodes);
    }

    public static boolean isNodeCompleted(String nodeId) {
        return completedNodes.contains(nodeId);
    }

    public static int getCompletedCount() {
        return completedNodes.size();
    }

    public static int getHighestCompletedStage() {
        return highestCompletedStage;
    }

    public static void reset() {
        completedNodes.clear();
        highestCompletedStage = 0;
    }
}
