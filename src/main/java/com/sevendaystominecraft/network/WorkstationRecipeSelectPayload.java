package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WorkstationRecipeSelectPayload(ResourceLocation recipeId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WorkstationRecipeSelectPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "workstation_recipe_select"));

    public static final StreamCodec<ByteBuf, WorkstationRecipeSelectPayload> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(WorkstationRecipeSelectPayload::new, WorkstationRecipeSelectPayload::recipeId);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
