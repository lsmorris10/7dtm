package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WorkstationRecipeRequestPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WorkstationRecipeRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "workstation_recipe_request"));

    public static final StreamCodec<ByteBuf, WorkstationRecipeRequestPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {},
            buf -> new WorkstationRecipeRequestPayload()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
