package com.sevendaystominecraft.client;

public class ChunkHeatClientState {

    private static float currentChunkHeat = 0f;

    public static float getCurrentChunkHeat() {
        return currentChunkHeat;
    }

    public static void update(float heat) {
        currentChunkHeat = heat;
    }

    public static void reset() {
        currentChunkHeat = 0f;
    }
}
