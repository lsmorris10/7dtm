package com.sevendaystominecraft.client;

public class BloodMoonClientState {

    private static boolean active = false;
    private static int currentWave = 0;
    private static int totalWaves = 0;
    private static int dayNumber = 0;

    public static boolean isActive() {
        return active;
    }

    public static int getCurrentWave() {
        return currentWave;
    }

    public static int getTotalWaves() {
        return totalWaves;
    }

    public static int getDayNumber() {
        return dayNumber;
    }

    public static void update(boolean active, int currentWave, int totalWaves, int dayNumber) {
        BloodMoonClientState.active = active;
        BloodMoonClientState.currentWave = currentWave;
        BloodMoonClientState.totalWaves = totalWaves;
        BloodMoonClientState.dayNumber = dayNumber;
    }

    public static void reset() {
        active = false;
        currentWave = 0;
        totalWaves = 0;
        dayNumber = 0;
    }
}
