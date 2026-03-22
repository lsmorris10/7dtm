package com.sevendaystominecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sevendaystominecraft.trader.TraderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;

public class TraderRenderer extends MobRenderer<TraderEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/steve.png");

    public TraderRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }
}
