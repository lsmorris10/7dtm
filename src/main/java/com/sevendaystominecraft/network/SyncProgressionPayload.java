package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record SyncProgressionPayload(List<String> completedNodeIds, int highestStage) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncProgressionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_progression"));

    public static final StreamCodec<ByteBuf, SyncProgressionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.completedNodeIds().size());
                for (String id : payload.completedNodeIds()) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, id);
                }
                buf.writeInt(payload.highestStage());
            },
            buf -> {
                int count = buf.readInt();
                List<String> ids = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    ids.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                }
                int highestStage = buf.readInt();
                return new SyncProgressionPayload(ids, highestStage);
            }
    );

    public Set<String> completedNodeIdSet() {
        return new HashSet<>(completedNodeIds);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
