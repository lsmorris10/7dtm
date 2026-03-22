package com.sevendaystominecraft.block.power;

import com.sevendaystominecraft.block.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SolarPanelBlockEntity extends BlockEntity implements PowerSourceBlockEntity {

    private static final int POWER_OUTPUT = 30;

    private boolean producing = false;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL_BE.get(), pos, state);
    }

    @Override
    public boolean isProducingPower() {
        return producing;
    }

    @Override
    public int getPowerOutput() {
        return producing ? POWER_OUTPUT : 0;
    }

    public int getMaxPowerOutput() {
        return POWER_OUTPUT;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        boolean wasProducing = producing;
        producing = isDaytime(level) && level.canSeeSky(pos.above());

        if (wasProducing != producing) {
            setChanged();
        }

        PowerGridManager grid = PowerGridManager.get(serverLevel);
        java.util.Set<BlockPos> devices = grid.getConnectedDevices(pos);
        if (!devices.isEmpty() && level.getGameTime() % 40 == 0) {
            PowerWireRenderer.spawnWireParticles(serverLevel, pos, devices, producing);
        }
    }

    private boolean isDaytime(Level level) {
        long dayTime = level.getDayTime() % 24000;
        return dayTime >= 0 && dayTime < 13000;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Producing", producing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        producing = tag.getBoolean("Producing");
    }
}
