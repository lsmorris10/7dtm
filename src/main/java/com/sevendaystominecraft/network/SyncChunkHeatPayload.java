package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncChunkHeatPayload(
        float chunkHeat
) implements CustomPacketPayload {

    public static final Type<SyncChunkHeatPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_chunk_heat")
    );

    public static final StreamCodec<ByteBuf, SyncChunkHeatPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SyncChunkHeatPayload decode(ByteBuf buf) {
                    return new SyncChunkHeatPayload(buf.readFloat());
                }

                @Override
                public void encode(ByteBuf buf, SyncChunkHeatPayload payload) {
                    buf.writeFloat(payload.chunkHeat);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
