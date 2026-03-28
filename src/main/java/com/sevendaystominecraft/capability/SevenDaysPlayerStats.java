package com.sevendaystominecraft.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sevendaystominecraft.magazine.MagazinePlayerData;
import com.sevendaystominecraft.perk.Attribute;
import com.sevendaystominecraft.quest.QuestInstance;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class SevenDaysPlayerStats implements ISevenDaysPlayerStats, INBTSerializable<CompoundTag> {

    private static final float DEFAULT_MAX_FOOD = 20.0f;
    private static final float DEFAULT_MAX_WATER = 20.0f;
    private static final float DEFAULT_MAX_STAMINA = 20.0f;
    private static final float DEFAULT_CORE_TEMP = 70.0f;

    private float food = DEFAULT_MAX_FOOD;
    private float maxFood = DEFAULT_MAX_FOOD;

    private float water = DEFAULT_MAX_WATER;
    private float maxWater = DEFAULT_MAX_WATER;

    private float stamina = DEFAULT_MAX_STAMINA;
    private float maxStamina = DEFAULT_MAX_STAMINA;

    private boolean staminaExhausted = false;

    private final MagazinePlayerData magazineData = new MagazinePlayerData();

    private float coreTemperature = DEFAULT_CORE_TEMP;

    private int coldExposureTicks = 0;
    private int heatExposureTicks = 0;

    private final Map<String, Integer> debuffs = new HashMap<>();

    

    private int xp = 0;
    private int level = 1;
    private int perkPoints = 0;
    private int attributePoints = 0;

    private final int[] attributeLevels = new int[Attribute.values().length];

    private final Map<String, Integer> activePerks = new HashMap<>();

    private long unkillableCooldownEnd = 0;

    private final Set<String> unlockedRecipes = new HashSet<>();

    private final List<QuestInstance> activeQuests = new ArrayList<>();
    private String trackedQuestId = "";
    private final Map<String, Long> consumedQuests = new HashMap<>();

    public SevenDaysPlayerStats() {
        for (int i = 0; i < attributeLevels.length; i++) {
            attributeLevels[i] = 1;
        }
    }

    // =====================================================================
    // ISevenDaysPlayerStats — survival stats
    // =====================================================================

    @Override public float getFood() { return food; }
    @Override public void setFood(float value) { this.food = clamp(value, 0f, maxFood); }
    @Override public float getMaxFood() { return maxFood; }
    @Override public void setMaxFood(float value) { this.maxFood = Math.max(0f, value); }

    @Override public float getWater() { return water; }
    @Override public void setWater(float value) { this.water = clamp(value, 0f, maxWater); }
    @Override public float getMaxWater() { return maxWater; }
    @Override public void setMaxWater(float value) { this.maxWater = Math.max(0f, value); }

    @Override public float getStamina() { return stamina; }
    @Override public void setStamina(float value) { this.stamina = clamp(value, 0f, maxStamina); }
    @Override public float getMaxStamina() { return maxStamina; }
    @Override public void setMaxStamina(float value) { this.maxStamina = Math.max(0f, value); }

    @Override public boolean isStaminaExhausted() { return staminaExhausted; }
    @Override public void setStaminaExhausted(boolean exhausted) { this.staminaExhausted = exhausted; }

    @Override public float getCoreTemperature() { return coreTemperature; }
    @Override public void setCoreTemperature(float value) { this.coreTemperature = value; }

    @Override public int getColdExposureTicks() { return coldExposureTicks; }
    @Override public void setColdExposureTicks(int ticks) { this.coldExposureTicks = Math.max(0, ticks); }
    @Override public int getHeatExposureTicks() { return heatExposureTicks; }
    @Override public void setHeatExposureTicks(int ticks) { this.heatExposureTicks = Math.max(0, ticks); }

    @Override
    public Map<String, Integer> getDebuffs() {
        return Collections.unmodifiableMap(debuffs);
    }

    @Override
    public void addDebuff(String debuffId, int durationTicks) {
        debuffs.merge(debuffId, durationTicks, Math::max);
    }

    @Override
    public void removeDebuff(String debuffId) {
        debuffs.remove(debuffId);
    }

    @Override
    public boolean hasDebuff(String debuffId) {
        return debuffs.containsKey(debuffId);
    }

    private boolean infection1Expired = false;

    public boolean isInfection1Expired() { return infection1Expired; }
    public void clearInfection1Expired() { infection1Expired = false; }

    @Override
    public void tickDebuffs() {
        infection1Expired = false;
        Iterator<Map.Entry<String, Integer>> it = debuffs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                if (entry.getKey().equals(DEBUFF_INFECTION_1)) {
                    infection1Expired = true;
                }
                it.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    // =====================================================================
    // ISevenDaysPlayerStats — XP & Leveling
    // =====================================================================

    @Override public int getXp() { return xp; }
    @Override public void setXp(int xp) { this.xp = Math.max(0, xp); }
    @Override public void addXp(int amount) { this.xp += Math.max(0, amount); }

    @Override public int getLevel() { return level; }
    @Override public void setLevel(int level) { this.level = Math.max(1, level); }

    @Override public int getPerkPoints() { return perkPoints; }
    @Override public void setPerkPoints(int points) { this.perkPoints = Math.max(0, points); }
    @Override public void addPerkPoints(int amount) { this.perkPoints += amount; }

    @Override public int getAttributePoints() { return attributePoints; }
    @Override public void setAttributePoints(int points) { this.attributePoints = Math.max(0, points); }
    @Override public void addAttributePoints(int amount) { this.attributePoints += amount; }

    // =====================================================================
    // ISevenDaysPlayerStats — Attributes
    // =====================================================================

    @Override
    public int getAttributeLevel(Attribute attribute) {
        return attributeLevels[attribute.ordinal()];
    }

    @Override
    public void setAttributeLevel(Attribute attribute, int level) {
        attributeLevels[attribute.ordinal()] = clampInt(level, 1, 10);
    }

    // =====================================================================
    // ISevenDaysPlayerStats — Perks
    // =====================================================================

    @Override
    public Map<String, Integer> getActivePerks() {
        return Collections.unmodifiableMap(activePerks);
    }

    @Override
    public int getPerkRank(String perkId) {
        return activePerks.getOrDefault(perkId, 0);
    }

    @Override
    public void setPerkRank(String perkId, int rank) {
        if (rank <= 0) {
            activePerks.remove(perkId);
        } else {
            activePerks.put(perkId, rank);
        }
    }

    // =====================================================================
    // Unkillable Cooldown
    // =====================================================================

    @Override public long getUnkillableCooldownEnd() { return unkillableCooldownEnd; }
    @Override public void setUnkillableCooldownEnd(long worldTimeTick) { this.unkillableCooldownEnd = worldTimeTick; }

    public MagazinePlayerData getMagazineData() { return magazineData; }

    // =====================================================================
    // Recipe Unlocks
    // =====================================================================

    public boolean unlockRecipe(String recipeId) {
        return unlockedRecipes.add(recipeId);
    }

    public boolean isRecipeUnlocked(String recipeId) {
        return unlockedRecipes.contains(recipeId);
    }

    public Set<String> getUnlockedRecipes() {
        return Collections.unmodifiableSet(unlockedRecipes);
    }

    // =====================================================================
    // Quests
    // =====================================================================

    public List<QuestInstance> getActiveQuests() {
        return activeQuests;
    }

    public QuestInstance getQuestById(String questId) {
        for (QuestInstance quest : activeQuests) {
            if (quest.getQuestId().equals(questId)) return quest;
        }
        return null;
    }

    public boolean addQuest(QuestInstance quest) {
        activeQuests.add(quest);
        return true;
    }

    public boolean removeQuest(String questId) {
        return activeQuests.removeIf(q -> q.getQuestId().equals(questId));
    }

    public String getTrackedQuestId() { return trackedQuestId; }
    public void setTrackedQuestId(String id) { this.trackedQuestId = id != null ? id : ""; }

    public void markQuestConsumed(String questGenId, long restockDay) {
        consumedQuests.put(questGenId, restockDay);
    }

    public boolean isQuestConsumed(String questGenId, long currentRestockDay) {
        Long consumedAt = consumedQuests.get(questGenId);
        if (consumedAt == null) return false;
        if (consumedAt < currentRestockDay) {
            consumedQuests.remove(questGenId);
            return false;
        }
        return true;
    }

    public Map<String, Long> getConsumedQuests() { return consumedQuests; }

    // =====================================================================
    // copyFrom
    // =====================================================================

    @Override
    public void copyFrom(ISevenDaysPlayerStats other) {
        this.food = other.getFood();
        this.maxFood = other.getMaxFood();
        this.water = other.getWater();
        this.maxWater = other.getMaxWater();
        this.stamina = other.getStamina();
        this.maxStamina = other.getMaxStamina();
        this.staminaExhausted = other.isStaminaExhausted();
        this.coreTemperature = other.getCoreTemperature();
        this.coldExposureTicks = other.getColdExposureTicks();
        this.heatExposureTicks = other.getHeatExposureTicks();
        this.debuffs.clear();
        this.debuffs.putAll(other.getDebuffs());

        this.xp = other.getXp();
        this.level = other.getLevel();
        this.perkPoints = other.getPerkPoints();
        this.attributePoints = other.getAttributePoints();

        for (Attribute attr : Attribute.values()) {
            this.setAttributeLevel(attr, other.getAttributeLevel(attr));
        }

        this.activePerks.clear();
        this.activePerks.putAll(other.getActivePerks());

        this.unkillableCooldownEnd = other.getUnkillableCooldownEnd();

        if (other instanceof SevenDaysPlayerStats concrete) {
            this.magazineData.copyFrom(concrete.getMagazineData());
            this.unlockedRecipes.clear();
            this.unlockedRecipes.addAll(concrete.unlockedRecipes);
            this.activeQuests.clear();
            for (QuestInstance quest : concrete.getActiveQuests()) {
                this.activeQuests.add(QuestInstance.load(quest.save()));
            }
            this.trackedQuestId = concrete.getTrackedQuestId();
            this.consumedQuests.clear();
            this.consumedQuests.putAll(concrete.getConsumedQuests());
        }
    }

    // =====================================================================
    // INBTSerializable<CompoundTag>
    // =====================================================================

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Food", food);
        tag.putFloat("MaxFood", maxFood);
        tag.putFloat("Water", water);
        tag.putFloat("MaxWater", maxWater);
        tag.putFloat("Stamina", stamina);
        tag.putFloat("MaxStamina", maxStamina);
        tag.putBoolean("StaminaExhausted", staminaExhausted);
        tag.putFloat("CoreTemp", coreTemperature);
        tag.putInt("ColdExposureTicks", coldExposureTicks);
        tag.putInt("HeatExposureTicks", heatExposureTicks);
        if (!debuffs.isEmpty()) {
            CompoundTag debuffTag = new CompoundTag();
            for (Map.Entry<String, Integer> entry : debuffs.entrySet()) {
                debuffTag.putInt(entry.getKey(), entry.getValue());
            }
            tag.put("Debuffs", debuffTag);
        }

        tag.putInt("XP", xp);
        tag.putInt("Level", level);
        tag.putInt("PerkPoints", perkPoints);
        tag.putInt("AttributePoints", attributePoints);

        CompoundTag attrTag = new CompoundTag();
        for (Attribute attr : Attribute.values()) {
            attrTag.putInt(attr.name(), attributeLevels[attr.ordinal()]);
        }
        tag.put("Attributes", attrTag);

        if (!activePerks.isEmpty()) {
            CompoundTag perkTag = new CompoundTag();
            for (Map.Entry<String, Integer> entry : activePerks.entrySet()) {
                perkTag.putInt(entry.getKey(), entry.getValue());
            }
            tag.put("Perks", perkTag);
        }

        tag.putLong("UnkillableCooldown", unkillableCooldownEnd);

        tag.put("Magazines", magazineData.save());

        if (!unlockedRecipes.isEmpty()) {
            ListTag recipeList = new ListTag();
            for (String recipeId : unlockedRecipes) {
                recipeList.add(net.minecraft.nbt.StringTag.valueOf(recipeId));
            }
            tag.put("UnlockedRecipes", recipeList);
        }

        if (!activeQuests.isEmpty()) {
            ListTag questList = new ListTag();
            for (QuestInstance quest : activeQuests) {
                questList.add(quest.save());
            }
            tag.put("Quests", questList);
        }
        if (!trackedQuestId.isEmpty()) {
            tag.putString("TrackedQuest", trackedQuestId);
        }
        if (!consumedQuests.isEmpty()) {
            CompoundTag consumed = new CompoundTag();
            for (Map.Entry<String, Long> entry : consumedQuests.entrySet()) {
                consumed.putLong(entry.getKey(), entry.getValue());
            }
            tag.put("ConsumedQuests", consumed);
        }

        return tag;
    }

    private static final float LEGACY_SCALE_THRESHOLD = 21.0f;

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        food = tag.getFloat("Food");
        maxFood = tag.contains("MaxFood") ? tag.getFloat("MaxFood") : DEFAULT_MAX_FOOD;
        water = tag.getFloat("Water");
        maxWater = tag.contains("MaxWater") ? tag.getFloat("MaxWater") : DEFAULT_MAX_WATER;
        stamina = tag.getFloat("Stamina");
        maxStamina = tag.contains("MaxStamina") ? tag.getFloat("MaxStamina") : DEFAULT_MAX_STAMINA;

        if (maxFood > LEGACY_SCALE_THRESHOLD) {
            float ratio = food / maxFood;
            maxFood = DEFAULT_MAX_FOOD;
            food = ratio * maxFood;
        }
        if (maxWater > LEGACY_SCALE_THRESHOLD) {
            float ratio = water / maxWater;
            maxWater = DEFAULT_MAX_WATER;
            water = ratio * maxWater;
        }
        if (maxStamina > LEGACY_SCALE_THRESHOLD) {
            float ratio = stamina / maxStamina;
            maxStamina = DEFAULT_MAX_STAMINA;
            stamina = ratio * maxStamina;
        }
        staminaExhausted = tag.contains("StaminaExhausted") && tag.getBoolean("StaminaExhausted");
        coreTemperature = tag.contains("CoreTemp") ? tag.getFloat("CoreTemp") : DEFAULT_CORE_TEMP;
        coldExposureTicks = tag.contains("ColdExposureTicks") ? tag.getInt("ColdExposureTicks") : 0;
        heatExposureTicks = tag.contains("HeatExposureTicks") ? tag.getInt("HeatExposureTicks") : 0;
        debuffs.clear();
        if (tag.contains("Debuffs")) {
            CompoundTag debuffTag = tag.getCompound("Debuffs");
            for (String key : debuffTag.getAllKeys()) {
                int ticks = debuffTag.getInt(key);
                if (ticks > 0) {
                    debuffs.put(key, ticks);
                }
            }
        }

        xp = tag.contains("XP") ? tag.getInt("XP") : 0;
        level = tag.contains("Level") ? tag.getInt("Level") : 1;
        perkPoints = tag.contains("PerkPoints") ? tag.getInt("PerkPoints") : 0;
        attributePoints = tag.contains("AttributePoints") ? tag.getInt("AttributePoints") : 0;

        if (tag.contains("Attributes")) {
            CompoundTag attrTag = tag.getCompound("Attributes");
            for (Attribute attr : Attribute.values()) {
                if (attrTag.contains(attr.name())) {
                    attributeLevels[attr.ordinal()] = clampInt(attrTag.getInt(attr.name()), 1, 10);
                } else {
                    attributeLevels[attr.ordinal()] = 1;
                }
            }
        }

        activePerks.clear();
        if (tag.contains("Perks")) {
            CompoundTag perkTag = tag.getCompound("Perks");
            for (String key : perkTag.getAllKeys()) {
                int rank = perkTag.getInt(key);
                if (rank > 0) {
                    activePerks.put(key, rank);
                }
            }
        }

        unkillableCooldownEnd = tag.contains("UnkillableCooldown") ? tag.getLong("UnkillableCooldown") : 0;

        if (tag.contains("Magazines")) {
            magazineData.load(tag.getCompound("Magazines"));
        }

        unlockedRecipes.clear();
        if (tag.contains("UnlockedRecipes")) {
            ListTag recipeList = tag.getList("UnlockedRecipes", Tag.TAG_STRING);
            for (int i = 0; i < recipeList.size(); i++) {
                unlockedRecipes.add(recipeList.getString(i));
            }
        }

        activeQuests.clear();
        if (tag.contains("Quests")) {
            ListTag questList = tag.getList("Quests", Tag.TAG_COMPOUND);
            for (int i = 0; i < questList.size(); i++) {
                activeQuests.add(QuestInstance.load(questList.getCompound(i)));
            }
        }
        trackedQuestId = tag.contains("TrackedQuest") ? tag.getString("TrackedQuest") : "";
        consumedQuests.clear();
        if (tag.contains("ConsumedQuests")) {
            CompoundTag consumed = tag.getCompound("ConsumedQuests");
            for (String key : consumed.getAllKeys()) {
                consumedQuests.put(key, consumed.getLong(key));
            }
        }
    }

    // ── Known debuff IDs from spec §1.2 ─────────────────────────────────
    public static final String[] KNOWN_DEBUFF_IDS = {
        "bleeding", "infection_1", "infection_2", "dysentery",
        "sprain", "fracture", "concussion", "burn",
        "hypothermia", "hyperthermia",
        "electrocuted", "stunned"
    };

    public static final String DEBUFF_BLEEDING = "bleeding";
    public static final String DEBUFF_INFECTION_1 = "infection_1";
    public static final String DEBUFF_INFECTION_2 = "infection_2";
    public static final String DEBUFF_DYSENTERY = "dysentery";
    public static final String DEBUFF_SPRAIN = "sprain";
    public static final String DEBUFF_FRACTURE = "fracture";
    public static final String DEBUFF_CONCUSSION = "concussion";
    public static final String DEBUFF_BURN = "burn";
    public static final String DEBUFF_HYPOTHERMIA = "hypothermia";
    public static final String DEBUFF_HYPERTHERMIA = "hyperthermia";
    public static final String DEBUFF_ELECTROCUTED = "electrocuted";
    public static final String DEBUFF_STUNNED = "stunned";

    // ── Helpers ──────────────────────────────────────────────────────────

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
