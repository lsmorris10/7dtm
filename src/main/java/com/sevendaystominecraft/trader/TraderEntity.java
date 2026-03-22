package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class TraderEntity extends PathfinderMob {

    private static final String[] TRADER_NAMES = {
            "Trader Joel", "Trader Rekt", "Trader Jen", "Trader Hugh", "Trader Bob"
    };

    private static final EntityDataAccessor<String> TRADER_NAME =
            SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TRADER_TIER =
            SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.INT);

    private int traderId = -1;

    public TraderEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TRADER_NAME, "Trader");
        builder.define(TRADER_TIER, 1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 100.0);
    }

    public void setTraderName(String name) {
        this.entityData.set(TRADER_NAME, name);
        this.setCustomName(Component.literal(name));
        this.setCustomNameVisible(true);
    }

    public String getTraderName() {
        return this.entityData.get(TRADER_NAME);
    }

    public void setTraderTier(int tier) {
        this.entityData.set(TRADER_TIER, tier);
    }

    public int getTraderTier() {
        return this.entityData.get(TRADER_TIER);
    }

    public void setTraderId(int id) {
        this.traderId = id;
    }

    public int getTraderId() {
        return traderId;
    }

    public static String randomName(net.minecraft.util.RandomSource random) {
        return TRADER_NAMES[random.nextInt(TRADER_NAMES.length)];
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int tier = getTraderTier();
            ServerLevel serverLevel = (ServerLevel) level();
            TraderData data = TraderData.getOrCreate(serverLevel);
            TraderRecord record = data.getTraderById(traderId);
            List<TraderInventory.TraderOffer> offers = TraderInventory.getOffersForTier(tier);

            int[] stockArray = new int[offers.size()];
            for (int i = 0; i < offers.size(); i++) {
                stockArray[i] = record != null ? record.getStock(i) : offers.get(i).maxStock();
            }

            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInv, p) -> new TraderMenu(containerId, playerInv, tier, traderId, stockArray),
                    Component.literal(getTraderName())
            ), buf -> {
                buf.writeInt(tier);
                buf.writeInt(traderId);
                buf.writeInt(stockArray.length);
                for (int s : stockArray) {
                    buf.writeInt(s);
                }
            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("traderName")) {
            setTraderName(tag.getString("traderName"));
        }
        if (tag.contains("traderTier")) {
            setTraderTier(tag.getInt("traderTier"));
        }
        if (tag.contains("traderId")) {
            traderId = tag.getInt("traderId");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("traderName", getTraderName());
        tag.putInt("traderTier", getTraderTier());
        tag.putInt("traderId", traderId);
    }
}
