package com.sevendaystominecraft.network;

import java.util.ArrayList;
import java.util.List;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncNearbyPlayersPayload(
        List<NearbyPlayerEntry> players
) implements CustomPacketPayload {

    public static final Type<SyncNearbyPlayersPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_nearby_players")
    );

    private static final int MAX_PLAYERS = 64;

    public record NearbyPlayerEntry(String name, double x, double z) {}

    public static final StreamCodec<ByteBuf, SyncNearbyPlayersPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SyncNearbyPlayersPayload decode(ByteBuf buf) {
                    int count = Math.min(ByteBufCodecs.VAR_INT.decode(buf), MAX_PLAYERS);
                    List<NearbyPlayerEntry> players = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                        double x = buf.readFloat();
                        double z = buf.readFloat();
                        players.add(new NearbyPlayerEntry(name, x, z));
                    }
                    return new SyncNearbyPlayersPayload(players);
                }

                @Override
                public void encode(ByteBuf buf, SyncNearbyPlayersPayload payload) {
                    int count = Math.min(payload.players.size(), MAX_PLAYERS);
                    ByteBufCodecs.VAR_INT.encode(buf, count);
                    for (int i = 0; i < count; i++) {
                        NearbyPlayerEntry entry = payload.players.get(i);
                        ByteBufCodecs.STRING_UTF8.encode(buf, entry.name);
                        buf.writeFloat((float) entry.x);
                        buf.writeFloat((float) entry.z);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
