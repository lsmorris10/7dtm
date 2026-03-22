package com.sevendaystominecraft.entity.zombie;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.config.ZombieConfig;
import com.sevendaystominecraft.entity.zombie.ai.ZombieBreakBlockGoal;
import com.sevendaystominecraft.entity.zombie.ai.ZombieDetectionGoal;
import com.sevendaystominecraft.entity.zombie.ai.ZombieHordePathGoal;
import com.sevendaystominecraft.entity.zombie.ai.ZombieInvestigateGoal;
import com.sevendaystominecraft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class BaseSevenDaysZombie extends Zombie {

    private static final EntityDataAccessor<Integer> DATA_DETECTION_STATE =
            SynchedEntityData.defineId(BaseSevenDaysZombie.class, EntityDataSerializers.INT);

    protected final ZombieVariant variant;
    protected ZombieVariant modifier;
    protected boolean isHordeMob;
    private boolean statsApplied = false;
    private float lastDisplayedHP = -1;

    public BaseSevenDaysZombie(EntityType<? extends Zombie> type, Level level, ZombieVariant variant) {
        super(type, level);
        this.variant = variant;
        this.modifier = null;
        this.isHordeMob = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DETECTION_STATE, DetectionState.UNAWARE.getId());
    }

    public DetectionState getDetectionState() {
        return DetectionState.fromId(entityData.get(DATA_DETECTION_STATE));
    }

    public void setDetectionState(DetectionState state) {
        entityData.set(DATA_DETECTION_STATE, state.getId());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new ZombieDetectionGoal(this));
        goalSelector.addGoal(3, new ZombieBreakBlockGoal(this));
        goalSelector.addGoal(4, new ZombieHordePathGoal(this));
        goalSelector.addGoal(5, new ZombieInvestigateGoal(this));
    }

    @Override
    public void setCanBreakDoors(boolean canBreakDoors) {
        super.setCanBreakDoors(false);
    }

    public ZombieVariant getVariant() {
        return variant;
    }

    public ZombieVariant getModifier() {
        return modifier;
    }

    public void setModifier(ZombieVariant mod) {
        if (mod != null && mod.isModifier()) {
            this.modifier = mod;
            if (statsApplied) {
                applyModifierStats();
            }
            applyNameTag();
        }
    }

    public void setHordeMob(boolean hordeMob) {
        this.isHordeMob = hordeMob;
        if (hordeMob) {
            setPersistenceRequired();
        }
    }

    public boolean isHordeMob() {
        return isHordeMob;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                         EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        applyAllStats();
        ModSounds.playAtEntity(ModSounds.ZOMBIE_SCREAM, this,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 0.8f + this.getRandom().nextFloat() * 0.4f);
        return spawnData;
    }

    @Override
    public void setTarget(@Nullable net.minecraft.world.entity.LivingEntity target) {
        if (!level().isClientSide() && target != null && getDetectionState() != DetectionState.ALERT) {
            return;
        }
        super.setTarget(target);
    }

    public void forceAlertTarget(net.minecraft.world.entity.LivingEntity target) {
        setDetectionState(DetectionState.ALERT);
        super.setTarget(target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean result = super.hurtServer(level, source, amount);
        if (result && source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
            forceAlertTarget(attacker);
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (!statsApplied && !level().isClientSide()) {
            applyAllStats();
        }

        if (!level().isClientSide() && getDetectionState() != DetectionState.ALERT && getTarget() != null) {
            super.setTarget(null);
        }

        if (!level().isClientSide() && modifier == ZombieVariant.RADIATED && tickCount % 20 == 0) {
            float regenPerSec = ZombieConfig.INSTANCE.radiatedRegenPerSec.get().floatValue();
            if (getHealth() < getMaxHealth()) {
                heal(regenPerSec);
            }
        }

        if (!level().isClientSide() && !isHordeMob) {
            applyNightSpeedBonus();
        }

        if (!level().isClientSide() && statsApplied && tickCount % 5 == 0) {
            float currentHP = getHealth();
            if (currentHP != lastDisplayedHP) {
                lastDisplayedHP = currentHP;
                applyNameTag();
            }
        }

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel && tickCount % 10 == 0) {
            DetectionState state = getDetectionState();
            if (state == DetectionState.SUSPICIOUS) {
                serverLevel.sendParticles(ParticleTypes.WITCH,
                        getX(), getY() + getBbHeight() + 0.5, getZ(),
                        3, 0.1, 0.1, 0.1, 0.01);
            } else if (state == DetectionState.ALERT) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        getX(), getY() + getBbHeight() + 0.5, getZ(),
                        3, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    private void applyAllStats() {
        applyVariantStats();
        if (modifier != null) {
            applyModifierStats();
        }
        applyNameTag();
        statsApplied = true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("7dtm_horde", isHordeMob);
        if (modifier != null) {
            tag.putString("7dtm_modifier", modifier.name());
        }
        tag.putInt("7dtm_detection", getDetectionState().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        isHordeMob = tag.getBoolean("7dtm_horde");
        if (tag.contains("7dtm_modifier")) {
            try {
                modifier = ZombieVariant.valueOf(tag.getString("7dtm_modifier"));
            } catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("7dtm_detection")) {
            setDetectionState(DetectionState.fromId(tag.getInt("7dtm_detection")));
        }
        applyNameTag();
        statsApplied = false;
    }

    protected void applyNameTag() {
        String displayName = buildDisplayName();
        int currentHP = (int) getHealth();
        int maxHP = (int) getMaxHealth();
        String fullText = displayName + "\n" + currentHP + " / " + maxHP;
        setCustomName(Component.literal(fullText));
        setCustomNameVisible(true);
    }

    private String buildDisplayName() {
        String name = formatEnumName(variant.name());
        if (modifier != null) {
            name = formatEnumName(modifier.name()) + " " + name;
        }
        return name;
    }

    private static String formatEnumName(String enumName) {
        String[] words = enumName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(Character.toUpperCase(words[i].charAt(0)));
            sb.append(words[i].substring(1));
        }
        return sb.toString();
    }

    protected void applyVariantStats() {
        double hp = variant.getBaseHP();
        double damage = variant.getBaseDamage();
        double speed = convertSpeedToAttribute(variant.getBaseSpeed());

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        setHealth((float) hp);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    protected void applyModifierStats() {
        if (modifier == null) return;

        double currentHP = getAttribute(Attributes.MAX_HEALTH).getBaseValue();
        double currentDmg = getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
        double currentSpd = getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();

        double hpMult, dmgMult, spdMult;
        ZombieConfig cfg = ZombieConfig.INSTANCE;

        switch (modifier) {
            case RADIATED -> {
                hpMult = cfg.radiatedHPMult.get();
                dmgMult = cfg.radiatedDamageMult.get();
                spdMult = cfg.radiatedSpeedMult.get();
            }
            case CHARGED -> {
                hpMult = cfg.chargedHPMult.get();
                dmgMult = cfg.chargedDamageMult.get();
                spdMult = cfg.chargedSpeedMult.get();
            }
            case INFERNAL -> {
                hpMult = cfg.infernalHPMult.get();
                dmgMult = cfg.infernalDamageMult.get();
                spdMult = cfg.infernalSpeedMult.get();
            }
            default -> { return; }
        }

        double newHP = currentHP * hpMult;
        double newDmg = currentDmg * dmgMult;
        double newSpd = currentSpd * spdMult;

        getAttribute(Attributes.MAX_HEALTH).setBaseValue(newHP);
        setHealth((float) newHP);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(newDmg);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(newSpd);
    }

    private void applyNightSpeedBonus() {
        if (level() instanceof ServerLevel serverLevel) {
            long timeOfDay = serverLevel.getDayTime() % SevenDaysConstants.DAY_LENGTH;
            boolean isNight = timeOfDay >= SevenDaysConstants.NIGHT_START && timeOfDay < SevenDaysConstants.NIGHT_END;
            double baseSpeed = convertSpeedToAttribute(variant.getBaseSpeed());
            ZombieConfig cfg = ZombieConfig.INSTANCE;
            if (modifier != null) {
                double spdMult = switch (modifier) {
                    case RADIATED -> cfg.radiatedSpeedMult.get();
                    case CHARGED -> cfg.chargedSpeedMult.get();
                    case INFERNAL -> cfg.infernalSpeedMult.get();
                    default -> 1.0;
                };
                baseSpeed *= spdMult;
            }

            int threshold = cfg.darknessLightThreshold.get();
            int blockLight = serverLevel.getBrightness(LightLayer.BLOCK, blockPosition());
            int skyLight = serverLevel.getBrightness(LightLayer.SKY, blockPosition());
            boolean isDark = blockLight <= threshold && skyLight <= threshold;

            if (isNight) {
                double nightBonus = cfg.nightSpeedBonus.get();
                if (isDark) {
                    double darknessBonus = cfg.darknessSpeedBonus.get();
                    double bonus = Math.max(nightBonus, darknessBonus);
                    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(baseSpeed * (1.0 + bonus));
                } else {
                    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(baseSpeed * (1.0 + nightBonus));
                }
            } else {
                if (isDark) {
                    double darknessBonus = cfg.darknessSpeedBonus.get();
                    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(baseSpeed * (1.0 + darknessBonus));
                } else {
                    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(baseSpeed);
                }
            }
        }
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        int base = variant.getXpReward();
        if (modifier != null) {
            base += modifier.getXpReward();
        }
        return base;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ZOMBIE_GROAN.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ZOMBIE_DEATH.get();
    }

    @Override
    public boolean isSunSensitive() {
        return false;
    }

    @Override
    public boolean isSensitiveToWater() {
        return false;
    }

    protected static double convertSpeedToAttribute(double blocksPerSecond) {
        return blocksPerSecond * 0.1;
    }

    public static AttributeSupplier.Builder createBaseZombieAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }
}
