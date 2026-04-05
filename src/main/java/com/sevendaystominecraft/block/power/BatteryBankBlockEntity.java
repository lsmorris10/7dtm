package com.sevendaystominecraft.block.power;

import com.sevendaystominecraft.block.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class BatteryBankBlockEntity extends BlockEntity implements PowerSourceBlockEntity {

    private static final int MAX_ENERGY = 1000;
    private static final int CHARGE_RATE = 2;
    private static final int DISCHARGE_RATE = 1;
    private static final int POWER_OUTPUT = 50;

    private int storedEnergy = 0;

    public BatteryBankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_BANK_BE.get(), pos, state);
    }

    @Override
    public boolean isProducingPower() {
        return storedEnergy > 0;
    }

    @Override
    public int getPowerOutput() {
        return storedEnergy > 0 ? POWER_OUTPUT : 0;
    }

    public int getStoredEnergy() {
        return storedEnergy;
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public boolean stillValid(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        PowerGridManager grid = PowerGridManager.get(serverLevel);
        Set<BlockPos> sources = grid.getConnectedSources(pos);
        for (BlockPos sourcePos : sources) {
            var be = serverLevel.getBlockEntity(sourcePos);
            if (be instanceof PowerSourceBlockEntity source && source != this && source.isProducingPower()) {
                int toCharge = Math.min(CHARGE_RATE, MAX_ENERGY - storedEnergy);
                if (toCharge > 0) {
                    storedEnergy += toCharge;
                    setChanged();
                }
                break;
            }
        }

        Set<BlockPos> devices = grid.getConnectedDevices(pos);
        if (!devices.isEmpty() && storedEnergy > 0) {
            boolean hasConsumer = false;
            for (BlockPos devicePos : devices) {
                if (serverLevel.getBlockState(devicePos).getBlock() instanceof PoweredDeviceBlock) {
                    hasConsumer = true;
                    break;
                }
            }
            if (hasConsumer) {
                storedEnergy = Math.max(0, storedEnergy - DISCHARGE_RATE);
                setChanged();
            }
        }

        if (!devices.isEmpty() && level.getGameTime() % 40 == 0) {
            PowerWireRenderer.spawnWireParticles(serverLevel, pos, devices, storedEnergy > 0);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("StoredEnergy", storedEnergy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedEnergy = tag.getInt("StoredEnergy");
    }
}
