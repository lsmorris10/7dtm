package com.sevendaystominecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sevendaystominecraft.territory.TerritoryLabelEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.joml.Matrix4f;

public class TerritoryLabelRenderer extends EntityRenderer<TerritoryLabelEntity, TerritoryLabelRenderState> {

    private static final int COLOR_EASY = 0xFF44FF44;
    private static final int COLOR_MEDIUM = 0xFFFFCC00;
    private static final int COLOR_HARD = 0xFFFF4444;
    private static final int COLOR_CLEARED = 0xFF888888;

    public TerritoryLabelRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TerritoryLabelRenderState createRenderState() {
        return new TerritoryLabelRenderState();
    }

    @Override
    public void extractRenderState(TerritoryLabelEntity entity, TerritoryLabelRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.labelText = entity.getLabelText();
        state.tier = entity.getTerritoryTier();
        state.isCleared = entity.getLabelText().contains("[Cleared]");
    }

    @Override
    public void render(TerritoryLabelRenderState state, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        // Don't call super.render() — we handle ALL rendering ourselves
        // This bypasses the vanilla 64-block name tag distance cap

        String labelText = state.labelText;
        if (labelText == null || labelText.isEmpty()) return;

        // Strip section symbols for rendering (we use our own coloring)
        String cleanText = labelText.replaceAll("§[0-9a-fk-or]", "");

        Font font = Minecraft.getInstance().font;
        int textColor = state.isCleared ? COLOR_CLEARED : getTierColor(state.tier);

        // Calculate distance-based scale: larger text at distance so it remains readable
        double distSqr = state.distanceToCameraSq;
        double dist = Math.sqrt(distSqr);

        // Base scale + distance scaling: at 0 blocks = 0.025, at 128 blocks = ~0.1
        float baseScale = 0.025f;
        float distScale = (float) Math.max(baseScale, baseScale + dist * 0.0006f);
        // Cap the scale so it doesn't get absurdly large
        distScale = Math.min(distScale, 0.15f);

        poseStack.pushPose();
        // Position above the entity
        poseStack.translate(0.0f, 1.5f, 0.0f);
        // Billboard: face the camera
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(distScale, -distScale, distScale);

        Matrix4f matrix = poseStack.last().pose();

        int textWidth = font.width(cleanText);
        float textX = -textWidth / 2.0f;
        float textY = 0f;

        // Draw background
        int bgColor = 0x80000000;
        font.drawInBatch(cleanText, textX, textY, textColor, false, matrix,
                bufferSource, Font.DisplayMode.NORMAL, bgColor, packedLight);

        poseStack.popPose();
    }

    private static int getTierColor(int tier) {
        if (tier <= 2) return COLOR_EASY;
        if (tier == 3) return COLOR_MEDIUM;
        return COLOR_HARD;
    }
}
