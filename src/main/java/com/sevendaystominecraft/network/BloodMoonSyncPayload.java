package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BloodMoonSyncPayload(
        boolean active,
        int currentWave,
        int totalWaves,
        int dayNumber
) implements CustomPacketPayload {

    public static final Type<BloodMoonSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "blood_moon_sync")
    );

    public static final StreamCodec<ByteBuf, BloodMoonSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BloodMoonSyncPayload decode(ByteBuf buf) {
                    boolean active = buf.readBoolean();
                    int currentWave = buf.readInt();
                    int totalWaves = buf.readInt();
                    int dayNumber = buf.readInt();
                    return new BloodMoonSyncPayload(active, currentWave, totalWaves, dayNumber);
                }

                @Override
                public void encode(ByteBuf buf, BloodMoonSyncPayload payload) {
                    buf.writeBoolean(payload.active);
                    buf.writeInt(payload.currentWave);
                    buf.writeInt(payload.totalWaves);
                    buf.writeInt(payload.dayNumber);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
