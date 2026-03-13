package com.sevendaystominecraft.block.workstation;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WorkstationScreen extends AbstractContainerScreen<WorkstationMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SevenDaysToMinecraft.MOD_ID, "textures/gui/workstation.png");

    public WorkstationScreen(WorkstationMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);

        graphics.fill(leftPos + 7, topPos + 7, leftPos + imageWidth - 7, topPos + 14, 0xFF555555);

        WorkstationBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            WorkstationType type = be.getWorkstationType();

            for (int i = 0; i < type.getInputSlots(); i++) {
                int col = i % 3;
                int row = i / 3;
                int sx = leftPos + 25 + col * 18;
                int sy = topPos + 16 + row * 18;
                graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
            }

            int outputStartX = 115;
            for (int i = 0; i < type.getOutputSlots(); i++) {
                int col = i % 3;
                int row = i / 3;
                int sx = leftPos + outputStartX + col * 18;
                int sy = topPos + 16 + row * 18;
                graphics.fill(sx, sy, sx + 18, sy + 18, 0xFFBC9862);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
            }

            if (type.usesFuel()) {
                for (int i = 0; i < type.getFuelSlots(); i++) {
                    int sx = leftPos + 25 + i * 18;
                    int sy = topPos + 52;
                    graphics.fill(sx, sy, sx + 18, sy + 18, 0xFFCC4400);
                    graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
                }
            }

            graphics.fill(leftPos + 75, topPos + 25, leftPos + 100, topPos + 40, 0xFF555555);
            if (menu.getData().get(3) > 0) {
                int progress = menu.getData().get(2) * 25 / menu.getData().get(3);
                graphics.fill(leftPos + 75, topPos + 25, leftPos + 75 + progress, topPos + 40, 0xFF00CC00);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = leftPos + 7 + col * 18;
                int sy = topPos + 83 + row * 18;
                graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
            }
        }
        for (int col = 0; col < 9; col++) {
            int sx = leftPos + 7 + col * 18;
            int sy = topPos + 141;
            graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
