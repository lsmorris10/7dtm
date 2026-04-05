package com.sevendaystominecraft.client;

import com.sevendaystominecraft.entity.npc.VanillaVillagerTraderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders VanillaVillagerTraderEntity using the player model with a vanilla villager texture.
 * Uses HumanoidModel (same approach as TraderRenderer) for compatibility with the render state system.
 */
public class VanillaVillagerTraderRenderer extends MobRenderer<VanillaVillagerTraderEntity, VanillaVillagerTraderRenderState, HumanoidModel<VanillaVillagerTraderRenderState>> {

    private static final ResourceLocation VILLAGER_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");

    public VanillaVillagerTraderRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(VanillaVillagerTraderRenderState state) {
        return VILLAGER_TEXTURE;
    }

    @Override
    public VanillaVillagerTraderRenderState createRenderState() {
        return new VanillaVillagerTraderRenderState();
    }

    @Override
    public void extractRenderState(VanillaVillagerTraderEntity entity, VanillaVillagerTraderRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.traderName = entity.getTraderName();
    }
}
