package com.sevendaystominecraft.block.workstation;

import com.sevendaystominecraft.block.ModBlockEntities;
import com.sevendaystominecraft.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WorkstationBlockEntity extends BlockEntity {

    private WorkstationType workstationType;
    private NonNullList<ItemStack> items;
    private int burnTime;
    private int burnTimeTotal;
    private int craftProgress;
    private int craftTimeTotal;

    public WorkstationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WORKSTATION_BE.get(), pos, state);
        this.workstationType = resolveTypeFromBlock(state);
        this.items = NonNullList.withSize(workstationType.getTotalSlots(), ItemStack.EMPTY);
    }

    public WorkstationBlockEntity(BlockPos pos, BlockState state, WorkstationType type) {
        super(ModBlockEntities.WORKSTATION_BE.get(), pos, state);
        this.workstationType = type;
        this.items = NonNullList.withSize(type.getTotalSlots(), ItemStack.EMPTY);
    }

    private static WorkstationType resolveTypeFromBlock(BlockState state) {
        if (state.getBlock() instanceof WorkstationBlock wb) {
            return wb.getWorkstationType();
        }
        return WorkstationType.WORKBENCH;
    }

    public WorkstationType getWorkstationType() {
        return workstationType;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public int getContainerSize() {
        return items.size();
    }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        return items.get(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) return;
        items.set(slot, stack);
        setChanged();
    }

    public ItemStack removeItem(int slot, int count) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        setChanged();
        return result;
    }

    public boolean stillValid(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    public int getBurnTime() { return burnTime; }
    public int getBurnTimeTotal() { return burnTimeTotal; }
    public int getCraftProgress() { return craftProgress; }
    public int getCraftTimeTotal() { return craftTimeTotal; }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (workstationType.usesFuel()) {
            if (burnTime > 0) {
                burnTime--;
            }

            if (burnTime == 0 && hasInputItems()) {
                int fuelSlot = getFuelSlotStart();
                if (fuelSlot < items.size()) {
                    ItemStack fuel = items.get(fuelSlot);
                    if (!fuel.isEmpty()) {
                        burnTimeTotal = getFuelBurnTime(fuel);
                        burnTime = burnTimeTotal;
                        fuel.shrink(1);
                        setChanged();
                    }
                }
            }

            if (burnTime > 0 && hasInputItems()) {
                craftProgress++;
                if (craftTimeTotal <= 0) craftTimeTotal = 200;
                if (craftProgress >= craftTimeTotal) {
                    processCraft();
                    craftProgress = 0;
                }
                setChanged();
            } else if (!hasInputItems()) {
                craftProgress = 0;
            }
        }
    }

    private boolean hasInputItems() {
        for (int i = 0; i < workstationType.getInputSlots() && i < items.size(); i++) {
            if (!items.get(i).isEmpty()) return true;
        }
        return false;
    }

    private int getFuelSlotStart() {
        return workstationType.getInputSlots() + workstationType.getOutputSlots();
    }

    private int getFuelBurnTime(ItemStack fuel) {
        if (fuel.is(Items.COAL) || fuel.is(Items.CHARCOAL)) return 1600;
        if (fuel.is(Items.OAK_LOG) || fuel.is(Items.BIRCH_LOG) || fuel.is(Items.SPRUCE_LOG)
            || fuel.is(Items.DARK_OAK_LOG) || fuel.is(Items.JUNGLE_LOG) || fuel.is(Items.ACACIA_LOG)) return 300;
        if (fuel.is(Items.OAK_PLANKS) || fuel.is(Items.BIRCH_PLANKS) || fuel.is(Items.SPRUCE_PLANKS)) return 200;
        if (fuel.is(Items.STICK)) return 100;
        return 200;
    }

    private void processCraft() {
        int outputStart = workstationType.getInputSlots();
        int outputEnd = outputStart + workstationType.getOutputSlots();

        if (workstationType == WorkstationType.FORGE) {
            processForgeRecipes(outputStart, outputEnd);
        } else if (workstationType == WorkstationType.CAMPFIRE || workstationType == WorkstationType.GRILL) {
            processCookingRecipes(outputStart, outputEnd);
        } else {
            processGenericSmelt(outputStart, outputEnd);
        }
    }

    private void processForgeRecipes(int outputStart, int outputEnd) {
        for (int i = 0; i < workstationType.getInputSlots() && i < items.size(); i++) {
            ItemStack input = items.get(i);
            if (input.isEmpty()) continue;

            ItemStack result = ItemStack.EMPTY;
            if (input.is(ModItems.IRON_SCRAP.get())) {
                result = new ItemStack(ModItems.IRON_INGOT.get(), 1);
            } else if (input.is(ModItems.SAND.get()) && hasMaterialInInput(ModItems.CLAY.get())) {
                result = new ItemStack(ModItems.GLASS_JAR.get(), 3);
                consumeMaterialFromInput(ModItems.CLAY.get(), 1);
                input.shrink(2);
            } else if (input.is(ModItems.IRON_INGOT.get())) {
                result = new ItemStack(ModItems.FORGED_IRON.get(), 1);
            }

            if (!result.isEmpty()) {
                if (input.is(ModItems.SAND.get())) {
                    // already handled above
                } else {
                    input.shrink(1);
                }
                addToOutput(result, outputStart, outputEnd);
                break;
            }
        }
    }

    private void processCookingRecipes(int outputStart, int outputEnd) {
        for (int i = 0; i < workstationType.getInputSlots() && i < items.size(); i++) {
            ItemStack input = items.get(i);
            if (input.isEmpty()) continue;

            ItemStack result = ItemStack.EMPTY;
            if (input.is(Items.BEEF)) result = new ItemStack(Items.COOKED_BEEF);
            else if (input.is(Items.PORKCHOP)) result = new ItemStack(Items.COOKED_PORKCHOP);
            else if (input.is(Items.CHICKEN)) result = new ItemStack(Items.COOKED_CHICKEN);
            else if (input.is(Items.MUTTON)) result = new ItemStack(Items.COOKED_MUTTON);
            else if (input.is(Items.COD)) result = new ItemStack(Items.COOKED_COD);
            else if (input.is(Items.SALMON)) result = new ItemStack(Items.COOKED_SALMON);
            else if (input.is(Items.POTATO)) result = new ItemStack(Items.BAKED_POTATO);

            if (!result.isEmpty()) {
                input.shrink(1);
                addToOutput(result, outputStart, outputEnd);
                break;
            }
        }
    }

    private void processGenericSmelt(int outputStart, int outputEnd) {
        for (int i = 0; i < workstationType.getInputSlots() && i < items.size(); i++) {
            ItemStack input = items.get(i);
            if (input.isEmpty()) continue;

            ItemStack result = ItemStack.EMPTY;
            if (input.is(ModItems.IRON_SCRAP.get())) {
                result = new ItemStack(ModItems.IRON_INGOT.get(), 1);
            }

            if (!result.isEmpty()) {
                input.shrink(1);
                addToOutput(result, outputStart, outputEnd);
                break;
            }
        }
    }

    private boolean hasMaterialInInput(net.minecraft.world.item.Item item) {
        for (int i = 0; i < workstationType.getInputSlots() && i < items.size(); i++) {
            if (items.get(i).is(item)) return true;
        }
        return false;
    }

    private void consumeMaterialFromInput(net.minecraft.world.item.Item item, int count) {
        for (int i = 0; i < workstationType.getInputSlots() && i < items.size(); i++) {
            if (items.get(i).is(item)) {
                items.get(i).shrink(count);
                return;
            }
        }
    }

    private void addToOutput(ItemStack result, int outputStart, int outputEnd) {
        for (int i = outputStart; i < outputEnd && i < items.size(); i++) {
            ItemStack existing = items.get(i);
            if (existing.isEmpty()) {
                items.set(i, result.copy());
                setChanged();
                return;
            }
            if (ItemStack.isSameItemSameComponents(existing, result)
                    && existing.getCount() + result.getCount() <= existing.getMaxStackSize()) {
                existing.grow(result.getCount());
                setChanged();
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
        tag.putInt("CraftProgress", craftProgress);
        tag.putInt("CraftTimeTotal", craftTimeTotal);
        tag.putString("StationType", workstationType.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("StationType")) {
            try {
                workstationType = WorkstationType.valueOf(tag.getString("StationType"));
            } catch (IllegalArgumentException e) {
                workstationType = resolveTypeFromBlock(getBlockState());
            }
        } else {
            workstationType = resolveTypeFromBlock(getBlockState());
        }
        items = NonNullList.withSize(workstationType.getTotalSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
        craftProgress = tag.getInt("CraftProgress");
        craftTimeTotal = tag.getInt("CraftTimeTotal");
    }
}
