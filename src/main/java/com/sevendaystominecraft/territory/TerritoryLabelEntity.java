package com.sevendaystominecraft.territory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class TerritoryLabelEntity extends Entity {

    private static final EntityDataAccessor<String> LABEL_TEXT =
            SynchedEntityData.defineId(TerritoryLabelEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(TerritoryLabelEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TERRITORY_ID =
            SynchedEntityData.defineId(TerritoryLabelEntity.class, EntityDataSerializers.INT);

    public TerritoryLabelEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LABEL_TEXT, "Territory");
        builder.define(TIER, 1);
        builder.define(TERRITORY_ID, -1);
    }

    public void setLabelText(String text) {
        this.entityData.set(LABEL_TEXT, text);
        this.setCustomName(Component.literal(text));
        this.setCustomNameVisible(true);
    }

    public String getLabelText() {
        return this.entityData.get(LABEL_TEXT);
    }

    public void setTerritoryTier(int tier) {
        this.entityData.set(TIER, tier);
    }

    public int getTerritoryTier() {
        return this.entityData.get(TIER);
    }

    public void setTerritoryId(int id) {
        this.entityData.set(TERRITORY_ID, id);
    }

    public int getTerritoryId() {
        return this.entityData.get(TERRITORY_ID);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 100 == 0 && level() instanceof ServerLevel serverLevel) {
            TerritoryData data = TerritoryData.getOrCreate(serverLevel);
            TerritoryRecord record = data.getTerritoryById(getTerritoryId());
            if (record != null && record.isCleared()) {
                String clearedLabel = "§a" + record.getLabel() + " §7[Cleared]";
                if (!clearedLabel.equals(getLabelText())) {
                    setLabelText(clearedLabel);
                }
            }
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("label")) {
            setLabelText(tag.getString("label"));
        }
        if (tag.contains("tier")) {
            setTerritoryTier(tag.getInt("tier"));
        }
        if (tag.contains("territoryId")) {
            setTerritoryId(tag.getInt("territoryId"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("label", getLabelText());
        tag.putInt("tier", getTerritoryTier());
        tag.putInt("territoryId", getTerritoryId());
    }
}
