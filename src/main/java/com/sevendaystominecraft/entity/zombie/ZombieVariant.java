package com.sevendaystominecraft.entity.zombie;

public enum ZombieVariant {
    WALKER(20.0, 3.0, 1.0, 200, 1),
    CRAWLER(14.0, 2.5, 0.8, 150, 1),
    FROZEN_LUMBERJACK(30.0, 4.0, 0.9, 250, 1),
    BLOATED_WALKER(40.0, 4.5, 0.7, 300, 3),
    SPIDER_ZOMBIE(24.0, 3.5, 1.8, 300, 5),
    FERAL_WIGHT(60.0, 6.0, 2.5, 350, 7),
    COP(70.0, 5.0, 1.2, 400, 14),
    SCREAMER(16.0, 2.0, 1.5, 250, 7),
    ZOMBIE_DOG(16.0, 4.0, 3.5, 200, 3),
    VULTURE(12.0, 3.0, 4.0, 200, 7),
    DEMOLISHER(160.0, 8.0, 1.0, 800, 21),
    MUTATED_CHUCK(50.0, 5.0, 1.3, 350, 14),
    ZOMBIE_BEAR(120.0, 7.0, 2.0, 500, 14),
    NURSE(24.0, 3.0, 1.0, 250, 7),
    SOLDIER(80.0, 6.0, 1.5, 400, 14),
    CHARGED(0, 0, 0, 550, 21),
    INFERNAL(0, 0, 0, 550, 21),
    RADIATED(0, 0, 0, 500, 28),
    BEHEMOTH(400.0, 12.0, 0.8, 2000, 35);

    private final double baseHP;
    private final double baseDamage;
    private final double baseSpeed;
    private final int xpReward;
    private final int minSpawnDay;

    ZombieVariant(double baseHP, double baseDamage, double baseSpeed, int xpReward, int minSpawnDay) {
        this.baseHP = baseHP;
        this.baseDamage = baseDamage;
        this.baseSpeed = baseSpeed;
        this.xpReward = xpReward;
        this.minSpawnDay = minSpawnDay;
    }

    public double getBaseHP() { return baseHP; }
    public double getBaseDamage() { return baseDamage; }
    public double getBaseSpeed() { return baseSpeed; }
    public int getXpReward() { return xpReward; }
    public int getMinSpawnDay() { return minSpawnDay; }

    public boolean isModifier() {
        return this == CHARGED || this == INFERNAL || this == RADIATED;
    }

    public double getModifierHPMultiplier() {
        return switch (this) {
            case CHARGED -> 1.8;
            case INFERNAL -> 1.8;
            case RADIATED -> 2.0;
            default -> 1.0;
        };
    }

    public double getModifierDamageMultiplier() {
        return switch (this) {
            case CHARGED -> 1.3;
            case INFERNAL -> 1.4;
            case RADIATED -> 1.5;
            default -> 1.0;
        };
    }

    public double getModifierSpeedMultiplier() {
        return switch (this) {
            case CHARGED -> 1.2;
            case INFERNAL -> 1.1;
            case RADIATED -> 1.3;
            default -> 1.0;
        };
    }
}
