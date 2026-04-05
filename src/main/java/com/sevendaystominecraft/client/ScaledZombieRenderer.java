package com.sevendaystominecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ScaledZombieRenderer extends ZombieRenderer {
    private final float scale;
    private final float nameTagExtraHeight;

    public ScaledZombieRenderer(EntityRendererProvider.Context context, float scale, float nameTagExtraHeight) {
        super(context);
        this.scale = scale;
        this.nameTagExtraHeight = nameTagExtraHeight;
    }

    @Override
    protected void scale(ZombieRenderState state, PoseStack poseStack) {
        poseStack.scale(scale, scale, scale);
        super.scale(state, poseStack);
    }

    @Override
    public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (state.nameTagAttachment != null) {
            state.nameTagAttachment = state.nameTagAttachment.add(0, nameTagExtraHeight, 0);
        }
    }

    // Uses Font.DisplayMode.NORMAL instead of SEE_THROUGH to prevent name tags from rendering through walls
    private void renderNameTagLine(ZombieRenderState state, Component text, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Vec3 vec3 = state.nameTagAttachment;
        if (vec3 != null) {
            poseStack.pushPose();
            poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();
            Font font = this.getFont();
            float f = (float)(-font.width(text)) / 2.0F;
            int bgOpacity = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            font.drawInBatch(
                text, f, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, bgOpacity, LightTexture.lightCoordsWithEmission(packedLight, 2)
            );
            poseStack.popPose();
        }
    }

    @Override
    protected void renderNameTag(ZombieRenderState state, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        String fullText = displayName.getString();
        String[] lines = fullText.split("\n");

        if (lines.length >= 2) {
            Vec3 original = state.nameTagAttachment;

            state.nameTagAttachment = original != null ? original.add(0, 0.3, 0) : null;
            renderNameTagLine(state, Component.literal(lines[0]), poseStack, bufferSource, packedLight);

            state.nameTagAttachment = original;
            renderNameTagLine(state, Component.literal("\u00a7c" + lines[1]), poseStack, bufferSource, packedLight);
        } else {
            renderNameTagLine(state, displayName, poseStack, bufferSource, packedLight);
        }
    }
}
