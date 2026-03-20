package com.sevendaystominecraft.capability;

import java.util.Map;
import com.sevendaystominecraft.perk.Attribute;

public interface ISevenDaysPlayerStats {

    // ── Food (Fullness) ─────────────────────────────────────────────────
    float getFood();
    void setFood(float value);
    float getMaxFood();
    void setMaxFood(float value);

    // ── Water (Hydration) ───────────────────────────────────────────────
    float getWater();
    void setWater(float value);
    float getMaxWater();
    void setMaxWater(float value);

    // ── Stamina ─────────────────────────────────────────────────────────
    float getStamina();
    void setStamina(float value);
    float getMaxStamina();
    void setMaxStamina(float value);

    boolean isStaminaExhausted();
    void setStaminaExhausted(boolean exhausted);

    // ── Core Temperature ────────────────────────────────────────────────
    float getCoreTemperature();
    void setCoreTemperature(float value);

    // ── Debuffs ─────────────────────────────────────────────────────────
    Map<String, Integer> getDebuffs();
    void addDebuff(String debuffId, int durationTicks);
    void removeDebuff(String debuffId);
    boolean hasDebuff(String debuffId);
    void tickDebuffs();

    // ── XP & Leveling (§1.4) ────────────────────────────────────────────
    int getXp();
    void setXp(int xp);
    void addXp(int amount);

    int getLevel();
    void setLevel(int level);

    int getPerkPoints();
    void setPerkPoints(int points);
    void addPerkPoints(int amount);

    int getAttributePoints();
    void setAttributePoints(int points);
    void addAttributePoints(int amount);

    // ── Attributes (§5.1) ───────────────────────────────────────────────
    int getAttributeLevel(Attribute attribute);
    void setAttributeLevel(Attribute attribute, int level);

    // ── Perks (§5.2) ────────────────────────────────────────────────────
    Map<String, Integer> getActivePerks();
    int getPerkRank(String perkId);
    void setPerkRank(String perkId, int rank);

    // ── Temperature Exposure Timers ─────────────────────────────────────
    int getColdExposureTicks();
    void setColdExposureTicks(int ticks);
    int getHeatExposureTicks();
    void setHeatExposureTicks(int ticks);

    // ── Unkillable Mastery Cooldown ─────────────────────────────────────
    long getUnkillableCooldownEnd();
    void setUnkillableCooldownEnd(long worldTimeTick);

    // ── Utility ─────────────────────────────────────────────────────────
    void copyFrom(ISevenDaysPlayerStats other);
}
