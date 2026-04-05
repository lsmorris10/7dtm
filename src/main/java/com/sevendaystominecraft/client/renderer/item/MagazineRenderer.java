package com.sevendaystominecraft.client.renderer.item;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.client.model.MagazineModel;
import com.sevendaystominecraft.magazine.MagazineItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MagazineRenderer extends GeoItemRenderer<MagazineItem> {
    public MagazineRenderer() {
        super(new MagazineModel());
    }

    @Override
    public ResourceLocation getTextureLocation(MagazineItem animatable) {
        String seriesId = animatable.getSeriesId();
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/item/magazine/" + seriesId + ".png");
    }
}
