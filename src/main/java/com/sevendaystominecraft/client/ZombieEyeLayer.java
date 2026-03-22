package com.sevendaystominecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ZombieEyeLayer extends RenderLayer<ZombieRenderState, ZombieModel<ZombieRenderState>> {

    private final ResourceLocation eyeTexture;

    public ZombieEyeLayer(RenderLayerParent<ZombieRenderState, ZombieModel<ZombieRenderState>> parent,
                           ResourceLocation eyeTexture) {
        super(parent);
        this.eyeTexture = eyeTexture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       ZombieRenderState state, float yRot, float xRot) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.eyes(this.eyeTexture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
    }
}
