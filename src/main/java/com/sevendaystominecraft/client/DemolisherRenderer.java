package com.sevendaystominecraft.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.sevendaystominecraft.SevenDaysToMinecraft;

public class DemolisherRenderer extends ScaledZombieRenderer {

    private static final ResourceLocation CHEST_GLOW =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/zombie/demolisher_chest_glow.png");

    public DemolisherRenderer(EntityRendererProvider.Context context, float scale, float nameTagExtraHeight) {
        super(context, scale, nameTagExtraHeight);
        this.addLayer(new ZombieEyeLayer(this, CHEST_GLOW));
    }
}
