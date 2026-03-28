package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncWaypointsPayload(List<WaypointData> waypoints) implements CustomPacketPayload {

    public static final Type<SyncWaypointsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_waypoints")
    );

    public record WaypointData(String name, int x, int z) {}

    public static final StreamCodec<ByteBuf, SyncWaypointsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.waypoints.size());
                for (WaypointData wp : payload.waypoints) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, wp.name);
                    buf.writeInt(wp.x);
                    buf.writeInt(wp.z);
                }
            },
            buf -> {
                int count = ByteBufCodecs.VAR_INT.decode(buf);
                List<WaypointData> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                    int x = buf.readInt();
                    int z = buf.readInt();
                    list.add(new WaypointData(name, x, z));
                }
                return new SyncWaypointsPayload(list);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
