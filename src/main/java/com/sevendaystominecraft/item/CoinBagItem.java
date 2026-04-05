package com.sevendaystominecraft.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class CoinBagItem extends Item {

    public static final String TAG_TIER = "CoinBagTier";
    public static final String TAG_ITEMS = "CoinBagItems";

    public CoinBagItem(Properties properties) {
        super(properties);
    }

    public static int getTier(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof CoinBagItem)) return 0;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 2;
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(TAG_TIER)) return 2;
        return tag.getInt(TAG_TIER);
    }

    public static void setTier(ItemStack stack, int tier) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(TAG_TIER, tier);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void clampTier(ItemStack stack, int maxAllowed) {
        int current = getTier(stack);
        int clamped = Math.max(2, Math.min(current, maxAllowed));
        if (clamped != current) {
            setTier(stack, clamped);
        }
    }

    public static int getSlotCount(ItemStack stack) {
        return getTier(stack);
    }

    public static NonNullList<ItemStack> getStoredItems(ItemStack bagStack, HolderLookup.Provider provider) {
        int slots = getSlotCount(bagStack);
        NonNullList<ItemStack> items = NonNullList.withSize(Math.max(slots, 1), ItemStack.EMPTY);
        CustomData customData = bagStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return items;
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(TAG_ITEMS)) return items;

        CompoundTag itemsTag = tag.getCompound(TAG_ITEMS);
        ContainerHelper.loadAllItems(itemsTag, items, provider);
        return items;
    }

    public static void setStoredItems(ItemStack bagStack, NonNullList<ItemStack> items, HolderLookup.Provider provider) {
        CompoundTag tag = bagStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag itemsTag = new CompoundTag();
        ContainerHelper.saveAllItems(itemsTag, items, provider);
        tag.put(TAG_ITEMS, itemsTag);
        bagStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static ItemStack getStoredItem(ItemStack bagStack, int slot, HolderLookup.Provider provider) {
        NonNullList<ItemStack> items = getStoredItems(bagStack, provider);
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        return items.get(slot);
    }

    public static void setStoredItem(ItemStack bagStack, int slot, ItemStack toStore, HolderLookup.Provider provider) {
        NonNullList<ItemStack> items = getStoredItems(bagStack, provider);
        if (slot >= 0 && slot < items.size()) {
            items.set(slot, toStore);
            setStoredItems(bagStack, items, provider);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int tier = getTier(stack);
        tooltipComponents.add(Component.literal("\u00A77Capacity: " + tier + " slots"));
        tooltipComponents.add(Component.literal("\u00A77Equip in coin bag slot for extra storage"));
    }
}
