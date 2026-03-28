package com.sevendaystominecraft.client.model;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.entity.FallingAirdropEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class FallingAirdropModel extends GeoModel<FallingAirdropEntity> {
    @Override
    public ResourceLocation getModelResource(FallingAirdropEntity object, GeoRenderer<FallingAirdropEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "geo/entity/airdrop_parachute.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FallingAirdropEntity object, GeoRenderer<FallingAirdropEntity> renderer) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/parachute.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FallingAirdropEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "animations/entity/parachute.animation.json");
    }
}
