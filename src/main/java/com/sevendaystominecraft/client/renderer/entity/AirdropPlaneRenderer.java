package com.sevendaystominecraft.client.renderer.entity;

import com.sevendaystominecraft.client.model.AirdropPlaneModel;
import com.sevendaystominecraft.entity.AirdropPlaneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AirdropPlaneRenderer extends GeoEntityRenderer<AirdropPlaneEntity> {
    public AirdropPlaneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AirdropPlaneModel());
        this.shadowRadius = 1.5f;
    }
}
