package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CoinBagActionPayload(
        int action,
        int slot
) implements CustomPacketPayload {

    public static final int ACTION_EQUIP = 0;
    public static final int ACTION_UNEQUIP = 1;
    public static final int ACTION_CLICK_BAG_SLOT = 2;

    public static final Type<CoinBagActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "coin_bag_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CoinBagActionPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, payload.action);
                        ByteBufCodecs.VAR_INT.encode(buf, payload.slot);
                    },
                    buf -> {
                        int action = ByteBufCodecs.VAR_INT.decode(buf);
                        int slot = ByteBufCodecs.VAR_INT.decode(buf);
                        return new CoinBagActionPayload(action, slot);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
