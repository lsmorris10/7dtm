package com.sevendaystominecraft.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.sevendaystominecraft.SevenDaysToMinecraft;

public class SpiderZombieRenderer extends ScaledZombieRenderer {

    private static final ResourceLocation SPIDER_EYES =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/entity/zombie/spider_eyes.png");

    public SpiderZombieRenderer(EntityRendererProvider.Context context, float scale, float nameTagExtraHeight) {
        super(context, scale, nameTagExtraHeight);
        this.addLayer(new ZombieEyeLayer(this, SPIDER_EYES));
    }
}
