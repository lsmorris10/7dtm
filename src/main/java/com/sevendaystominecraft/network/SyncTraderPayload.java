package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncTraderPayload(List<TraderEntry> traders) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncTraderPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_trader"));

    public record TraderEntry(int id, int x, int y, int z, int tier, String name) {}

    private static final StreamCodec<ByteBuf, TraderEntry> ENTRY_CODEC = StreamCodec.of(
            (buf, entry) -> {
                buf.writeInt(entry.id());
                buf.writeInt(entry.x());
                buf.writeInt(entry.y());
                buf.writeInt(entry.z());
                buf.writeInt(entry.tier());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.name());
            },
            buf -> {
                int id = buf.readInt();
                int x = buf.readInt();
                int y = buf.readInt();
                int z = buf.readInt();
                int tier = buf.readInt();
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                return new TraderEntry(id, x, y, z, tier, name);
            }
    );

    public static final StreamCodec<ByteBuf, SyncTraderPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.traders().size());
                for (TraderEntry entry : payload.traders()) {
                    ENTRY_CODEC.encode(buf, entry);
                }
            },
            buf -> {
                int count = buf.readInt();
                List<TraderEntry> entries = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    entries.add(ENTRY_CODEC.decode(buf));
                }
                return new SyncTraderPayload(entries);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
