package com.sevendaystominecraft.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Render state that carries territory label data from the entity to the renderer.
 */
public class TerritoryLabelRenderState extends EntityRenderState {
    public String labelText = "";
    public int tier = 1;
    public boolean isCleared = false;
}
