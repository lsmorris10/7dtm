package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RemoveWaypointPayload(int x, int z) implements CustomPacketPayload {

    public static final Type<RemoveWaypointPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "remove_waypoint")
    );

    public static final StreamCodec<ByteBuf, RemoveWaypointPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.x);
                buf.writeInt(payload.z);
            },
            buf -> {
                int x = buf.readInt();
                int z = buf.readInt();
                return new RemoveWaypointPayload(x, z);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
