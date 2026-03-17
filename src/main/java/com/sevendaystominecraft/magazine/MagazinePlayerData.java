package com.sevendaystominecraft.magazine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public class MagazinePlayerData {

    private final Map<String, Set<Integer>> readIssues = new HashMap<>();

    public boolean hasRead(String seriesId, int issue) {
        Set<Integer> issues = readIssues.get(seriesId);
        return issues != null && issues.contains(issue);
    }

    public boolean markRead(String seriesId, int issue) {
        return readIssues.computeIfAbsent(seriesId, k -> new HashSet<>()).add(issue);
    }

    public boolean hasCompletedSeries(String seriesId) {
        MagazineSeries series = MagazineRegistry.getSeries(seriesId);
        if (series == null) return false;
        Set<Integer> issues = readIssues.get(seriesId);
        if (issues == null) return false;
        for (int i = 1; i <= series.issueCount(); i++) {
            if (!issues.contains(i)) return false;
        }
        return true;
    }

    public int getReadCount(String seriesId) {
        Set<Integer> issues = readIssues.get(seriesId);
        return issues == null ? 0 : issues.size();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Set<Integer>> entry : readIssues.entrySet()) {
            ListTag issueList = new ListTag();
            for (int issue : entry.getValue()) {
                issueList.add(StringTag.valueOf(String.valueOf(issue)));
            }
            tag.put(entry.getKey(), issueList);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        readIssues.clear();
        for (String key : tag.getAllKeys()) {
            Set<Integer> issues = new HashSet<>();
            ListTag list = tag.getList(key, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    issues.add(Integer.parseInt(list.getString(i)));
                } catch (NumberFormatException ignored) {}
            }
            readIssues.put(key, issues);
        }
    }

    public void copyFrom(MagazinePlayerData other) {
        readIssues.clear();
        for (Map.Entry<String, Set<Integer>> entry : other.readIssues.entrySet()) {
            readIssues.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }
}
