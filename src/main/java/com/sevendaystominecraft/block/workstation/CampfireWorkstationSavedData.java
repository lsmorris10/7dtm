package com.sevendaystominecraft.block.workstation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class CampfireWorkstationSavedData extends SavedData {

    private static final String DATA_NAME = "sevendaystominecraft_campfire_workstations";
    private static final WorkstationType TYPE = WorkstationType.CAMPFIRE;

    private final Map<BlockPos, CampfireData> entries = new HashMap<>();

    public CampfireWorkstationSavedData() {
    }

    public static CampfireWorkstationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(CampfireWorkstationSavedData::new, CampfireWorkstationSavedData::load),
                DATA_NAME);
    }

    public CampfireData getOrCreate(BlockPos pos) {
        return entries.computeIfAbsent(pos, p -> {
            setDirty();
            CampfireData data = new CampfireData();
            data.setDirtyCallback(this::setDirty);
            return data;
        });
    }

    public CampfireData get(BlockPos pos) {
        return entries.get(pos);
    }

    public void remove(BlockPos pos) {
        if (entries.remove(pos) != null) {
            setDirty();
        }
    }

    public Set<Map.Entry<BlockPos, CampfireData>> allEntries() {
        return entries.entrySet();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, CampfireData> entry : entries.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("Pos", entry.getKey().asLong());
            entry.getValue().save(entryTag, registries);
            list.add(entryTag);
        }
        tag.put("Entries", list);
        return tag;
    }

    private static CampfireWorkstationSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        CampfireWorkstationSavedData data = new CampfireWorkstationSavedData();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            BlockPos pos = BlockPos.of(entryTag.getLong("Pos"));
            CampfireData campfireData = new CampfireData();
            campfireData.load(entryTag, registries);
            campfireData.setDirtyCallback(data::setDirty);
            data.entries.put(pos, campfireData);
        }
        return data;
    }

    public static class CampfireData {
        private NonNullList<ItemStack> items = NonNullList.withSize(TYPE.getTotalSlots(), ItemStack.EMPTY);
        private int burnTime;
        private int burnTimeTotal;
        private int craftProgress;
        private int craftTimeTotal;
        private Runnable dirtyCallback;

        public void setDirtyCallback(Runnable callback) {
            this.dirtyCallback = callback;
        }

        public void markDirty() {
            if (dirtyCallback != null) {
                dirtyCallback.run();
            }
        }

        public NonNullList<ItemStack> getItems() { return items; }
        public int getContainerSize() { return items.size(); }

        public ItemStack getItem(int slot) {
            if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
            return items.get(slot);
        }

        public void setItem(int slot, ItemStack stack) {
            if (slot < 0 || slot >= items.size()) return;
            items.set(slot, stack);
            markDirty();
        }

        public ItemStack removeItem(int slot, int count) {
            if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
            ItemStack result = ContainerHelper.removeItem(items, slot, count);
            if (!result.isEmpty()) {
                markDirty();
            }
            return result;
        }

        public int getBurnTime() { return burnTime; }
        public int getBurnTimeTotal() { return burnTimeTotal; }
        public int getCraftProgress() { return craftProgress; }
        public int getCraftTimeTotal() { return craftTimeTotal; }

        public boolean tick() {
            boolean changed = false;
            if (burnTime > 0) {
                burnTime--;
                changed = true;
            }

            WorkstationRecipe matchedRecipe = findMatchingRecipe();

            if (burnTime == 0 && matchedRecipe != null) {
                int fuelSlot = TYPE.getInputSlots() + TYPE.getOutputSlots();
                if (fuelSlot < items.size()) {
                    ItemStack fuel = items.get(fuelSlot);
                    if (!fuel.isEmpty()) {
                        int fuelTime = getFuelBurnTime(fuel);
                        if (fuelTime > 0) {
                            burnTimeTotal = fuelTime;
                            burnTime = burnTimeTotal;
                            if (fuel.is(Items.LAVA_BUCKET)) {
                                items.set(fuelSlot, new ItemStack(Items.BUCKET));
                            } else {
                                fuel.shrink(1);
                            }
                            changed = true;
                        }
                    }
                }
            }

            if (burnTime > 0 && matchedRecipe != null) {
                if (craftTimeTotal != matchedRecipe.processingTicks()) {
                    craftTimeTotal = matchedRecipe.processingTicks();
                }
                if (craftTimeTotal <= 0) craftTimeTotal = 200;
                craftProgress++;
                changed = true;
                if (craftProgress >= craftTimeTotal) {
                    processRecipe(matchedRecipe);
                    craftProgress = 0;
                }
            } else if (matchedRecipe == null) {
                if (craftProgress > 0) {
                    craftProgress = 0;
                    changed = true;
                }
            }
            return changed;
        }

        private WorkstationRecipe findMatchingRecipe() {
            List<ItemStack> inputStacks = new ArrayList<>();
            for (int i = 0; i < TYPE.getInputSlots() && i < items.size(); i++) {
                ItemStack stack = items.get(i);
                if (!stack.isEmpty()) {
                    inputStacks.add(stack);
                }
            }
            if (inputStacks.isEmpty()) return null;

            WorkstationRecipe recipe = WorkstationRecipes.findMatch(TYPE, ing -> {
                int total = 0;
                for (ItemStack stack : inputStacks) {
                    if (ing.testStack(stack)) {
                        total += stack.getCount();
                    }
                }
                return total;
            });
            if (recipe == null) return null;

            if (!canFitOutput(recipe.output())) return null;
            return recipe;
        }

        private boolean canFitOutput(ItemStack output) {
            int outputStart = TYPE.getInputSlots();
            int outputEnd = outputStart + TYPE.getOutputSlots();
            for (int i = outputStart; i < outputEnd && i < items.size(); i++) {
                ItemStack existing = items.get(i);
                if (existing.isEmpty()) return true;
                if (ItemStack.isSameItemSameComponents(existing, output)
                        && existing.getCount() + output.getCount() <= existing.getMaxStackSize()) {
                    return true;
                }
            }
            return false;
        }

        private void processRecipe(WorkstationRecipe recipe) {
            recipe.consumeInputs((ing, count) -> consumeFromInputSlots(ing, count));
            addToOutput(recipe.output().copy());
        }

        private void consumeFromInputSlots(WorkstationRecipe.Ingredient ing, int count) {
            int remaining = count;
            for (int i = 0; i < TYPE.getInputSlots() && i < items.size() && remaining > 0; i++) {
                ItemStack stack = items.get(i);
                if (ing.testStack(stack)) {
                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    remaining -= take;
                }
            }
        }

        private void addToOutput(ItemStack result) {
            int outputStart = TYPE.getInputSlots();
            int outputEnd = outputStart + TYPE.getOutputSlots();
            for (int i = outputStart; i < outputEnd && i < items.size(); i++) {
                ItemStack existing = items.get(i);
                if (existing.isEmpty()) {
                    items.set(i, result.copy());
                    return;
                }
                if (ItemStack.isSameItemSameComponents(existing, result)
                        && existing.getCount() + result.getCount() <= existing.getMaxStackSize()) {
                    existing.grow(result.getCount());
                    return;
                }
            }
        }

        private int getFuelBurnTime(ItemStack fuel) {
            if (fuel.is(Items.COAL) || fuel.is(Items.CHARCOAL)) return 1600;
            if (fuel.is(Items.COAL_BLOCK)) return 16000;
            if (fuel.is(Items.LAVA_BUCKET)) return 20000;
            if (fuel.is(Items.OAK_LOG) || fuel.is(Items.BIRCH_LOG) || fuel.is(Items.SPRUCE_LOG)
                || fuel.is(Items.DARK_OAK_LOG) || fuel.is(Items.JUNGLE_LOG) || fuel.is(Items.ACACIA_LOG)
                || fuel.is(Items.MANGROVE_LOG) || fuel.is(Items.CHERRY_LOG)) return 300;
            if (fuel.is(Items.OAK_PLANKS) || fuel.is(Items.BIRCH_PLANKS) || fuel.is(Items.SPRUCE_PLANKS)
                || fuel.is(Items.DARK_OAK_PLANKS) || fuel.is(Items.JUNGLE_PLANKS) || fuel.is(Items.ACACIA_PLANKS)) return 200;
            if (fuel.is(Items.STICK)) return 100;
            if (fuel.is(Items.DRIED_KELP_BLOCK)) return 4000;
            if (fuel.is(Items.BLAZE_ROD)) return 2400;
            return 0;
        }

        public boolean hasItems() {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) return true;
            }
            return false;
        }

        public void save(CompoundTag tag, HolderLookup.Provider registries) {
            ContainerHelper.saveAllItems(tag, items, registries);
            tag.putInt("BurnTime", burnTime);
            tag.putInt("BurnTimeTotal", burnTimeTotal);
            tag.putInt("CraftProgress", craftProgress);
            tag.putInt("CraftTimeTotal", craftTimeTotal);
        }

        public void load(CompoundTag tag, HolderLookup.Provider registries) {
            items = NonNullList.withSize(TYPE.getTotalSlots(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag, items, registries);
            burnTime = tag.getInt("BurnTime");
            burnTimeTotal = tag.getInt("BurnTimeTotal");
            craftProgress = tag.getInt("CraftProgress");
            craftTimeTotal = tag.getInt("CraftTimeTotal");
        }
    }
}
