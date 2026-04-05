package com.sevendaystominecraft.client.model;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.magazine.MagazineItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MagazineModel extends GeoModel<MagazineItem> {
    @Override
    public ResourceLocation getModelResource(MagazineItem animatable, software.bernie.geckolib.renderer.GeoRenderer<MagazineItem> renderer) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "geo/item/magazine.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MagazineItem animatable, software.bernie.geckolib.renderer.GeoRenderer<MagazineItem> renderer) {
        // This is a fallback; the renderer will override this based on seriesId
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/item/magazine/steady_steve.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MagazineItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "animations/item/magazine.animation.json");
    }
}
