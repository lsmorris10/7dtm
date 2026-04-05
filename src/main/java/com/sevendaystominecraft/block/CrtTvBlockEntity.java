package com.sevendaystominecraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CrtTvBlockEntity extends BlockEntity {

    private String channel = "";
    private boolean playing = false;
    private int frameIndex = 0;
    private int tickCounter = 0;
    private static final int TICKS_PER_FRAME = 2; // ~10 fps at 20 tps

    public CrtTvBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRT_TV_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!playing) return;

        tickCounter++;
        if (tickCounter >= TICKS_PER_FRAME) {
            tickCounter = 0;
            frameIndex++;
        }
    }

    public void startStatic() {
        this.channel = "__static__";
        this.playing = true;
        this.frameIndex = 0;
        this.tickCounter = 0;
        setChanged();
        syncToClient();
    }

    public void setChannel(String channel) {
        this.channel = channel;
        this.playing = true;
        this.frameIndex = 0;
        this.tickCounter = 0;
        setChanged();
        syncToClient();
    }

    public void stop() {
        this.playing = false;
        this.channel = "";
        this.frameIndex = 0;
        this.tickCounter = 0;
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public String getChannel() {
        return channel;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Channel", channel);
        tag.putBoolean("Playing", playing);
        tag.putInt("FrameIndex", frameIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        channel = tag.getString("Channel");
        playing = tag.getBoolean("Playing");
        frameIndex = tag.getInt("FrameIndex");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
