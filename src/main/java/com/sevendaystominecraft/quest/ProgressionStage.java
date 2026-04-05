package com.sevendaystominecraft.quest;

public enum ProgressionStage {
    SCAVENGER(1, "Scavenger", "Learn to survive"),
    SURVIVOR(2, "Survivor", "Establish your foothold"),
    ESTABLISHED(3, "Established", "The iron age begins"),
    FORTIFIED(4, "Fortified", "Fortify and arm up"),
    SELF_SUFFICIENT(5, "Self-Sufficient", "Master of the wasteland");

    private final int number;
    private final String displayName;
    private final String subtitle;

    ProgressionStage(int number, String displayName, String subtitle) {
        this.number = number;
        this.displayName = displayName;
        this.subtitle = subtitle;
    }

    public int getNumber() { return number; }
    public String getDisplayName() { return displayName; }
    public String getSubtitle() { return subtitle; }

    public static ProgressionStage fromNumber(int number) {
        for (ProgressionStage stage : values()) {
            if (stage.number == number) return stage;
        }
        return SCAVENGER;
    }
}
