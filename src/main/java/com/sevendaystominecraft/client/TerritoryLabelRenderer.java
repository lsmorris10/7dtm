package com.sevendaystominecraft.client;

import com.sevendaystominecraft.territory.TerritoryLabelEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class TerritoryLabelRenderer extends EntityRenderer<TerritoryLabelEntity, EntityRenderState> {

    public TerritoryLabelRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
