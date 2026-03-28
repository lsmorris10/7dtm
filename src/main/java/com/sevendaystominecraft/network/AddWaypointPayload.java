package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AddWaypointPayload(String name, int x, int z) implements CustomPacketPayload {

    public static final Type<AddWaypointPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "add_waypoint")
    );

    public static final StreamCodec<ByteBuf, AddWaypointPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.name);
                buf.writeInt(payload.x);
                buf.writeInt(payload.z);
            },
            buf -> {
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                int x = buf.readInt();
                int z = buf.readInt();
                return new AddWaypointPayload(name, x, z);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
