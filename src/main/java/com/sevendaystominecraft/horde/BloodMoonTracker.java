package com.sevendaystominecraft.horde;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.config.HordeConfig;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class BloodMoonTracker extends SavedData {

    public enum Phase {
        NONE,
        PREP,
        ACTIVE,
        POST,
    }

    private static final String DATA_NAME = SevenDaysToMinecraft.MOD_ID + "_blood_moon";

    private int gameDay;
    private Phase phase;
    private int currentWave;
    private int ticksUntilNextWave;
    private int lastKnownDay;
    private boolean sentWarning;
    private boolean sentSkyRed;
    private boolean sentSiren;
    private boolean startedHorde;
    private boolean spawnedFinalWave;
    private boolean burnedAtDawn;

    public BloodMoonTracker() {
        this.gameDay = 0;
        this.phase = Phase.NONE;
        this.currentWave = 0;
        this.ticksUntilNextWave = 0;
        this.lastKnownDay = -1;
        this.sentWarning = false;
        this.sentSkyRed = false;
        this.sentSiren = false;
        this.startedHorde = false;
        this.spawnedFinalWave = false;
        this.burnedAtDawn = false;
    }

    public static BloodMoonTracker load(CompoundTag tag, HolderLookup.Provider registries) {
        BloodMoonTracker tracker = new BloodMoonTracker();
        tracker.gameDay = tag.getInt("GameDay");
        tracker.phase = Phase.values()[Math.min(tag.getInt("Phase"), Phase.values().length - 1)];
        tracker.currentWave = tag.getInt("CurrentWave");
        tracker.ticksUntilNextWave = tag.getInt("TicksUntilNextWave");
        tracker.lastKnownDay = tag.getInt("LastKnownDay");
        tracker.sentWarning = tag.getBoolean("SentWarning");
        tracker.sentSkyRed = tag.getBoolean("SentSkyRed");
        tracker.sentSiren = tag.getBoolean("SentSiren");
        tracker.startedHorde = tag.getBoolean("StartedHorde");
        tracker.spawnedFinalWave = tag.getBoolean("SpawnedFinalWave");
        tracker.burnedAtDawn = tag.getBoolean("BurnedAtDawn");
        return tracker;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("GameDay", gameDay);
        tag.putInt("Phase", phase.ordinal());
        tag.putInt("CurrentWave", currentWave);
        tag.putInt("TicksUntilNextWave", ticksUntilNextWave);
        tag.putInt("LastKnownDay", lastKnownDay);
        tag.putBoolean("SentWarning", sentWarning);
        tag.putBoolean("SentSkyRed", sentSkyRed);
        tag.putBoolean("SentSiren", sentSiren);
        tag.putBoolean("StartedHorde", startedHorde);
        tag.putBoolean("SpawnedFinalWave", spawnedFinalWave);
        tag.putBoolean("BurnedAtDawn", burnedAtDawn);
        return tag;
    }

    public static BloodMoonTracker getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BloodMoonTracker::new, BloodMoonTracker::load),
                DATA_NAME
        );
    }

    public boolean isBloodMoonDay(int dayNumber) {
        if (dayNumber <= 0) return false;
        int cycle = HordeConfig.INSTANCE.hordeCycleLength.get();
        return dayNumber % cycle == 0;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
        setDirty();
    }

    public boolean isBloodMoonActive() {
        return phase == Phase.PREP || phase == Phase.ACTIVE || phase == Phase.POST;
    }

    public boolean isHordeSpawning() {
        return phase == Phase.ACTIVE;
    }

    public void startBloodMoon() {
        this.phase = Phase.ACTIVE;
        this.currentWave = 0;
        this.ticksUntilNextWave = 0;
        setDirty();
    }

    public void endBloodMoon() {
        this.phase = Phase.NONE;
        this.currentWave = 0;
        this.ticksUntilNextWave = 0;
        this.spawnedFinalWave = false;
        this.burnedAtDawn = false;
        setDirty();
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int wave) {
        this.currentWave = wave;
        setDirty();
    }

    public int getTicksUntilNextWave() {
        return ticksUntilNextWave;
    }

    public void setTicksUntilNextWave(int ticks) {
        this.ticksUntilNextWave = ticks;
        setDirty();
    }

    public void decrementWaveTimer() {
        if (ticksUntilNextWave > 0) {
            ticksUntilNextWave--;
            if (ticksUntilNextWave % 200 == 0) {
                setDirty();
            }
        }
    }

    public int getGameDay() {
        return gameDay;
    }

    public void setGameDay(int day) {
        this.gameDay = day;
        setDirty();
    }

    public int getLastKnownDay() {
        return lastKnownDay;
    }

    public void setLastKnownDay(int day) {
        this.lastKnownDay = day;
        setDirty();
    }

    public boolean hasSentWarning() { return sentWarning; }
    public void setSentWarning(boolean v) { sentWarning = v; setDirty(); }

    public boolean hasSentSkyRed() { return sentSkyRed; }
    public void setSentSkyRed(boolean v) { sentSkyRed = v; setDirty(); }

    public boolean hasSentSiren() { return sentSiren; }
    public void setSentSiren(boolean v) { sentSiren = v; setDirty(); }

    public boolean hasStartedHorde() { return startedHorde; }
    public void setStartedHorde(boolean v) { startedHorde = v; setDirty(); }

    public boolean hasSpawnedFinalWave() { return spawnedFinalWave; }
    public void setSpawnedFinalWave(boolean v) { spawnedFinalWave = v; setDirty(); }

    public boolean hasBurnedAtDawn() { return burnedAtDawn; }
    public void setBurnedAtDawn(boolean v) { burnedAtDawn = v; setDirty(); }

    public void resetDayFlags() {
        sentWarning = false;
        sentSkyRed = false;
        sentSiren = false;
        startedHorde = false;
        spawnedFinalWave = false;
        burnedAtDawn = false;
        setDirty();
    }

    public static int calculateGameDay(ServerLevel level) {
        return (int) (level.getDayTime() / SevenDaysConstants.DAY_LENGTH) + 1;
    }

    public static int getTimeOfDay(ServerLevel level) {
        return (int) (level.getDayTime() % SevenDaysConstants.DAY_LENGTH);
    }
}
