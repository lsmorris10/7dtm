package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record CoinBagSyncPayload(
        ItemStack equippedCoinBag
) implements CustomPacketPayload {

    public static final Type<CoinBagSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "coin_bag_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CoinBagSyncPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.equippedCoinBag);
                    },
                    buf -> {
                        ItemStack equippedCoinBag = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                        return new CoinBagSyncPayload(equippedCoinBag);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
