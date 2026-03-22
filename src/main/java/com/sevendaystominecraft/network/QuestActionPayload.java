package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record QuestActionPayload(int traderId, String questId, int action) implements CustomPacketPayload {

    public static final int ACTION_ACCEPT = 0;
    public static final int ACTION_ABANDON = 1;
    public static final int ACTION_TURN_IN = 2;
    public static final int ACTION_TRACK = 3;

    public static final CustomPacketPayload.Type<QuestActionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "quest_action"));

    public static final StreamCodec<ByteBuf, QuestActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.traderId());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.questId());
                buf.writeInt(payload.action());
            },
            buf -> new QuestActionPayload(buf.readInt(), ByteBufCodecs.STRING_UTF8.decode(buf), buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
