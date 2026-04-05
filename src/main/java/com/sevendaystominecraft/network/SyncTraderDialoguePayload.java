package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client payload that sends trader dialogue lines to display
 * in the trader screen's dialogue bubble instead of in chat.
 */
public record SyncTraderDialoguePayload(List<String> lines) implements CustomPacketPayload {

    public static final Type<SyncTraderDialoguePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_trader_dialogue"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTraderDialoguePayload> STREAM_CODEC =
            StreamCodec.of(SyncTraderDialoguePayload::encode, SyncTraderDialoguePayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SyncTraderDialoguePayload payload) {
        buf.writeInt(payload.lines.size());
        for (String line : payload.lines) {
            buf.writeUtf(line);
        }
    }

    private static SyncTraderDialoguePayload decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readInt();
        List<String> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            lines.add(buf.readUtf());
        }
        return new SyncTraderDialoguePayload(lines);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
