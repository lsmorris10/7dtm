package com.sevendaystominecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

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

    @Override
    protected void renderNameTag(ZombieRenderState state, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        String fullText = displayName.getString();
        String[] lines = fullText.split("\n");

        if (lines.length >= 2) {
            Vec3 original = state.nameTagAttachment;

            state.nameTagAttachment = original != null ? original.add(0, 0.3, 0) : null;
            super.renderNameTag(state, Component.literal(lines[0]), poseStack, bufferSource, packedLight);

            state.nameTagAttachment = original;
            super.renderNameTag(state, Component.literal("\u00a7c" + lines[1]), poseStack, bufferSource, packedLight);
        } else {
            super.renderNameTag(state, displayName, poseStack, bufferSource, packedLight);
        }
    }
}
