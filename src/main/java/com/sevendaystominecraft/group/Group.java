package com.sevendaystominecraft.group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {

    public record Member(UUID uuid, String name) {}

    private final UUID groupId;
    private final UUID ownerUUID;
    private final String ownerName;
    private final List<Member> members = new ArrayList<>();

    public Group(UUID groupId, UUID ownerUUID, String ownerName) {
        this.groupId = groupId;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
    }

    public UUID getGroupId() { return groupId; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public List<Member> getMembers() { return members; }

    public void addMember(UUID uuid, String name) {
        if (!uuid.equals(ownerUUID) && members.stream().noneMatch(m -> m.uuid().equals(uuid))) {
            members.add(new Member(uuid, name));
        }
    }

    public void removeMember(UUID uuid) {
        members.removeIf(m -> m.uuid().equals(uuid));
    }

    public boolean containsPlayer(UUID uuid) {
        if (ownerUUID.equals(uuid)) return true;
        return members.stream().anyMatch(m -> m.uuid().equals(uuid));
    }

    public List<String> getAllMemberNames() {
        List<String> names = new ArrayList<>();
        names.add(ownerName);
        for (Member m : members) {
            names.add(m.name());
        }
        return names;
    }
}
