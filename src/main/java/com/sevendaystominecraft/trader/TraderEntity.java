package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.quest.QuestActionHandler;

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

    private static final long TRADER_OPEN_TICK = 0;
    private static final long TRADER_CLOSE_TICK = 16000;

    private static final EntityDataAccessor<String> TRADER_NAME =
            SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TRADER_TIER =
            SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TRADER_CLOSED =
            SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.BOOLEAN);

    private int traderId = -1;
    private boolean lastClosedState = false;

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
        builder.define(TRADER_CLOSED, false);
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

    public boolean isTraderOpen() {
        long timeOfDay = level().getDayTime() % 24000;
        return timeOfDay >= TRADER_OPEN_TICK && timeOfDay < TRADER_CLOSE_TICK;
    }

    public boolean isClosed() {
        return this.entityData.get(TRADER_CLOSED);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            boolean closed = !isTraderOpen();
            if (closed != lastClosedState) {
                lastClosedState = closed;
                this.entityData.set(TRADER_CLOSED, closed);
                String baseName = getTraderName();
                if (closed) {
                    this.setCustomName(Component.literal("\u00a7c[CLOSED] " + baseName));
                } else {
                    this.setCustomName(Component.literal(baseName));
                }
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (!isTraderOpen()) {
                serverPlayer.displayClientMessage(
                        Component.literal("\u00a7c" + getTraderName() + " is closed. Come back between 06:00 and 22:00."),
                        true);
                return InteractionResult.FAIL;
            }

            int tier = getTraderTier();
            String name = getTraderName();
            ServerLevel serverLevel = (ServerLevel) level();
            TraderData data = TraderData.getOrCreate(serverLevel);
            TraderRecord record = data.getTraderById(traderId);

            SevenDaysPlayerStats stats = serverPlayer.getData(ModAttachments.PLAYER_STATS.get());
            int barterRank = stats.getPerkRank("better_barter");

            List<TraderInventory.TraderOffer> offers = TraderInventory.getOffersForTrader(name);
            List<TraderInventory.TraderOffer> secretStash = barterRank >= 5
                    ? TraderInventory.getSecretStash(name) : List.of();
            int totalOffers = offers.size() + secretStash.size();

            int[] stockArray = new int[totalOffers];
            for (int i = 0; i < offers.size(); i++) {
                stockArray[i] = record != null ? record.getStock(i) : offers.get(i).maxStock();
            }
            for (int i = 0; i < secretStash.size(); i++) {
                int idx = offers.size() + i;
                stockArray[idx] = record != null ? record.getStock(idx) : secretStash.get(i).maxStock();
            }

            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInv, p) -> new TraderMenu(containerId, playerInv, tier, traderId, name, stockArray, barterRank),
                    Component.literal(name)
            ), buf -> {
                buf.writeInt(tier);
                buf.writeInt(traderId);
                buf.writeUtf(name);
                buf.writeInt(barterRank);
                buf.writeInt(stockArray.length);
                for (int s : stockArray) {
                    buf.writeInt(s);
                }
            });

            QuestActionHandler.syncTraderQuests(serverPlayer, traderId);

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
