package com.sevendaystominecraft.block.building;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LandClaimData extends SavedData {

    private static final String DATA_NAME = SevenDaysToMinecraft.MOD_ID + "_land_claims";

    private final Map<UUID, BlockPos> playerClaims = new HashMap<>();

    public LandClaimData() {}

    public static LandClaimData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(LandClaimData::new, LandClaimData::load),
                DATA_NAME
        );
    }

    public void setClaim(UUID playerId, BlockPos pos) {
        playerClaims.put(playerId, pos);
        setDirty();
    }

    public void removeClaim(UUID playerId) {
        playerClaims.remove(playerId);
        setDirty();
    }

    public void removeClaimAtPos(BlockPos pos) {
        playerClaims.entrySet().removeIf(entry -> entry.getValue().equals(pos));
        setDirty();
    }

    public BlockPos getClaim(UUID playerId) {
        return playerClaims.get(playerId);
    }

    public Map<UUID, BlockPos> getAllClaims() {
        return playerClaims;
    }

    public boolean isWithinAnyClaimRadius(BlockPos pos, int radius) {
        for (BlockPos claimPos : playerClaims.values()) {
            double dx = pos.getX() - claimPos.getX();
            double dy = pos.getY() - claimPos.getY();
            double dz = pos.getZ() - claimPos.getZ();
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, BlockPos> entry : playerClaims.entrySet()) {
            CompoundTag claimTag = new CompoundTag();
            claimTag.putUUID("player", entry.getKey());
            claimTag.putInt("x", entry.getValue().getX());
            claimTag.putInt("y", entry.getValue().getY());
            claimTag.putInt("z", entry.getValue().getZ());
            list.add(claimTag);
        }
        tag.put("claims", list);
        return tag;
    }

    public static LandClaimData load(CompoundTag tag, HolderLookup.Provider registries) {
        LandClaimData data = new LandClaimData();
        ListTag list = tag.getList("claims", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag claimTag = list.getCompound(i);
            UUID playerId = claimTag.getUUID("player");
            BlockPos pos = new BlockPos(claimTag.getInt("x"), claimTag.getInt("y"), claimTag.getInt("z"));
            data.playerClaims.put(playerId, pos);
        }
        return data;
    }
}
