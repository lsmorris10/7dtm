package com.sevendaystominecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

public class CrawlerZombieRenderer extends ScaledZombieRenderer {

    public CrawlerZombieRenderer(EntityRendererProvider.Context context, float scale, float nameTagExtraHeight) {
        super(context, scale, nameTagExtraHeight);
    }

    @Override
    protected void scale(ZombieRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        // Rotate entire model to lay flat (face-down crawling)
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, -0.3F, 0.6F);
    }

    @Override
    public void render(ZombieRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Adjust arm and head poses before rendering
        ZombieModel<ZombieRenderState> zombieModel = (ZombieModel<ZombieRenderState>) this.getModel();

        // Tilt head back so the crawler looks up at the player (-60 degrees)
        zombieModel.head.xRot = (float) Math.toRadians(-60.0);

        // Raise both arms up and slightly outward (reaching forward while crawling)
        zombieModel.leftArm.xRot = (float) Math.toRadians(-160.0);
        zombieModel.leftArm.zRot = (float) Math.toRadians(15.0);
        zombieModel.rightArm.xRot = (float) Math.toRadians(-160.0);
        zombieModel.rightArm.zRot = (float) Math.toRadians(-15.0);

        super.render(state, poseStack, bufferSource, packedLight);
    }
}
