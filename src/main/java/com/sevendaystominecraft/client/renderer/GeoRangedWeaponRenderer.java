package com.sevendaystominecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GeoRangedWeaponRenderer extends GeoItemRenderer<GeoRangedWeaponItem> {

    private final String weaponName;

    public GeoRangedWeaponRenderer(GeoRangedWeaponItem item) {
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(
                SevenDaysToMinecraft.MOD_ID,
                getModelName(item)
        )));
        this.weaponName = getModelName(item);
    }

    private static String getModelName(GeoRangedWeaponItem item) {
        return switch (item.getWeaponType()) {
            case AK47 -> "ak47";
            case PISTOL_9MM -> "pistol_9mm";
            case SHOTGUN -> "shotgun";
            case SMG -> "smg";
            case HUNTING_RIFLE -> "hunting_rifle";
            case SNIPER_RIFLE -> "sniper_rifle";
            case M60 -> "m60";
        };
    }

    @Override
    public RenderType getRenderType(GeoRangedWeaponItem animatable, ResourceLocation texture,
                                     @org.jetbrains.annotations.Nullable MultiBufferSource bufferSource,
                                     float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public ResourceLocation getTextureLocation(GeoRangedWeaponItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID,
                "textures/item/" + weaponName + ".png");
    }

    @Override
    public void preRender(PoseStack poseStack, GeoRangedWeaponItem animatable, BakedGeoModel model,
                           MultiBufferSource bufferSource, VertexConsumer buffer,
                           boolean isReRender, float partialTick,
                           int packedLight, int packedOverlay, int renderColor) {
        if (!isReRender) {
            applyDisplayTransforms(poseStack, this.renderPerspective);
        }
        super.preRender(poseStack, animatable, model, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, renderColor);
    }

    private boolean isLongGun() {
        return switch (weaponName) {
            case "ak47", "shotgun", "hunting_rifle", "sniper_rifle", "m60" -> true;
            default -> false;
        };
    }

    private void applyDisplayTransforms(PoseStack poseStack, ItemDisplayContext context) {
        if (context == null) return;

        boolean longGun = isLongGun();
        float scale;

        switch (context) {
            case FIRST_PERSON_RIGHT_HAND:
                if (longGun) {
                    scale = 1.0f;
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0.1, -0.3, 0.4);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                } else {
                    scale = 0.5f;
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0.6, 0.4, -0.2);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                }
                break;
            case FIRST_PERSON_LEFT_HAND:
                if (longGun) {
                    scale = 1.0f;
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(-0.1, -0.3, 0.4);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                } else {
                    scale = 0.5f;
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(-0.6, 0.4, -0.2);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                }
                break;
            case THIRD_PERSON_RIGHT_HAND:
                if (longGun) {
                    scale = 0.8f;
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0.0, 0.1, 0.2);
                    poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                } else {
                    scale = 0.4f;
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0.0, 0.5, 0.0);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                }
                break;
            case THIRD_PERSON_LEFT_HAND:
                if (longGun) {
                    scale = 0.8f;
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0.0, 0.1, 0.2);
                    poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                } else {
                    scale = 0.4f;
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0.0, 0.5, 0.0);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                }
                break;
            case GUI:
                if (longGun) {
                    scale = 0.45f;
                } else {
                    scale = 0.5f;
                }
                poseStack.scale(scale, scale, scale);
                poseStack.translate(0.0, 0.5, 0.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(-15));
                poseStack.mulPose(Axis.YP.rotationDegrees(135));
                break;
            case GROUND:
                scale = longGun ? 0.4f : 0.35f;
                poseStack.scale(scale, scale, scale);
                poseStack.translate(0.0, 0.3, 0.0);
                break;
            case FIXED:
                scale = 0.45f;
                poseStack.scale(scale, scale, scale);
                poseStack.translate(0.0, 0.5, 0.0);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
            default:
                break;
        }
    }
}
