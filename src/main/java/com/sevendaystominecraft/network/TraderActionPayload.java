package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TraderActionPayload(int traderId, int actionIndex, boolean isBuy) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TraderActionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "trader_action"));

    public static final StreamCodec<ByteBuf, TraderActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.traderId());
                buf.writeInt(payload.actionIndex());
                buf.writeBoolean(payload.isBuy());
            },
            buf -> new TraderActionPayload(buf.readInt(), buf.readInt(), buf.readBoolean())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
