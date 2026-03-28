package com.sevendaystominecraft.client.renderer.entity;

import com.sevendaystominecraft.client.model.FallingAirdropModel;
import com.sevendaystominecraft.entity.FallingAirdropEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FallingAirdropRenderer extends GeoEntityRenderer<FallingAirdropEntity> {
    public FallingAirdropRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FallingAirdropModel());
        this.shadowRadius = 1.0f;
    }
}
