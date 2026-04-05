package com.sevendaystominecraft.network;

import java.util.HashMap;
import java.util.Map;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncPlayerStatsPayload(
        float food,
        float maxFood,
        float water,
        float maxWater,
        float stamina,
        float maxStamina,
        boolean staminaExhausted,
        float coreTemp,
        Map<String, Integer> debuffs,
        int xp,
        int level,
        int perkPoints,
        int attributePoints,
        Map<String, Integer> activePerks,
        int[] attributeLevels,
        long unkillableCooldownEnd
) implements CustomPacketPayload {

    public static final Type<SyncPlayerStatsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "sync_player_stats")
    );

    public static final StreamCodec<ByteBuf, SyncPlayerStatsPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SyncPlayerStatsPayload decode(ByteBuf buf) {
                    float food = buf.readFloat();
                    float maxFood = buf.readFloat();
                    float water = buf.readFloat();
                    float maxWater = buf.readFloat();
                    float stamina = buf.readFloat();
                    float maxStamina = buf.readFloat();
                    boolean staminaExhausted = buf.readBoolean();
                    float coreTemp = buf.readFloat();

                    int count = ByteBufCodecs.VAR_INT.decode(buf);
                    Map<String, Integer> debuffs = new HashMap<>();
                    for (int i = 0; i < count; i++) {
                        String id = ByteBufCodecs.STRING_UTF8.decode(buf);
                        int ticks = ByteBufCodecs.VAR_INT.decode(buf);
                        debuffs.put(id, ticks);
                    }

                    int xp = ByteBufCodecs.VAR_INT.decode(buf);
                    int level = ByteBufCodecs.VAR_INT.decode(buf);
                    int perkPoints = ByteBufCodecs.VAR_INT.decode(buf);
                    int attributePoints = ByteBufCodecs.VAR_INT.decode(buf);

                    int perkCount = ByteBufCodecs.VAR_INT.decode(buf);
                    Map<String, Integer> activePerks = new HashMap<>();
                    for (int i = 0; i < perkCount; i++) {
                        String id = ByteBufCodecs.STRING_UTF8.decode(buf);
                        int rank = ByteBufCodecs.VAR_INT.decode(buf);
                        activePerks.put(id, rank);
                    }

                    int attrCount = ByteBufCodecs.VAR_INT.decode(buf);
                    int[] attributeLevels = new int[attrCount];
                    for (int i = 0; i < attrCount; i++) {
                        attributeLevels[i] = ByteBufCodecs.VAR_INT.decode(buf);
                    }

                    long unkillableCooldownEnd = buf.readLong();

                    return new SyncPlayerStatsPayload(food, maxFood, water, maxWater,
                            stamina, maxStamina, staminaExhausted, coreTemp, debuffs,
                            xp, level, perkPoints, attributePoints, activePerks, attributeLevels,
                            unkillableCooldownEnd);
                }

                @Override
                public void encode(ByteBuf buf, SyncPlayerStatsPayload payload) {
                    buf.writeFloat(payload.food);
                    buf.writeFloat(payload.maxFood);
                    buf.writeFloat(payload.water);
                    buf.writeFloat(payload.maxWater);
                    buf.writeFloat(payload.stamina);
                    buf.writeFloat(payload.maxStamina);
                    buf.writeBoolean(payload.staminaExhausted);
                    buf.writeFloat(payload.coreTemp);

                    ByteBufCodecs.VAR_INT.encode(buf, payload.debuffs.size());
                    for (Map.Entry<String, Integer> entry : payload.debuffs.entrySet()) {
                        ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
                        ByteBufCodecs.VAR_INT.encode(buf, entry.getValue());
                    }

                    ByteBufCodecs.VAR_INT.encode(buf, payload.xp);
                    ByteBufCodecs.VAR_INT.encode(buf, payload.level);
                    ByteBufCodecs.VAR_INT.encode(buf, payload.perkPoints);
                    ByteBufCodecs.VAR_INT.encode(buf, payload.attributePoints);

                    ByteBufCodecs.VAR_INT.encode(buf, payload.activePerks.size());
                    for (Map.Entry<String, Integer> entry : payload.activePerks.entrySet()) {
                        ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
                        ByteBufCodecs.VAR_INT.encode(buf, entry.getValue());
                    }

                    ByteBufCodecs.VAR_INT.encode(buf, payload.attributeLevels.length);
                    for (int attrLevel : payload.attributeLevels) {
                        ByteBufCodecs.VAR_INT.encode(buf, attrLevel);
                    }

                    buf.writeLong(payload.unkillableCooldownEnd);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
