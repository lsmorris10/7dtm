package com.sevendaystominecraft.client.renderer;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.item.weapon.GeoRangedWeaponItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GeoRangedWeaponRenderer extends GeoItemRenderer<GeoRangedWeaponItem> {

    public GeoRangedWeaponRenderer(GeoRangedWeaponItem item) {
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(
                SevenDaysToMinecraft.MOD_ID,
                getModelName(item)
        )));
    }

    private static String getModelName(GeoRangedWeaponItem item) {
        return switch (item.getWeaponType()) {
            case AK47 -> "ak47";
            case PISTOL_9MM -> "pistol_9mm";
        };
    }
}
