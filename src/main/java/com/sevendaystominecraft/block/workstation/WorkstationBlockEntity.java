package com.sevendaystominecraft.block.workstation;

import com.sevendaystominecraft.block.ModBlockEntities;
import com.sevendaystominecraft.block.workstation.recipe.WorkstationCraftingRecipe;
import com.sevendaystominecraft.block.workstation.recipe.WorkstationRecipeInput;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkstationBlockEntity extends BlockEntity {

    private WorkstationType workstationType;
    private NonNullList<ItemStack> items;
    private int burnTime;
    private int burnTimeTotal;
    private int craftProgress;
    private int craftTimeTotal;
    private java.util.UUID lastCrafterId;

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

    public void setLastCrafter(Player player) {
        this.lastCrafterId = player != null ? player.getUUID() : null;
    }

    public java.util.UUID getLastCrafterId() {
        return lastCrafterId;
    }

    public int getBurnTime() { return burnTime; }
    public int getBurnTimeTotal() { return burnTimeTotal; }
    public int getCraftProgress() { return craftProgress; }
    public int getCraftTimeTotal() { return craftTimeTotal; }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (workstationType.usesFuel()) {
            tickFuelStation();
        } else {
            tickInstantStation();
        }
    }

    private void tickFuelStation() {
        if (burnTime > 0) {
            burnTime--;
        }

        WorkstationCraftingRecipe matchedRecipe = findMatchingRecipe();

        if (burnTime == 0 && matchedRecipe != null) {
            int fuelSlot = getFuelSlotStart();
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
                        setChanged();
                    }
                }
            }
        }

        if (burnTime > 0 && matchedRecipe != null) {
            if (craftTimeTotal != matchedRecipe.getProcessingTicks()) {
                craftTimeTotal = matchedRecipe.getProcessingTicks();
            }
            if (craftTimeTotal <= 0) craftTimeTotal = 200;
            craftProgress++;
            if (craftProgress >= craftTimeTotal) {
                processRecipe(matchedRecipe);
                craftProgress = 0;
            }
            setChanged();
        } else if (matchedRecipe == null) {
            if (craftProgress > 0) {
                craftProgress = 0;
                setChanged();
            }
        }
    }

    private void tickInstantStation() {
        WorkstationCraftingRecipe matchedRecipe = findMatchingRecipe();
        if (matchedRecipe != null) {
            if (matchedRecipe.getProcessingTicks() <= 0) {
                processRecipe(matchedRecipe);
                craftProgress = 0;
                craftTimeTotal = 0;
                setChanged();
            } else {
                if (craftTimeTotal != matchedRecipe.getProcessingTicks()) {
                    craftTimeTotal = matchedRecipe.getProcessingTicks();
                }
                craftProgress++;
                if (craftProgress >= craftTimeTotal) {
                    processRecipe(matchedRecipe);
                    craftProgress = 0;
                }
                setChanged();
            }
        } else {
            if (craftProgress > 0) {
                craftProgress = 0;
                setChanged();
            }
        }
    }

    private WorkstationCraftingRecipe findMatchingRecipe() {
        if (level == null) return null;

        List<ItemStack> inputStacks = collectInputStacks();
        if (inputStacks.isEmpty()) return null;

        WorkstationRecipeInput input = new WorkstationRecipeInput(inputStacks);
        if (level.getServer() == null) return null;
        Optional<RecipeHolder<WorkstationCraftingRecipe>> holder =
                level.getServer().getRecipeManager().getRecipeFor(workstationType.getRecipeType(), input, level);

        if (holder.isEmpty()) return null;

        WorkstationCraftingRecipe recipe = holder.get().value();
        if (!canFitOutput(recipe.getResult())) return null;

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            String recipeId = holder.get().id().location().toString();
            net.minecraft.server.level.ServerPlayer crafter = resolveCrafter(serverLevel);
            if (crafter != null) {
                com.sevendaystominecraft.capability.SevenDaysPlayerStats stats =
                        crafter.getData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get());
                if (!com.sevendaystominecraft.advancement.RecipeUnlockManager.isRecipeUnlocked(stats, recipeId)) {
                    return null;
                }
            } else {
                if (!com.sevendaystominecraft.advancement.RecipeUnlockManager.isStarterRecipe(recipeId)) {
                    return null;
                }
            }
        }

        return recipe;
    }

    private List<ItemStack> collectInputStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < workstationType.getInputSlots() && i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private boolean canFitOutput(ItemStack output) {
        int outputStart = workstationType.getInputSlots();
        int outputEnd = outputStart + workstationType.getOutputSlots();
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

    private void processRecipe(WorkstationCraftingRecipe recipe) {
        consumeRecipeInputs(recipe);
        ItemStack output = recipe.getResult().copy();

        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            applyPerkQualityBonus(sl, output);
            applyCraftingPerkBonuses(sl, output);
            com.sevendaystominecraft.sound.ModSounds.playAtBlock(
                    com.sevendaystominecraft.sound.ModSounds.CRAFT_COMPLETE, level, worldPosition,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        addToOutput(output);

        if ((workstationType == WorkstationType.CAMPFIRE || workstationType == WorkstationType.GRILL)
                && level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && com.sevendaystominecraft.config.ZombieConfig.INSTANCE.smellTrackingEnabled.get()) {
            for (net.minecraft.server.level.ServerPlayer player : serverLevel.getPlayers(
                    p -> p.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 64)) {
                com.sevendaystominecraft.smell.SmellTracker.markRecentlyCookedFor(player);
            }
        }
    }

    private void consumeRecipeInputs(WorkstationCraftingRecipe recipe) {
        for (SizedIngredient sized : recipe.getIngredients()) {
            int remaining = sized.count();
            for (int i = 0; i < workstationType.getInputSlots() && i < items.size() && remaining > 0; i++) {
                ItemStack stack = items.get(i);
                if (sized.ingredient().test(stack)) {
                    int take = Math.min(remaining, stack.getCount());
                    stack.shrink(take);
                    remaining -= take;
                }
            }
        }
    }

    private net.minecraft.server.level.ServerPlayer resolveCrafter(net.minecraft.server.level.ServerLevel serverLevel) {
        if (lastCrafterId != null) {
            net.minecraft.server.level.ServerPlayer crafter = serverLevel.getServer().getPlayerList().getPlayer(lastCrafterId);
            if (crafter != null && crafter.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 256) {
                return crafter;
            }
        }
        for (net.minecraft.server.level.ServerPlayer p : serverLevel.getPlayers(
                pl -> pl.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 64)) {
            return p;
        }
        return null;
    }

    private void applyPerkQualityBonus(net.minecraft.server.level.ServerLevel serverLevel, ItemStack output) {
        if (output.getMaxDamage() <= 0) return;

        net.minecraft.server.level.ServerPlayer crafter = resolveCrafter(serverLevel);
        if (crafter == null) return;
        if (!crafter.hasData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get())) return;

        com.sevendaystominecraft.capability.SevenDaysPlayerStats stats =
                crafter.getData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get());

        int engRank = stats.getPerkRank("advanced_engineering");
        int mastermindRank = stats.getPerkRank("mastermind");

        int qualityLevel = 1;
        if (engRank >= 5) qualityLevel = 4;
        else if (engRank >= 3) qualityLevel = 3;
        else if (engRank >= 1) qualityLevel = 2;

        if (mastermindRank > 0 && serverLevel.getRandom().nextFloat() < 0.20f) {
            qualityLevel = Math.min(6, qualityLevel + 1);
        }

        if (qualityLevel > 1) {
            CompoundTag tag = output.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            tag.putInt("QualityTier", qualityLevel);
            output.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.of(tag));
        }
    }

    private void applyCraftingPerkBonuses(net.minecraft.server.level.ServerLevel serverLevel, ItemStack output) {
        net.minecraft.server.level.ServerPlayer crafter = resolveCrafter(serverLevel);
        if (crafter == null) return;
        if (!crafter.hasData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get())) return;

        com.sevendaystominecraft.capability.SevenDaysPlayerStats stats =
                crafter.getData(com.sevendaystominecraft.capability.ModAttachments.PLAYER_STATS.get());

        if (workstationType == WorkstationType.CAMPFIRE || workstationType == WorkstationType.GRILL) {
            int cookRank = stats.getPerkRank("campfire_cook");
            if (cookRank > 0 && serverLevel.getRandom().nextFloat() < 0.15f * cookRank) {
                output.grow(1);
            }
        }

        if (workstationType == WorkstationType.CHEMISTRY_STATION) {
            int physicianRank = stats.getPerkRank("physician");
            if (physicianRank > 0 && serverLevel.getRandom().nextFloat() < 0.15f * physicianRank) {
                output.grow(1);
            }
        }

        if (output.getItem() instanceof com.sevendaystominecraft.item.CoinBagItem) {
            int packMuleRank = stats.getPerkRank("pack_mule");
            int tier = packMuleRank >= 4 ? 4 : 2;
            com.sevendaystominecraft.item.CoinBagItem.setTier(output, tier);
        }
    }

    private void addToOutput(ItemStack result) {
        int outputStart = workstationType.getInputSlots();
        int outputEnd = outputStart + workstationType.getOutputSlots();
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

    private int getFuelSlotStart() {
        return workstationType.getInputSlots() + workstationType.getOutputSlots();
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

    public static boolean isValidFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(Items.COAL_BLOCK)
            || stack.is(Items.LAVA_BUCKET) || stack.is(Items.STICK)
            || stack.is(Items.DRIED_KELP_BLOCK) || stack.is(Items.BLAZE_ROD)
            || stack.is(Items.OAK_LOG) || stack.is(Items.BIRCH_LOG) || stack.is(Items.SPRUCE_LOG)
            || stack.is(Items.DARK_OAK_LOG) || stack.is(Items.JUNGLE_LOG) || stack.is(Items.ACACIA_LOG)
            || stack.is(Items.MANGROVE_LOG) || stack.is(Items.CHERRY_LOG)
            || stack.is(Items.OAK_PLANKS) || stack.is(Items.BIRCH_PLANKS) || stack.is(Items.SPRUCE_PLANKS)
            || stack.is(Items.DARK_OAK_PLANKS) || stack.is(Items.JUNGLE_PLANKS) || stack.is(Items.ACACIA_PLANKS);
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
