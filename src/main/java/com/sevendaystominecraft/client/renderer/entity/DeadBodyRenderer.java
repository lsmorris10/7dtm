package com.sevendaystominecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sevendaystominecraft.entity.DeadBodyEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class DeadBodyRenderer extends EntityRenderer<DeadBodyEntity, EntityRenderState> {

    public DeadBodyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void render(EntityRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.1f, 0.0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.scale(0.8f, 0.1f, 1.8f);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/block/dark_oak_planks.png")));

        float x1 = -0.5f, y1 = -0.5f, z1 = -0.5f;
        float x2 = 0.5f, y2 = 0.5f, z2 = 0.5f;
        int overlay = OverlayTexture.NO_OVERLAY;

        renderQuad(consumer, poseStack, x1, y2, z1, x2, y2, z2, 0, 1, 0, packedLight, overlay);
        renderQuad(consumer, poseStack, x2, y1, z1, x1, y1, z2, 0, -1, 0, packedLight, overlay);
        renderQuad(consumer, poseStack, x1, y1, z2, x2, y2, z2, 0, 0, 1, packedLight, overlay);
        renderQuad(consumer, poseStack, x2, y1, z1, x1, y2, z1, 0, 0, -1, packedLight, overlay);
        renderQuad(consumer, poseStack, x1, y1, z1, x1, y2, z2, -1, 0, 0, packedLight, overlay);
        renderQuad(consumer, poseStack, x2, y1, z2, x2, y2, z1, 1, 0, 0, packedLight, overlay);

        poseStack.popPose();
        super.render(state, poseStack, bufferSource, packedLight);
    }

    private void renderQuad(VertexConsumer consumer, PoseStack poseStack,
                            float x1, float y1, float z1, float x2, float y2, float z2,
                            float nx, float ny, float nz, int light, int overlay) {
        PoseStack.Pose pose = poseStack.last();
        consumer.addVertex(pose, x1, y1, z1).setColor(0.45f, 0.3f, 0.2f, 1.0f).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y1, z1).setColor(0.45f, 0.3f, 0.2f, 1.0f).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(0.45f, 0.3f, 0.2f, 1.0f).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x1, y2, z2).setColor(0.45f, 0.3f, 0.2f, 1.0f).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
