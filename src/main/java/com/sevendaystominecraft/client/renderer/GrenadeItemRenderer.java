package com.sevendaystominecraft.client.renderer;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.item.weapon.GrenadeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GrenadeItemRenderer extends GeoItemRenderer<GrenadeItem> {

    public GrenadeItemRenderer() {
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(
                SevenDaysToMinecraft.MOD_ID,
                "grenade"
        )));
    }
}
