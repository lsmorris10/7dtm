package com.sevendaystominecraft.client;

import java.util.Collections;
import java.util.List;

/**
 * Client-side state that holds the current trader's dialogue lines.
 * Updated when the player opens a trader menu; the TraderScreen reads
 * these lines and renders them as a dialogue bubble.
 */
public class TraderDialogueClientState {

    private static List<String> currentDialogue = Collections.emptyList();

    public static void update(List<String> lines) {
        currentDialogue = List.copyOf(lines);
    }

    public static List<String> getDialogue() {
        return currentDialogue;
    }

    public static void clear() {
        currentDialogue = Collections.emptyList();
    }
}
