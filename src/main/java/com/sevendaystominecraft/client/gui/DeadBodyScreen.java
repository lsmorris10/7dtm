package com.sevendaystominecraft.client.gui;

import com.sevendaystominecraft.menu.DeadBodyMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DeadBodyScreen extends AbstractContainerScreen<DeadBodyMenu> {

    private static final int BODY_SECTION_HEIGHT = 18 + 24 + 3 * 18 + 4 + 18;

    public DeadBodyScreen(DeadBodyMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = BODY_SECTION_HEIGHT + 14 + 76 + 14;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = BODY_SECTION_HEIGHT + 3;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + 16, 0xFF555555);

        for (var slot : menu.slots) {
            int sx = leftPos + slot.x - 1;
            int sy = topPos + slot.y - 1;
            graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
        }

        int armorLabelY = topPos + 18;
        graphics.drawString(this.font, "Armor", leftPos + 8 + 4 * 18 + 4, armorLabelY + 4, 0x404040, false);

        int hotbarLabelY = topPos + 18 + 24 + 3 * 18 + 4;
        graphics.drawString(this.font, "Hotbar", leftPos + 8 + 9 * 18 + 4, hotbarLabelY + 4, 0x404040, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
