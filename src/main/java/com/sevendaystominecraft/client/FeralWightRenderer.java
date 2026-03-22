package com.sevendaystominecraft.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.sevendaystominecraft.SevenDaysToMinecraft;

public class FeralWightRenderer extends ScaledZombieRenderer {

    private static final ResourceLocation FERAL_EYES =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/zombie/feral_eyes.png");

    public FeralWightRenderer(EntityRendererProvider.Context context, float scale, float nameTagExtraHeight) {
        super(context, scale, nameTagExtraHeight);
        this.addLayer(new ZombieEyeLayer(this, FERAL_EYES));
    }
}
