package com.sevendaystominecraft.client.gui;

import com.sevendaystominecraft.block.workstation.WorkstationMenu;
import com.sevendaystominecraft.network.WorkstationRecipeListPayload;
import com.sevendaystominecraft.network.WorkstationRecipeSelectPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class WorkstationRecipeBookComponent {

    private static final int PANEL_WIDTH = 131;
    private static final int COLUMNS = 5;
    private static final int SLOT_SIZE = 25;
    private static final int HEADER_HEIGHT = 22;

    private boolean visible = false;
    private int leftPos;
    private int topPos;
    private int panelHeight;

    private List<WorkstationRecipeListPayload.Entry> allRecipes = new ArrayList<>();
    private List<WorkstationRecipeListPayload.Entry> filteredRecipes = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedIndex = -1;
    private String searchText = "";

    public WorkstationRecipeBookComponent(WorkstationMenu menu) {
    }

    public void init(int screenLeftPos, int screenTopPos, int screenHeight) {
        this.topPos = screenTopPos;
        this.panelHeight = screenHeight;
        this.leftPos = screenLeftPos - PANEL_WIDTH - 2;
    }

    public void setRecipes(List<WorkstationRecipeListPayload.Entry> entries) {
        this.allRecipes = new ArrayList<>(entries);
        applyFilter();
    }

    private void applyFilter() {
        if (searchText.isEmpty()) {
            filteredRecipes = new ArrayList<>(allRecipes);
        } else {
            String lower = searchText.toLowerCase();
            filteredRecipes = allRecipes.stream()
                    .filter(entry -> {
                        String name = entry.result().getHoverName().getString().toLowerCase();
                        return name.contains(lower);
                    })
                    .toList();
        }
        scrollOffset = 0;
        selectedIndex = -1;
    }

    public boolean isVisible() {
        return visible;
    }

    public void toggleVisibility() {
        visible = !visible;
    }

    public int getXOffset() {
        return visible ? PANEL_WIDTH + 2 : 0;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        graphics.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + panelHeight, 0xF0262626);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + PANEL_WIDTH - 1, topPos + panelHeight - 1, 0xF0383838);

        graphics.fill(leftPos + 3, topPos + 3, leftPos + PANEL_WIDTH - 3, topPos + HEADER_HEIGHT, 0xFF555555);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("Recipes"), leftPos + 5, topPos + 7, 0xFFFFFF, false);

        graphics.fill(leftPos + 3, topPos + HEADER_HEIGHT + 2, leftPos + PANEL_WIDTH - 3, topPos + HEADER_HEIGHT + 14, 0xFF222222);
        graphics.drawString(Minecraft.getInstance().font,
                searchText.isEmpty() ? Component.literal("Search...") : Component.literal(searchText),
                leftPos + 5, topPos + HEADER_HEIGHT + 4, searchText.isEmpty() ? 0x808080 : 0xFFFFFF, false);

        int gridTop = topPos + HEADER_HEIGHT + 18;
        int maxVisibleRows = (panelHeight - HEADER_HEIGHT - 18 - 4) / SLOT_SIZE;
        int maxVisible = maxVisibleRows * COLUMNS;

        for (int i = 0; i < maxVisible && (i + scrollOffset * COLUMNS) < filteredRecipes.size(); i++) {
            int recipeIdx = i + scrollOffset * COLUMNS;
            WorkstationRecipeListPayload.Entry entry = filteredRecipes.get(recipeIdx);
            ItemStack result = entry.result();

            int col = i % COLUMNS;
            int row = i / COLUMNS;
            int sx = leftPos + 3 + col * SLOT_SIZE;
            int sy = gridTop + row * SLOT_SIZE;

            boolean hovered = mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE;
            boolean selected = recipeIdx == selectedIndex;

            int bgColor = selected ? 0xFF4A6A2F : (hovered ? 0xFF4A4A4A : 0xFF2A2A2A);
            graphics.fill(sx, sy, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, bgColor);
            graphics.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 2, sy + SLOT_SIZE - 2, 0xFF1A1A1A);

            graphics.renderItem(result, sx + 4, sy + 4);
            if (result.getCount() > 1) {
                graphics.renderItemDecorations(Minecraft.getInstance().font, result, sx + 4, sy + 4);
            }

            if (hovered) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(result.getHoverName());
                tooltip.add(Component.literal(""));

                for (SizedIngredient sized : entry.ingredients()) {
                    String itemName = "???";
                    var itemStream = sized.ingredient().items();
                    var first = itemStream.findFirst();
                    if (first.isPresent()) {
                        itemName = new ItemStack(first.get()).getHoverName().getString();
                    }
                    tooltip.add(Component.literal("  " + sized.count() + "x " + itemName).withStyle(net.minecraft.ChatFormatting.GRAY));
                }

                if (entry.processingTicks() > 0) {
                    float seconds = entry.processingTicks() / 20.0f;
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal(String.format("Time: %.1fs", seconds)).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
                }

                graphics.renderTooltip(Minecraft.getInstance().font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
            }
        }

        if (selectedIndex >= 0 && selectedIndex < filteredRecipes.size()) {
            renderSelectedRecipeDetails(graphics, mouseX, mouseY);
        }
    }

    private void renderSelectedRecipeDetails(GuiGraphics graphics, int mouseX, int mouseY) {
        int detailY = topPos + panelHeight - 28;
        graphics.fill(leftPos + 3, detailY, leftPos + PANEL_WIDTH - 3, detailY + 24, 0xFF2E5A1E);
        graphics.fill(leftPos + 4, detailY + 1, leftPos + PANEL_WIDTH - 4, detailY + 23, 0xFF1A3A10);

        graphics.drawString(Minecraft.getInstance().font,
                Component.literal("Click to Fill"),
                leftPos + PANEL_WIDTH / 2 - 30, detailY + 8, 0xFFFFFF, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (mouseX < leftPos || mouseX > leftPos + PANEL_WIDTH || mouseY < topPos || mouseY > topPos + panelHeight) {
            return false;
        }

        int gridTop = topPos + HEADER_HEIGHT + 18;
        int maxVisibleRows = (panelHeight - HEADER_HEIGHT - 18 - 4) / SLOT_SIZE;
        int maxVisible = maxVisibleRows * COLUMNS;

        for (int i = 0; i < maxVisible && (i + scrollOffset * COLUMNS) < filteredRecipes.size(); i++) {
            int recipeIdx = i + scrollOffset * COLUMNS;
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            int sx = leftPos + 3 + col * SLOT_SIZE;
            int sy = gridTop + row * SLOT_SIZE;

            if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                if (selectedIndex == recipeIdx) {
                    sendRecipeSelect(filteredRecipes.get(recipeIdx));
                    selectedIndex = -1;
                } else {
                    selectedIndex = recipeIdx;
                }
                return true;
            }
        }

        if (selectedIndex >= 0 && selectedIndex < filteredRecipes.size()) {
            int detailY = topPos + panelHeight - 28;
            if (mouseX >= leftPos + 3 && mouseX < leftPos + PANEL_WIDTH - 3
                    && mouseY >= detailY && mouseY < detailY + 24) {
                sendRecipeSelect(filteredRecipes.get(selectedIndex));
                selectedIndex = -1;
                return true;
            }
        }

        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible) return false;
        if (mouseX < leftPos || mouseX > leftPos + PANEL_WIDTH) return false;

        int totalRows = (filteredRecipes.size() + COLUMNS - 1) / COLUMNS;
        int maxVisibleRows = (panelHeight - HEADER_HEIGHT - 18 - 4) / SLOT_SIZE;

        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
            return true;
        } else if (scrollY < 0 && scrollOffset < totalRows - maxVisibleRows) {
            scrollOffset++;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
            searchText = searchText.substring(0, searchText.length() - 1);
            applyFilter();
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            if (!searchText.isEmpty()) {
                searchText = "";
                applyFilter();
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!visible) return false;

        if (Character.isLetterOrDigit(codePoint) || codePoint == ' ' || codePoint == '_') {
            if (searchText.length() < 20) {
                searchText += codePoint;
                applyFilter();
                return true;
            }
        }
        return false;
    }

    private void sendRecipeSelect(WorkstationRecipeListPayload.Entry entry) {
        PacketDistributor.sendToServer(new WorkstationRecipeSelectPayload(entry.recipeId()));
    }
}
