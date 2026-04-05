package com.sevendaystominecraft.quest;

public enum QuestType {
    CLEAR_TERRITORY("Clear Territory"),
    FETCH_DELIVER("Fetch/Deliver"),
    KILL_COUNT("Kill Count"),
    BURIED_TREASURE("Buried Treasure");

    private final String displayName;

    QuestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
