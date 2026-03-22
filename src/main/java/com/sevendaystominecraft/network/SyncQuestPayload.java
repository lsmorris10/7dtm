package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncQuestPayload(List<QuestEntry> quests, String trackedQuestId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncQuestPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_quest"));

    public record QuestEntry(
            String questId,
            String typeName,
            String questName,
            String objectiveDescription,
            int targetCount,
            int progress,
            String stateName,
            int rewardXp,
            int rewardTokens,
            int traderId,
            boolean hasLocation,
            int locX, int locY, int locZ
    ) {}

    private static final StreamCodec<ByteBuf, QuestEntry> ENTRY_CODEC = StreamCodec.of(
            (buf, entry) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.questId());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.typeName());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.questName());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.objectiveDescription());
                buf.writeInt(entry.targetCount());
                buf.writeInt(entry.progress());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.stateName());
                buf.writeInt(entry.rewardXp());
                buf.writeInt(entry.rewardTokens());
                buf.writeInt(entry.traderId());
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
                int progress = buf.readInt();
                String stateName = ByteBufCodecs.STRING_UTF8.decode(buf);
                int rewardXp = buf.readInt();
                int rewardTokens = buf.readInt();
                int traderId = buf.readInt();
                boolean hasLocation = buf.readBoolean();
                int locX = buf.readInt();
                int locY = buf.readInt();
                int locZ = buf.readInt();
                return new QuestEntry(questId, typeName, questName, objectiveDescription,
                        targetCount, progress, stateName, rewardXp, rewardTokens, traderId,
                        hasLocation, locX, locY, locZ);
            }
    );

    public static final StreamCodec<ByteBuf, SyncQuestPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.quests().size());
                for (QuestEntry entry : payload.quests()) {
                    ENTRY_CODEC.encode(buf, entry);
                }
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.trackedQuestId());
            },
            buf -> {
                int count = buf.readInt();
                List<QuestEntry> entries = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    entries.add(ENTRY_CODEC.decode(buf));
                }
                String trackedId = ByteBufCodecs.STRING_UTF8.decode(buf);
                return new SyncQuestPayload(entries, trackedId);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
