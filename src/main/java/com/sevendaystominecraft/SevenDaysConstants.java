package com.sevendaystominecraft;

public final class SevenDaysConstants {

    private SevenDaysConstants() {}

    public static final int TIME_SCALE = 2;

    public static final long DAY_LENGTH = 24000L;

    public static final long NIGHT_START = 13000L;

    public static final long NIGHT_END = 23000L;

    public static final long REAL_TICKS_PER_DAY = DAY_LENGTH * TIME_SCALE;
}
