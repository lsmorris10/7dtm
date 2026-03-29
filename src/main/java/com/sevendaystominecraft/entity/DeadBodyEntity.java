package com.sevendaystominecraft.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.sevendaystominecraft.menu.DeadBodyMenu;

import java.util.UUID;

public class DeadBodyEntity extends Entity {

    public static final int SLOT_COUNT = 42;
    private static final int DESPAWN_TICKS = 36000;

    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(DeadBodyEntity.class, EntityDataSerializers.STRING);

    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT);
    private UUID ownerUUID;
    private int despawnTimer = 0;

    public DeadBodyEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_NAME, "Unknown");
    }

    public void setOwnerName(String name) {
        this.entityData.set(OWNER_NAME, name);
        this.setCustomName(Component.literal(name + "'s Body"));
        this.setCustomNameVisible(true);
    }

    public String getOwnerName() {
        return this.entityData.get(OWNER_NAME);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public boolean isInventoryEmpty() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            despawnTimer++;
            if (despawnTimer >= DESPAWN_TICKS || isInventoryEmpty()) {
                discard();
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInv, p) -> new DeadBodyMenu(containerId, playerInv, this),
                    Component.literal(getOwnerName() + "'s Body")
            ), buf -> buf.writeInt(this.getId()));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("OwnerName")) {
            setOwnerName(tag.getString("OwnerName"));
        }
        if (tag.contains("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("DespawnTimer")) {
            this.despawnTimer = tag.getInt("DespawnTimer");
        }
        if (tag.contains("Items")) {
            HolderLookup.Provider registries = level().registryAccess();
            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTag = items.getCompound(i);
                int slot = itemTag.getByte("Slot") & 0xFF;
                if (slot < inventory.getContainerSize()) {
                    inventory.setItem(slot, ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY));
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("OwnerName", getOwnerName());
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putInt("DespawnTimer", despawnTimer);

        HolderLookup.Provider registries = level().registryAccess();
        ListTag items = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                items.add(stack.save(registries, itemTag));
            }
        }
        tag.put("Items", items);
    }

    public void onMenuClosed() {
        if (!level().isClientSide() && isInventoryEmpty()) {
            discard();
        }
    }
}
