package com.sevendaystominecraft.group;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class GroupData extends SavedData {

    private static final String DATA_NAME = SevenDaysToMinecraft.MOD_ID + "_groups";

    private final Map<UUID, Group> groups = new HashMap<>();
    private final Map<UUID, UUID> playerToGroup = new HashMap<>();
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public GroupData() {}

    public static GroupData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(GroupData::new, GroupData::load),
                DATA_NAME
        );
    }

    public Group createGroup(UUID ownerUUID, String ownerName) {
        if (playerToGroup.containsKey(ownerUUID)) return null;
        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, ownerUUID, ownerName);
        groups.put(groupId, group);
        playerToGroup.put(ownerUUID, groupId);
        setDirty();
        return group;
    }

    public boolean invite(UUID inviterUUID, UUID targetUUID) {
        UUID groupId = playerToGroup.get(inviterUUID);
        if (groupId == null) return false;
        if (playerToGroup.containsKey(targetUUID)) return false;
        pendingInvites.put(targetUUID, groupId);
        setDirty();
        return true;
    }

    public Group acceptInvite(UUID playerUUID, String playerName) {
        if (playerToGroup.containsKey(playerUUID)) return null;
        UUID groupId = pendingInvites.remove(playerUUID);
        if (groupId == null) return null;
        Group group = groups.get(groupId);
        if (group == null) return null;
        group.addMember(playerUUID, playerName);
        playerToGroup.put(playerUUID, groupId);
        setDirty();
        return group;
    }

    public boolean declineInvite(UUID playerUUID) {
        boolean had = pendingInvites.remove(playerUUID) != null;
        if (had) setDirty();
        return had;
    }

    public boolean hasPendingInvite(UUID playerUUID) {
        return pendingInvites.containsKey(playerUUID);
    }

    public Group leaveGroup(UUID playerUUID) {
        UUID groupId = playerToGroup.remove(playerUUID);
        if (groupId == null) return null;
        Group group = groups.get(groupId);
        if (group == null) return null;
        if (group.getOwnerUUID().equals(playerUUID)) {
            disbandGroup(groupId);
            setDirty();
            return null;
        }
        group.removeMember(playerUUID);
        setDirty();
        return group;
    }

    public Set<UUID> disbandGroup(UUID groupId) {
        Group group = groups.remove(groupId);
        if (group == null) return Collections.emptySet();
        Set<UUID> allMembers = new HashSet<>();
        allMembers.add(group.getOwnerUUID());
        for (Group.Member m : group.getMembers()) {
            allMembers.add(m.uuid());
        }
        for (UUID uuid : allMembers) {
            playerToGroup.remove(uuid);
        }
        pendingInvites.values().removeIf(gid -> gid.equals(groupId));
        setDirty();
        return allMembers;
    }

    public Group disbandGroupByOwner(UUID ownerUUID) {
        UUID groupId = playerToGroup.get(ownerUUID);
        if (groupId == null) return null;
        Group group = groups.get(groupId);
        if (group == null) return null;
        if (!group.getOwnerUUID().equals(ownerUUID)) return null;
        disbandGroup(groupId);
        return group;
    }

    public Group getGroupForPlayer(UUID playerUUID) {
        UUID groupId = playerToGroup.get(playerUUID);
        if (groupId == null) return null;
        return groups.get(groupId);
    }

    public boolean isInGroup(UUID playerUUID) {
        return playerToGroup.containsKey(playerUUID);
    }

    public Set<UUID> getGroupMemberUUIDs(UUID playerUUID) {
        Group group = getGroupForPlayer(playerUUID);
        if (group == null) return Collections.emptySet();
        Set<UUID> result = new HashSet<>();
        result.add(group.getOwnerUUID());
        for (Group.Member m : group.getMembers()) {
            result.add(m.uuid());
        }
        result.remove(playerUUID);
        return result;
    }

    public static GroupData load(CompoundTag tag, HolderLookup.Provider provider) {
        GroupData data = new GroupData();
        ListTag groupList = tag.getList("Groups", Tag.TAG_COMPOUND);
        for (int i = 0; i < groupList.size(); i++) {
            CompoundTag groupTag = groupList.getCompound(i);
            UUID groupId = groupTag.getUUID("GroupId");
            UUID ownerUUID = groupTag.getUUID("Owner");
            String ownerName = groupTag.getString("OwnerName");
            Group group = new Group(groupId, ownerUUID, ownerName);
            data.playerToGroup.put(ownerUUID, groupId);

            ListTag memberList = groupTag.getList("Members", Tag.TAG_COMPOUND);
            for (int j = 0; j < memberList.size(); j++) {
                CompoundTag memberTag = memberList.getCompound(j);
                UUID memberUUID = memberTag.getUUID("UUID");
                String memberName = memberTag.getString("Name");
                group.addMember(memberUUID, memberName);
                data.playerToGroup.put(memberUUID, groupId);
            }
            data.groups.put(groupId, group);
        }

        if (tag.contains("Invites")) {
            ListTag inviteList = tag.getList("Invites", Tag.TAG_COMPOUND);
            for (int i = 0; i < inviteList.size(); i++) {
                CompoundTag inviteTag = inviteList.getCompound(i);
                UUID playerUUID = inviteTag.getUUID("Player");
                UUID groupId = inviteTag.getUUID("Group");
                if (data.groups.containsKey(groupId)) {
                    data.pendingInvites.put(playerUUID, groupId);
                }
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag groupList = new ListTag();
        for (Group group : groups.values()) {
            CompoundTag groupTag = new CompoundTag();
            groupTag.putUUID("GroupId", group.getGroupId());
            groupTag.putUUID("Owner", group.getOwnerUUID());
            groupTag.putString("OwnerName", group.getOwnerName());
            ListTag memberList = new ListTag();
            for (Group.Member m : group.getMembers()) {
                CompoundTag memberTag = new CompoundTag();
                memberTag.putUUID("UUID", m.uuid());
                memberTag.putString("Name", m.name());
                memberList.add(memberTag);
            }
            groupTag.put("Members", memberList);
            groupList.add(groupTag);
        }
        tag.put("Groups", groupList);

        ListTag inviteList = new ListTag();
        for (Map.Entry<UUID, UUID> entry : pendingInvites.entrySet()) {
            CompoundTag inviteTag = new CompoundTag();
            inviteTag.putUUID("Player", entry.getKey());
            inviteTag.putUUID("Group", entry.getValue());
            inviteList.add(inviteTag);
        }
        tag.put("Invites", inviteList);
        return tag;
    }
}
