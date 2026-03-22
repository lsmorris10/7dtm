package com.sevendaystominecraft.block.power;

import com.sevendaystominecraft.block.ModBlockEntities;
import com.sevendaystominecraft.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBankBlockEntity extends BlockEntity implements PowerSourceBlockEntity {

    private static final int FUEL_SLOT = 0;
    private static final int SLOT_COUNT = 1;
    private static final int FUEL_BURN_TIME = 6000;
    private static final int POWER_OUTPUT = 100;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int burnTime = 0;
    private int burnTimeTotal = 0;

    public GeneratorBankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR_BANK_BE.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public int getContainerSize() {
        return SLOT_COUNT;
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

    @Override
    public boolean isProducingPower() {
        return burnTime > 0;
    }

    @Override
    public int getPowerOutput() {
        return burnTime > 0 ? POWER_OUTPUT : 0;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getBurnTimeTotal() {
        return burnTimeTotal;
    }

    public int getMaxPowerOutput() {
        return POWER_OUTPUT;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (burnTime > 0) {
            burnTime--;
            setChanged();
        }

        if (burnTime == 0) {
            ItemStack fuel = items.get(FUEL_SLOT);
            if (!fuel.isEmpty() && fuel.is(ModItems.GAS_CAN.get())) {
                burnTimeTotal = FUEL_BURN_TIME;
                burnTime = burnTimeTotal;
                fuel.shrink(1);
                setChanged();
            }
        }

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PowerGridManager grid = PowerGridManager.get(serverLevel);
            java.util.Set<BlockPos> devices = grid.getConnectedDevices(pos);
            if (!devices.isEmpty() && level.getGameTime() % 40 == 0) {
                PowerWireRenderer.spawnWireParticles(serverLevel, pos, devices, burnTime > 0);
            }
        }
    }

    public static boolean isValidFuel(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.GAS_CAN.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
    }
}
