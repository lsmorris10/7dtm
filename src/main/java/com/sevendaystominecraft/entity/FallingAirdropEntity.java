package com.sevendaystominecraft.entity;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.ModBlocks;
import com.sevendaystominecraft.block.loot.LootContainerType;
import com.sevendaystominecraft.block.loot.LootContainerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FallingAirdropEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private double speed = -0.15;

    public FallingAirdropEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false; // Need collision for the ground
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) { }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) { }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(0, speed, 0);
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

        if (!this.level().isClientSide()) {
            if (this.onGround() || this.verticalCollision) {
                placeCrate();
            } else if (this.getY() < this.level().getMinBuildHeight()) {
                this.discard();
            }
        }
    }

    private void placeCrate() {
        BlockPos pos = this.blockPosition();
        Level level = this.level();

        // Find standard ground
        while (level.getBlockState(pos).isAir() && pos.getY() > level.getMinBuildHeight()) {
            pos = pos.below();
        }

        pos = pos.above();
        
        BlockState crateState = ModBlocks.SUPPLY_CRATE_BLOCK.get().defaultBlockState();
        level.setBlockAndUpdate(pos, crateState);
        
        if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity be) {
            be.setTerritoryTier(5); // High tier loot for airdrops
        }

        SevenDaysToMinecraft.LOGGER.info("[BZHS] Airdrop landed at {}!", pos);
        this.discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
