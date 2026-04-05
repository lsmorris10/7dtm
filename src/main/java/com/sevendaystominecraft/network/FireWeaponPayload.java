package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FireWeaponPayload(boolean aiming) implements CustomPacketPayload {

    public static final Type<FireWeaponPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "fire_weapon")
    );

    public static final StreamCodec<ByteBuf, FireWeaponPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public FireWeaponPayload decode(ByteBuf buf) {
                    boolean aiming = buf.readBoolean();
                    return new FireWeaponPayload(aiming);
                }

                @Override
                public void encode(ByteBuf buf, FireWeaponPayload payload) {
                    buf.writeBoolean(payload.aiming);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
