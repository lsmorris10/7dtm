package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CrtTvActionPayload(int x, int y, int z, String action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CrtTvActionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "crt_tv_action"));

    public static final StreamCodec<ByteBuf, CrtTvActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.x());
                buf.writeInt(payload.y());
                buf.writeInt(payload.z());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.action());
            },
            buf -> new CrtTvActionPayload(
                    buf.readInt(), buf.readInt(), buf.readInt(),
                    ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
