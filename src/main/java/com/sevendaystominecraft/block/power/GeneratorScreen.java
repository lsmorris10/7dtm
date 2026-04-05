package com.sevendaystominecraft.block.power;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {

    public GeneratorScreen(GeneratorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);

        this.imageWidth = 176;
        this.imageHeight = 166;

        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
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

        int burnTime = menu.getBurnTime();
        int burnTotal = menu.getBurnTimeTotal();
        int powerOutput = menu.getPowerOutput();

        int barX = leftPos + 30;
        int barY = topPos + 20;
        int barWidth = 50;
        int barHeight = 10;

        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        if (burnTotal > 0 && burnTime > 0) {
            int fillWidth = (int) ((float) burnTime / burnTotal * barWidth);
            graphics.fill(barX, barY, barX + fillWidth, barY + barHeight, 0xFFFF8800);
        }

        graphics.drawString(this.font, "Fuel", barX, barY - 10, 0xFFFFFF, false);

        String powerStr = "Power: " + powerOutput + " W";
        boolean active = burnTime > 0;
        String statusStr = active ? "ACTIVE" : "OFFLINE";
        int statusColor = active ? 0x00FF00 : 0xFF0000;

        graphics.drawString(this.font, powerStr, leftPos + 100, topPos + 22, 0xFFFFFF, false);
        graphics.drawString(this.font, statusStr, leftPos + 100, topPos + 34, statusColor, false);

        graphics.drawString(this.font, "Gas Can:", leftPos + 30, topPos + 50, 0x404040, false);
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
