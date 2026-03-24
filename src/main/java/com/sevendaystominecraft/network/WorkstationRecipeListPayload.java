package com.sevendaystominecraft.network;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

public record WorkstationRecipeListPayload(List<Entry> entries) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WorkstationRecipeListPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "workstation_recipe_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstationRecipeListPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.entries().size());
                for (Entry entry : payload.entries()) {
                    buf.writeResourceLocation(entry.recipeId());
                    ItemStack.STREAM_CODEC.encode(buf, entry.result());
                    buf.writeVarInt(entry.processingTicks());
                    buf.writeVarInt(entry.ingredients().size());
                    for (SizedIngredient sized : entry.ingredients()) {
                        SizedIngredient.STREAM_CODEC.encode(buf, sized);
                    }
                }
            },
            buf -> {
                int count = buf.readVarInt();
                List<Entry> entries = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    ResourceLocation id = buf.readResourceLocation();
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    int ticks = buf.readVarInt();
                    int ingCount = buf.readVarInt();
                    List<SizedIngredient> ings = new ArrayList<>(ingCount);
                    for (int j = 0; j < ingCount; j++) {
                        ings.add(SizedIngredient.STREAM_CODEC.decode(buf));
                    }
                    entries.add(new Entry(id, result, ticks, ings));
                }
                return new WorkstationRecipeListPayload(entries);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Entry(ResourceLocation recipeId, ItemStack result, int processingTicks, List<SizedIngredient> ingredients) {
    }
}
