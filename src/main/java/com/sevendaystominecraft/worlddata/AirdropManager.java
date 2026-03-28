package com.sevendaystominecraft.worlddata;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class AirdropManager extends SavedData {

    private static final String DATA_NAME = SevenDaysToMinecraft.MOD_ID + "_airdrop";

    private int spawnLootedCount = 0;
    private long ticksSinceLastAirdrop = 0;
    private boolean airdropsEnabled = false;

    public AirdropManager() { }

    public static AirdropManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(AirdropManager::new, AirdropManager::load),
                DATA_NAME
        );
    }

    public void onContainerLootedNearSpawn() {
        if (!airdropsEnabled) {
            spawnLootedCount++;
            setDirty();
            if (spawnLootedCount >= 50) {
                airdropsEnabled = true;
                SevenDaysToMinecraft.LOGGER.info("[BZHS] 50 containers fully looted near spawn. Airdrop Phase Initiated!");
            }
        }
    }

    public boolean areAirdropsEnabled() {
        return airdropsEnabled;
    }

    public long getTicksSinceLastAirdrop() {
        return ticksSinceLastAirdrop;
    }

    public void incrementTicks(long amount) {
        this.ticksSinceLastAirdrop += amount;
        setDirty();
    }

    public void resetTimer() {
        this.ticksSinceLastAirdrop = 0;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("SpawnLootedCount", this.spawnLootedCount);
        tag.putLong("TicksSinceLastAirdrop", this.ticksSinceLastAirdrop);
        tag.putBoolean("AirdropsEnabled", this.airdropsEnabled);
        return tag;
    }

    public static AirdropManager load(CompoundTag tag, HolderLookup.Provider registries) {
        AirdropManager data = new AirdropManager();
        data.spawnLootedCount = tag.getInt("SpawnLootedCount");
        data.ticksSinceLastAirdrop = tag.getLong("TicksSinceLastAirdrop");
        data.airdropsEnabled = tag.getBoolean("AirdropsEnabled");
        return data;
    }
}
