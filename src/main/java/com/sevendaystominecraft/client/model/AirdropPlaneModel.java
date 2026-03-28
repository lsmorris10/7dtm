package com.sevendaystominecraft.client.model;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.entity.AirdropPlaneEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class AirdropPlaneModel extends GeoModel<AirdropPlaneEntity> {
    @Override
    public ResourceLocation getModelResource(AirdropPlaneEntity object, GeoRenderer<AirdropPlaneEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "geo/entity/airdrop_plane.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AirdropPlaneEntity object, GeoRenderer<AirdropPlaneEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/plane.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AirdropPlaneEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "animations/entity/plane.animation.json");
    }
}
