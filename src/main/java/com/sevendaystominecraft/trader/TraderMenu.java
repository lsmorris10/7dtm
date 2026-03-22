package com.sevendaystominecraft.trader;

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
    private final List<TraderInventory.TraderOffer> offers;
    private final Player owner;
    private final SimpleContainer sellContainer;
    private final ContainerData stockData;

    public static final int SELL_SLOT_COUNT = 4;

    public TraderMenu(int containerId, Inventory playerInv, int traderTier, int traderId, int[] initialStock) {
        super(ModMenuTypes.TRADER_MENU.get(), containerId);
        this.traderTier = traderTier;
        this.traderId = traderId;
        this.offers = TraderInventory.getOffersForTier(traderTier);
        this.owner = playerInv.player;
        this.sellContainer = new SimpleContainer(SELL_SLOT_COUNT);

        this.stockData = new SimpleContainerData(offers.size());
        for (int i = 0; i < offers.size() && i < initialStock.length; i++) {
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
        int stockCount = buf.readInt();
        int[] stock = new int[stockCount];
        for (int i = 0; i < stockCount; i++) {
            stock[i] = buf.readInt();
        }
        return new TraderMenu(containerId, playerInv, tier, traderId, stock);
    }

    public int getTraderTier() { return traderTier; }
    public int getTraderId() { return traderId; }
    public List<TraderInventory.TraderOffer> getOffers() { return offers; }

    public int getStock(int offerIndex) {
        if (offerIndex < 0 || offerIndex >= stockData.getCount()) return 0;
        return stockData.get(offerIndex);
    }

    public boolean tryBuy(Player player, int offerIndex) {
        if (offerIndex < 0 || offerIndex >= offers.size()) return false;
        TraderInventory.TraderOffer offer = offers.get(offerIndex);

        if (player.level() instanceof ServerLevel serverLevel) {
            TraderData data = TraderData.getOrCreate(serverLevel);
            TraderRecord record = data.getTraderById(traderId);
            if (record != null && record.getStock(offerIndex) <= 0) return false;

            int tokensNeeded = offer.buyPrice();
            int tokensHeld = countTokens(player);
            if (tokensHeld < tokensNeeded) return false;

            ItemStack result = offer.item().copy();
            if (!player.getInventory().add(result)) return false;

            removeTokens(player, tokensNeeded);
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
        int totalValue = 0;
        for (int i = 0; i < SELL_SLOT_COUNT; i++) {
            ItemStack stack = sellContainer.getItem(i);
            if (stack.isEmpty()) continue;
            int valuePerItem = TraderInventory.getSellValue(stack);
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
            int valuePerItem = TraderInventory.getSellValue(stack);
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
