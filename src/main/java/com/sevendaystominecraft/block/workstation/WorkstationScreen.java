package com.sevendaystominecraft.block.workstation;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.client.gui.WorkstationRecipeBookComponent;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WorkstationScreen extends AbstractContainerScreen<WorkstationMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SevenDaysToMinecraft.MOD_ID, "textures/gui/workstation.png");

    private final int workstationBottom;
    private final int fuelY;
    private WorkstationRecipeBookComponent recipeBook;
    private int recipeBookButtonX;
    private int recipeBookButtonY;

    public WorkstationScreen(WorkstationMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);

        WorkstationBlockEntity be = menu.getBlockEntity();
        WorkstationType type = be != null ? be.getWorkstationType() : WorkstationType.CAMPFIRE;

        int inputRows = (int) Math.ceil((double) type.getInputSlots() / 3.0);
        int outputRows = (int) Math.ceil((double) type.getOutputSlots() / 3.0);
        int maxSlotRows = Math.max(inputRows, outputRows);
        int slotAreaBottom = 17 + maxSlotRows * 18;

        if (type.usesFuel()) {
            this.fuelY = slotAreaBottom + 4;
            this.workstationBottom = fuelY + 18;
        } else {
            this.fuelY = -1;
            this.workstationBottom = slotAreaBottom;
        }

        int playerInvY = workstationBottom + 14;
        this.imageWidth = 176;
        this.imageHeight = playerInvY + 58 + 18 + 7;

        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = workstationBottom + 3;
    }

    @Override
    protected void init() {
        super.init();
        recipeBook = new WorkstationRecipeBookComponent(this.menu);
        recipeBook.init(this.leftPos, this.topPos, this.imageHeight);
        this.menu.setRecipeListConsumer(recipeBook::setRecipes);
        recipeBookButtonX = leftPos + imageWidth - 22;
        recipeBookButtonY = topPos + 4;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= recipeBookButtonX && mouseX < recipeBookButtonX + 16
                && mouseY >= recipeBookButtonY && mouseY < recipeBookButtonY + 12) {
            recipeBook.toggleVisibility();
            if (recipeBook.isVisible()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new com.sevendaystominecraft.network.WorkstationRecipeRequestPayload());
            }
            updateRecipeBookPosition();
            return true;
        }

        if (recipeBook != null && recipeBook.isVisible()) {
            if (recipeBook.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (recipeBook != null && recipeBook.isVisible()) {
            if (recipeBook.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (recipeBook != null && recipeBook.isVisible()) {
            if (recipeBook.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (recipeBook != null && recipeBook.isVisible()) {
            if (recipeBook.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void updateRecipeBookPosition() {
        if (recipeBook != null) {
            recipeBook.init(this.leftPos, this.topPos, this.imageHeight);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);

        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + 16, 0xFF555555);

        int btnColor = (recipeBook != null && recipeBook.isVisible()) ? 0xFF4A6A2F : 0xFF6A6A6A;
        graphics.fill(recipeBookButtonX, recipeBookButtonY, recipeBookButtonX + 16, recipeBookButtonY + 12, btnColor);
        graphics.drawString(this.font, Component.literal("\u2261"), recipeBookButtonX + 4, recipeBookButtonY + 2, 0xFFFFFF, false);

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

            if (type.usesFuel() && fuelY >= 0) {
                for (int i = 0; i < type.getFuelSlots(); i++) {
                    int sx = leftPos + 25 + i * 18;
                    int sy = topPos + fuelY - 1;
                    graphics.fill(sx, sy, sx + 18, sy + 18, 0xFFCC4400);
                    graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
                }

                int flameX = leftPos + 26 + type.getFuelSlots() * 18 + 4;
                int flameY = topPos + fuelY;
                int burnTotal = menu.getData().get(1);
                int burnCurrent = menu.getData().get(0);
                graphics.fill(flameX, flameY, flameX + 14, flameY + 14, 0xFF555555);
                if (burnTotal > 0 && burnCurrent > 0) {
                    int flameHeight = burnCurrent * 14 / burnTotal;
                    graphics.fill(flameX, flameY + 14 - flameHeight, flameX + 14, flameY + 14, 0xFFFF6600);
                }
            }

            int inputRows = (int) Math.ceil((double) type.getInputSlots() / 3.0);
            int outputRows = (int) Math.ceil((double) type.getOutputSlots() / 3.0);
            int maxSlotRows = Math.max(inputRows, outputRows);
            int progressY = 17 + (maxSlotRows * 18 - 15) / 2;
            graphics.fill(leftPos + 75, topPos + progressY, leftPos + 100, topPos + progressY + 15, 0xFF555555);
            if (menu.getData().get(3) > 0) {
                int progress = menu.getData().get(2) * 25 / menu.getData().get(3);
                graphics.fill(leftPos + 75, topPos + progressY, leftPos + 75 + progress, topPos + progressY + 15, 0xFF00CC00);
            }
        }

        int playerInvY = menu.getPlayerInvY();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = leftPos + 7 + col * 18;
                int sy = topPos + playerInvY - 1 + row * 18;
                graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
            }
        }
        for (int col = 0; col < 9; col++) {
            int sx = leftPos + 7 + col * 18;
            int sy = topPos + playerInvY + 57;
            graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF373737);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (recipeBook != null && recipeBook.isVisible()) {
            recipeBook.render(graphics, mouseX, mouseY, partialTick);
        }

        renderTooltip(graphics, mouseX, mouseY);
    }
}
