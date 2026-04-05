package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncTraderQuestsPayload(int traderId, List<TraderQuestEntry> quests) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncTraderQuestsPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_trader_quests"));

    public record TraderQuestEntry(
            String questId,
            String typeName,
            String questName,
            String objectiveDescription,
            int targetCount,
            int rewardXp,
            int rewardTokens,
            boolean hasLocation,
            int locX, int locY, int locZ
    ) {}

    private static final StreamCodec<ByteBuf, TraderQuestEntry> ENTRY_CODEC = StreamCodec.of(
            (buf, entry) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.questId());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.typeName());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.questName());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.objectiveDescription());
                buf.writeInt(entry.targetCount());
                buf.writeInt(entry.rewardXp());
                buf.writeInt(entry.rewardTokens());
                buf.writeBoolean(entry.hasLocation());
                buf.writeInt(entry.locX());
                buf.writeInt(entry.locY());
                buf.writeInt(entry.locZ());
            },
            buf -> {
                String questId = ByteBufCodecs.STRING_UTF8.decode(buf);
                String typeName = ByteBufCodecs.STRING_UTF8.decode(buf);
                String questName = ByteBufCodecs.STRING_UTF8.decode(buf);
                String objectiveDescription = ByteBufCodecs.STRING_UTF8.decode(buf);
                int targetCount = buf.readInt();
                int rewardXp = buf.readInt();
                int rewardTokens = buf.readInt();
                boolean hasLocation = buf.readBoolean();
                int locX = buf.readInt();
                int locY = buf.readInt();
                int locZ = buf.readInt();
                return new TraderQuestEntry(questId, typeName, questName, objectiveDescription,
                        targetCount, rewardXp, rewardTokens, hasLocation, locX, locY, locZ);
            }
    );

    public static final StreamCodec<ByteBuf, SyncTraderQuestsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.traderId());
                buf.writeInt(payload.quests().size());
                for (TraderQuestEntry entry : payload.quests()) {
                    ENTRY_CODEC.encode(buf, entry);
                }
            },
            buf -> {
                int traderId = buf.readInt();
                int count = buf.readInt();
                List<TraderQuestEntry> entries = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    entries.add(ENTRY_CODEC.decode(buf));
                }
                return new SyncTraderQuestsPayload(traderId, entries);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
