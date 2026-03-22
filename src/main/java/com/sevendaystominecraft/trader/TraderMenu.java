package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.menu.ModMenuTypes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class TraderMenu extends AbstractContainerMenu {

    private final int traderTier;
    private final int traderId;
    private final String traderName;
    private final List<TraderInventory.TraderOffer> offers;
    private final List<TraderInventory.TraderOffer> secretStash;
    private final Player owner;
    private final SimpleContainer sellContainer;
    private final ContainerData stockData;
    private final int betterBarterRank;

    public static final int SELL_SLOT_COUNT = 4;

    public TraderMenu(int containerId, Inventory playerInv, int traderTier, int traderId,
                      String traderName, int[] initialStock, int betterBarterRank) {
        super(ModMenuTypes.TRADER_MENU.get(), containerId);
        this.traderTier = traderTier;
        this.traderId = traderId;
        this.traderName = traderName;
        this.offers = TraderInventory.getOffersForTrader(traderName);
        this.secretStash = betterBarterRank >= 5 ? TraderInventory.getSecretStash(traderName) : List.of();
        this.owner = playerInv.player;
        this.sellContainer = new SimpleContainer(SELL_SLOT_COUNT);
        this.betterBarterRank = betterBarterRank;

        int totalOffers = offers.size() + secretStash.size();
        this.stockData = new SimpleContainerData(totalOffers);
        for (int i = 0; i < totalOffers && i < initialStock.length; i++) {
            stockData.set(i, initialStock[i]);
        }
        addDataSlots(stockData);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
        }

        for (int i = 0; i < SELL_SLOT_COUNT; i++) {
            addSlot(new Slot(sellContainer, i, 8 + i * 18, 110) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() != ModItems.DUKE_TOKEN.get();
                }
            });
        }
    }

    public static TraderMenu fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        int tier = buf.readInt();
        int traderId = buf.readInt();
        String traderName = buf.readUtf();
        int barterRank = buf.readInt();
        int stockCount = buf.readInt();
        int[] stock = new int[stockCount];
        for (int i = 0; i < stockCount; i++) {
            stock[i] = buf.readInt();
        }
        return new TraderMenu(containerId, playerInv, tier, traderId, traderName, stock, barterRank);
    }

    public int getTraderTier() { return traderTier; }
    public int getTraderId() { return traderId; }
    public String getTraderName() { return traderName; }
    public List<TraderInventory.TraderOffer> getOffers() { return offers; }
    public List<TraderInventory.TraderOffer> getSecretStash() { return secretStash; }
    public int getBetterBarterRank() { return betterBarterRank; }

    public int getAdjustedBuyPrice(TraderInventory.TraderOffer offer) {
        return TraderInventory.getBuyPrice(offer.buyPrice(), betterBarterRank, 1.0f);
    }

    public int getStock(int offerIndex) {
        if (offerIndex < 0 || offerIndex >= stockData.getCount()) return 0;
        return stockData.get(offerIndex);
    }

    private boolean isTraderOpen(Player player) {
        long timeOfDay = player.level().getDayTime() % 24000;
        return timeOfDay >= 0 && timeOfDay < 16000;
    }

    public boolean tryBuy(Player player, int offerIndex) {
        if (player.level() instanceof ServerLevel && !isTraderOpen(player)) return false;

        boolean isSecretStash = offerIndex >= offers.size();
        List<TraderInventory.TraderOffer> targetList;
        int listIndex;

        if (isSecretStash) {
            if (secretStash.isEmpty()) return false;
            listIndex = offerIndex - offers.size();
            if (listIndex < 0 || listIndex >= secretStash.size()) return false;
            targetList = secretStash;
        } else {
            if (offerIndex < 0 || offerIndex >= offers.size()) return false;
            listIndex = offerIndex;
            targetList = offers;
        }

        TraderInventory.TraderOffer offer = targetList.get(listIndex);

        if (player.level() instanceof ServerLevel serverLevel) {
            TraderData data = TraderData.getOrCreate(serverLevel);
            TraderRecord record = data.getTraderById(traderId);
            if (record != null && record.getStock(offerIndex) <= 0) return false;

            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            int rank = stats.getPerkRank("better_barter");
            int tokensNeeded = TraderInventory.getBuyPrice(offer.buyPrice(), rank, 1.0f);
            int tokensHeld = countTokens(player);
            if (tokensHeld < tokensNeeded) return false;

            ItemStack result = offer.item().copy();
            if (!player.getInventory().add(result)) return false;

            removeTokens(player, tokensNeeded);
            com.sevendaystominecraft.sound.ModSounds.playAtEntity(
                    com.sevendaystominecraft.sound.ModSounds.TRADER_BUY, player,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
            if (record != null) {
                record.decrementStock(offerIndex);
                stockData.set(offerIndex, record.getStock(offerIndex));
                data.markDirtyRecord();
            }
            return true;
        }

        return false;
    }

    public boolean trySellSlots(Player player) {
        if (player.level() instanceof ServerLevel && !isTraderOpen(player)) return false;

        int barterRank = 0;
        if (player.level() instanceof ServerLevel) {
            SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
            barterRank = stats.getPerkRank("better_barter");
        }

        int totalValue = 0;
        for (int i = 0; i < SELL_SLOT_COUNT; i++) {
            ItemStack stack = sellContainer.getItem(i);
            if (stack.isEmpty()) continue;
            int valuePerItem = TraderInventory.getSellValue(stack, barterRank);
            totalValue += valuePerItem * stack.getCount();
        }
        if (totalValue <= 0) return false;

        for (int i = 0; i < SELL_SLOT_COUNT; i++) {
            sellContainer.setItem(i, ItemStack.EMPTY);
        }

        giveTokens(player, totalValue);
        return true;
    }

    public int getSellSlotsValue() {
        int totalValue = 0;
        for (int i = 0; i < SELL_SLOT_COUNT; i++) {
            ItemStack stack = sellContainer.getItem(i);
            if (stack.isEmpty()) continue;
            int valuePerItem = TraderInventory.getSellValue(stack, betterBarterRank);
            totalValue += valuePerItem * stack.getCount();
        }
        return totalValue;
    }

    private int countTokens(Player player) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.DUKE_TOKEN.get()) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private void removeTokens(Player player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.DUKE_TOKEN.get()) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                remaining -= take;
            }
        }
    }

    private void giveTokens(Player player, int amount) {
        ItemStack tokens = new ItemStack(ModItems.DUKE_TOKEN.get(), amount);
        if (!player.getInventory().add(tokens)) {
            player.drop(tokens, false);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.level().isClientSide()) return true;
        List<TraderEntity> nearby = player.level().getEntitiesOfClass(
                TraderEntity.class,
                new AABB(player.blockPosition()).inflate(10.0),
                e -> e.getTraderId() == traderId
        );
        return !nearby.isEmpty();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        for (int i = 0; i < SELL_SLOT_COUNT; i++) {
            ItemStack stack = sellContainer.getItem(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                sellContainer.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
