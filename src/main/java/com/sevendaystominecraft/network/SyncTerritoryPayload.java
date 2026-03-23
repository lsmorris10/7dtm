package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncTerritoryPayload(List<TerritoryEntry> territories) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncTerritoryPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_territory"));

    public record BuildingEntry(int x, int y, int z, String displayName) {}

    public record TerritoryEntry(int id, int x, int y, int z, int tier, String label,
                                  String typeName, List<BuildingEntry> buildings) {}

    private static final StreamCodec<ByteBuf, BuildingEntry> BUILDING_CODEC = StreamCodec.of(
            (buf, entry) -> {
                buf.writeInt(entry.x());
                buf.writeInt(entry.y());
                buf.writeInt(entry.z());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.displayName());
            },
            buf -> {
                int x = buf.readInt();
                int y = buf.readInt();
                int z = buf.readInt();
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                return new BuildingEntry(x, y, z, name);
            }
    );

    private static final StreamCodec<ByteBuf, TerritoryEntry> ENTRY_CODEC = StreamCodec.of(
            (buf, entry) -> {
                buf.writeInt(entry.id());
                buf.writeInt(entry.x());
                buf.writeInt(entry.y());
                buf.writeInt(entry.z());
                buf.writeInt(entry.tier());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.label());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.typeName());
                buf.writeInt(entry.buildings().size());
                for (BuildingEntry b : entry.buildings()) {
                    BUILDING_CODEC.encode(buf, b);
                }
            },
            buf -> {
                int id = buf.readInt();
                int x = buf.readInt();
                int y = buf.readInt();
                int z = buf.readInt();
                int tier = buf.readInt();
                String label = ByteBufCodecs.STRING_UTF8.decode(buf);
                String typeName = ByteBufCodecs.STRING_UTF8.decode(buf);
                int buildingCount = buf.readInt();
                List<BuildingEntry> buildings = new ArrayList<>(buildingCount);
                for (int i = 0; i < buildingCount; i++) {
                    buildings.add(BUILDING_CODEC.decode(buf));
                }
                return new TerritoryEntry(id, x, y, z, tier, label, typeName, buildings);
            }
    );

    public static final StreamCodec<ByteBuf, SyncTerritoryPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.territories().size());
                for (TerritoryEntry entry : payload.territories()) {
                    ENTRY_CODEC.encode(buf, entry);
                }
            },
            buf -> {
                int count = buf.readInt();
                List<TerritoryEntry> entries = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    entries.add(ENTRY_CODEC.decode(buf));
                }
                return new SyncTerritoryPayload(entries);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
