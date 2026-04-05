package com.sevendaystominecraft.entity.zombie;

public enum DetectionState {
    UNAWARE(0),
    SUSPICIOUS(1),
    ALERT(2);

    private final int id;

    DetectionState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static DetectionState fromId(int id) {
        return switch (id) {
            case 1 -> SUSPICIOUS;
            case 2 -> ALERT;
            default -> UNAWARE;
        };
    }
}
