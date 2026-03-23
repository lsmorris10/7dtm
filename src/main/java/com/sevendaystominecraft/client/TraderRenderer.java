package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.trader.TraderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class TraderRenderer extends MobRenderer<TraderEntity, TraderRenderState, HumanoidModel<TraderRenderState>> {

    private static final Map<String, ResourceLocation> TRADER_TEXTURES = Map.of(
            "Trader Joel", ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/npc/trader_joel.png"),
            "Trader Rekt", ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/npc/trader_rekt.png"),
            "Trader Jen", ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/npc/trader_jen.png"),
            "Trader Bob", ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/npc/trader_bob.png"),
            "Trader Hugh", ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/npc/trader_hugh.png")
    );

    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/npc/trader_joel.png");

    public TraderRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TraderRenderState state) {
        return TRADER_TEXTURES.getOrDefault(state.traderName, DEFAULT_TEXTURE);
    }

    @Override
    public TraderRenderState createRenderState() {
        return new TraderRenderState();
    }

    @Override
    public void extractRenderState(TraderEntity entity, TraderRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.traderName = entity.getTraderName();
    }
}
