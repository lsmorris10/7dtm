package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncLootStagePayload(int lootStage) implements CustomPacketPayload {

    public static final Type<SyncLootStagePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_loot_stage"));

    public static final StreamCodec<ByteBuf, SyncLootStagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncLootStagePayload::lootStage,
                    SyncLootStagePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
