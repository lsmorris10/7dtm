package com.sevendaystominecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sevendaystominecraft.quest.ProgressionNode;
import com.sevendaystominecraft.quest.ProgressionStage;
import com.sevendaystominecraft.quest.ProgressionTreeRegistry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

/**
 * Interactive visual progression tree screen showing survival stages as a node graph.
 * Supports panning and zooming (scroll wheel). Nodes glow when available.
 */
public class ProgressionTreeScreen extends Screen {

    // Layout constants
    private static final int NODE_WIDTH = 120;
    private static final int NODE_HEIGHT = 44;
    private static final int NODE_SPACING_X = 180;
    private static final int NODE_SPACING_Y = 58;
    private static final int STAGE_HEADER_HEIGHT = 36;
    private static final int PADDING = 20;

    // Colors
    private static final int COMPLETED_BG = 0xCC1A331A;
    private static final int COMPLETED_BORDER = 0xFF22CC44;
    private static final int AVAILABLE_BG = 0xCC1A1A33;
    private static final int LOCKED_BG = 0xCC1A1A1A;
    private static final int LOCKED_BORDER = 0xFF444444;
    private static final int HEADER_BG = 0xDD111122;
    private static final int HEADER_BORDER = 0xFF666699;
    private static final int TITLE_COLOR = 0xFFFFCC00;
    private static final int SUBTITLE_COLOR = 0xFF999999;
    private static final int NAME_COLOR = 0xFFEEEEEE;
    private static final int DESC_COLOR = 0xFFAAAAAA;
    private static final int REWARD_COLOR = 0xFFFFAA00;
    private static final int CONNECTION_COMPLETE = 0xFF22CC44;
    private static final int CONNECTION_AVAILABLE = 0xFF4488FF;
    private static final int CONNECTION_LOCKED = 0xFF333333;
    private static final int PROGRESS_BAR_BG = 0xFF222233;
    private static final int PROGRESS_BAR_FILL = 0xFF22CC44;

    // Pan/zoom state
    private float panX = -50;
    private float panY = -30;
    private float zoom = 1.0f;
    private boolean dragging = false;
    private double dragStartX, dragStartY;
    private float dragStartPanX, dragStartPanY;

    // Tooltip
    private ProgressionNode hoveredNode = null;

    // Animation
    private long openTime;
    private static final float PULSE_SPEED = 0.05f;

    public ProgressionTreeScreen() {
        super(Component.literal("Progression Tree"));
    }

    @Override
    protected void init() {
        super.init();
        openTime = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Full-screen dark background
        graphics.fill(0, 0, width, height, 0xDD000008);

        Set<String> completed = ProgressionClientState.getCompletedNodes();
        int totalNodes = ProgressionTreeRegistry.getTotalNodeCount();
        int completedCount = 0;
        for (ProgressionNode node : ProgressionTreeRegistry.getAllNodes()) {
            if (completed.contains(node.getId())) completedCount++;
        }

        float elapsed = (System.currentTimeMillis() - openTime) / 1000f;
        float pulse = (float) (0.5f + 0.5f * Math.sin(elapsed * PULSE_SPEED * Math.PI * 2));

        // ── Render connections first (behind nodes) ─────────────────────
        hoveredNode = null;
        for (ProgressionNode node : ProgressionTreeRegistry.getAllNodes()) {
            for (String prereqId : node.getPrerequisiteIds()) {
                ProgressionNode prereq = ProgressionTreeRegistry.getNode(prereqId);
                if (prereq == null) continue;
                renderConnection(graphics, prereq, node, completed);
            }
        }

        // ── Render stage headers ────────────────────────────────────────
        for (ProgressionStage stage : ProgressionStage.values()) {
            renderStageHeader(graphics, stage, completed);
        }

        // ── Render nodes ────────────────────────────────────────────────
        for (ProgressionNode node : ProgressionTreeRegistry.getAllNodes()) {
            boolean isCompleted = completed.contains(node.getId());
            boolean isAvailable = !isCompleted && ProgressionTreeRegistry.arePrerequisitesMet(node, completed);
            renderNode(graphics, node, isCompleted, isAvailable, mouseX, mouseY, pulse);
        }

        // ── Render top HUD bar ──────────────────────────────────────────
        renderTopBar(graphics, completedCount, totalNodes, completed);

        // ── Render tooltip ──────────────────────────────────────────────
        if (hoveredNode != null) {
            renderNodeTooltip(graphics, hoveredNode, mouseX, mouseY, completed);
        }

        // ── Render navigation hint ──────────────────────────────────────
        String hint = "§7Drag to pan  •  Scroll to zoom  •  K to close";
        graphics.drawString(font, hint, (width - font.width(hint)) / 2, height - 14, 0xFF555555, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // ════════════════════════════════════════════════════════════════════
    // Top progress bar
    // ════════════════════════════════════════════════════════════════════

    private void renderTopBar(GuiGraphics graphics, int completed, int total, Set<String> completedSet) {
        int barW = Math.min(400, width - 40);
        int barX = (width - barW) / 2;
        int barY = 8;
        int barH = 24;

        // Background
        graphics.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, 0xFF333344);
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xDD111122);

        // Title
        String title = "§6§lSURVIVAL PROGRESSION";
        graphics.drawString(font, title, barX + 6, barY + 3, TITLE_COLOR, true);

        // Progress text
        int highestStage = ProgressionTreeRegistry.getHighestCompletedStage(completedSet);
        String progress = completed + "/" + total + " §7nodes";
        if (highestStage > 0) {
            progress += " §8| §aStage " + highestStage + " Complete";
        }
        graphics.drawString(font, progress, barX + barW - font.width(progress) - 4, barY + 3, 0xFFCCCCCC, true);

        // Progress bar
        int pbX = barX + 6;
        int pbY = barY + barH - 6;
        int pbW = barW - 12;
        int pbH = 3;
        graphics.fill(pbX, pbY, pbX + pbW, pbY + pbH, PROGRESS_BAR_BG);
        if (total > 0) {
            int fillW = (int) ((float) completed / total * pbW);
            graphics.fill(pbX, pbY, pbX + fillW, pbY + pbH, PROGRESS_BAR_FILL);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Stage headers
    // ════════════════════════════════════════════════════════════════════

    private void renderStageHeader(GuiGraphics graphics, ProgressionStage stage, Set<String> completed) {
        int sx = getScreenX(stage.getNumber() - 1, -1);
        int sy = getScreenY(-1);

        int hw = (int) (NODE_WIDTH * zoom) + 20;
        int hh = (int) (STAGE_HEADER_HEIGHT * zoom);

        int x = sx - hw / 2;
        int y = sy - hh / 2;

        boolean stageComplete = ProgressionTreeRegistry.isStageComplete(stage, completed);
        int borderColor = stageComplete ? COMPLETED_BORDER : HEADER_BORDER;

        // Border
        graphics.fill(x - 1, y - 1, x + hw + 1, y + hh + 1, borderColor);
        graphics.fill(x, y, x + hw, y + hh, HEADER_BG);

        // Stage name
        String stageName = "§l" + stage.getDisplayName();
        if (stageComplete) stageName = "§a" + stageName;
        else stageName = "§e" + stageName;


        if (zoom >= 0.6f) {
            graphics.drawString(font, stageName,
                    x + (hw - font.width(stageName)) / 2,
                    y + 4, TITLE_COLOR, true);

            // Subtitle
            String sub = "Stage " + stage.getNumber() + " — " + stage.getSubtitle();
            if (font.width(sub) > hw - 8) {
                sub = font.plainSubstrByWidth(sub, hw - 14) + "..";
            }
            graphics.drawString(font, sub,
                    x + (hw - font.width(sub)) / 2,
                    y + 4 + font.lineHeight + 2, SUBTITLE_COLOR, true);

            // Node count
            int stageCompleted = 0;
            int stageTotal = ProgressionTreeRegistry.getNodeCountForStage(stage);
            for (ProgressionNode node : ProgressionTreeRegistry.getNodesForStage(stage)) {
                if (completed.contains(node.getId())) stageCompleted++;
            }
            String countText = stageCompleted + "/" + stageTotal;
            int countColor = stageCompleted == stageTotal ? 0xFF22CC44 : 0xFF888888;
            graphics.drawString(font, countText,
                    x + (hw - font.width(countText)) / 2,
                    y + hh - font.lineHeight - 2, countColor, true);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Node rendering
    // ════════════════════════════════════════════════════════════════════

    private void renderNode(GuiGraphics graphics, ProgressionNode node,
                            boolean isCompleted, boolean isAvailable,
                            int mouseX, int mouseY, float pulse) {
        int sx = getScreenX(node.getGridX(), node.getGridY());
        int sy = getScreenY(node.getGridY());

        int nw = (int) (NODE_WIDTH * zoom);
        int nh = (int) (NODE_HEIGHT * zoom);
        int x = sx - nw / 2;
        int y = sy - nh / 2;

        // Determine colors
        int bgColor, borderColor;
        if (isCompleted) {
            bgColor = COMPLETED_BG;
            borderColor = COMPLETED_BORDER;
        } else if (isAvailable) {
            bgColor = AVAILABLE_BG;
            // Pulsing border for available nodes
            int pulseAlpha = (int) (180 + 75 * pulse);
            borderColor = (pulseAlpha << 24) | (0x44 << 16) | (0x88 << 8) | 0xFF;
        } else {
            bgColor = LOCKED_BG;
            borderColor = LOCKED_BORDER;
        }

        // Check hover
        boolean hovered = mouseX >= x && mouseX <= x + nw && mouseY >= y && mouseY <= y + nh;
        if (hovered) {
            hoveredNode = node;
            bgColor = brighten(bgColor, 20);
            borderColor = brighten(borderColor, 30);
        }

        // Glow effect for available nodes
        if (isAvailable && !hovered) {
            int glowAlpha = (int) (20 + 30 * pulse);
            int glowColor = (glowAlpha << 24) | (0x44 << 16) | (0x88 << 8) | 0xFF;
            graphics.fill(x - 3, y - 3, x + nw + 3, y + nh + 3, glowColor);
        }

        // Border + background
        graphics.fill(x - 1, y - 1, x + nw + 1, y + nh + 1, borderColor);
        graphics.fill(x, y, x + nw, y + nh, bgColor);

        // Category color bar on left
        int catBarW = Math.max(2, (int) (3 * zoom));
        graphics.fill(x, y, x + catBarW, y + nh, node.getCategory().getColor());

        if (zoom >= 0.5f) {
            int textX = x + catBarW + 3;
            int textY = y + 3;
            int maxTextW = nw - catBarW - 6;

            // Node icon (item)
            if (zoom >= 0.7f) {
                ItemStack iconStack = getIconItem(node.getIconItemId());
                if (!iconStack.isEmpty()) {
                    int iconSize = (int) (16 * Math.min(1f, zoom));
                    graphics.renderItem(iconStack, x + nw - iconSize - 3, y + 3);
                    maxTextW -= iconSize + 2;
                }
            }

            // Node name
            String name = node.getDisplayName();
            if (isCompleted) name = "§a✓ " + name;
            if (font.width(name) > maxTextW) {
                name = font.plainSubstrByWidth(name, maxTextW - 6) + "..";
            }
            graphics.drawString(font, name, textX, textY, NAME_COLOR, true);

            // Description (smaller)
            if (zoom >= 0.7f && nh > font.lineHeight * 2 + 8) {
                String desc = node.getDescription();
                if (font.width(desc) > nw - catBarW - 8) {
                    desc = font.plainSubstrByWidth(desc, nw - catBarW - 14) + "..";
                }
                graphics.drawString(font, desc, textX, textY + font.lineHeight + 2, DESC_COLOR, true);
            }

            // Reward text at bottom
            if (zoom >= 0.8f && nh > font.lineHeight * 3 + 8) {
                String reward = node.getRewardText();
                if (!reward.isEmpty()) {
                    if (font.width(reward) > nw - catBarW - 8) {
                        reward = font.plainSubstrByWidth(reward, nw - catBarW - 14) + "..";
                    }
                    graphics.drawString(font, reward, textX, y + nh - font.lineHeight - 2, REWARD_COLOR, true);
                }
            }
        }

        // Status icon for completed
        if (isCompleted && zoom >= 0.5f) {
            // already showing checkmark in name
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Connection lines
    // ════════════════════════════════════════════════════════════════════

    private void renderConnection(GuiGraphics graphics, ProgressionNode from, ProgressionNode to, Set<String> completed) {
        int x1 = getScreenX(from.getGridX(), from.getGridY());
        int y1 = getScreenY(from.getGridY());
        int x2 = getScreenX(to.getGridX(), to.getGridY());
        int y2 = getScreenY(to.getGridY());

        boolean fromDone = completed.contains(from.getId());
        boolean toDone = completed.contains(to.getId());

        int color;
        if (toDone) {
            color = CONNECTION_COMPLETE;
        } else if (fromDone) {
            color = CONNECTION_AVAILABLE;
        } else {
            color = CONNECTION_LOCKED;
        }

        // Draw line segments (horizontal then vertical for L-shaped connections)
        int midX = (x1 + x2) / 2;

        int lineThickness = Math.max(1, (int) (2 * zoom));

        // Horizontal from x1 to midX
        drawHLine(graphics, x1, midX, y1, color, lineThickness);
        // Vertical from y1 to y2
        drawVLine(graphics, midX, y1, y2, color, lineThickness);
        // Horizontal from midX to x2
        drawHLine(graphics, midX, x2, y2, color, lineThickness);
    }

    private void drawHLine(GuiGraphics graphics, int x1, int x2, int y, int color, int thickness) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        graphics.fill(minX, y - thickness / 2, maxX, y - thickness / 2 + thickness, color);
    }

    private void drawVLine(GuiGraphics graphics, int x, int y1, int y2, int color, int thickness) {
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        graphics.fill(x - thickness / 2, minY, x - thickness / 2 + thickness, maxY, color);
    }

    // ════════════════════════════════════════════════════════════════════
    // Tooltip
    // ════════════════════════════════════════════════════════════════════

    private void renderNodeTooltip(GuiGraphics graphics, ProgressionNode node, int mx, int my, Set<String> completed) {
        boolean isCompleted = completed.contains(node.getId());
        boolean isAvailable = !isCompleted && ProgressionTreeRegistry.arePrerequisitesMet(node, completed);

        List<String> lines = new java.util.ArrayList<>();
        lines.add("§e§l" + node.getDisplayName());
        lines.add("§7" + node.getDescription());
        lines.add("");

        // Status
        if (isCompleted) {
            lines.add("§a✓ Completed");
        } else if (isAvailable) {
            lines.add("§b● Available");
        } else {
            lines.add("§8✕ Locked");
        }

        // Category
        lines.add("§7Category: §f" + node.getCategory().getDisplayName());

        // Rewards
        String reward = node.getRewardText();
        if (!reward.isEmpty()) {
            lines.add("§7Reward: §6" + reward);
        }

        // Prerequisites
        if (!node.getPrerequisiteIds().isEmpty()) {
            lines.add("");
            lines.add("§7Requires:");
            for (String prereqId : node.getPrerequisiteIds()) {
                ProgressionNode prereq = ProgressionTreeRegistry.getNode(prereqId);
                if (prereq != null) {
                    boolean met = completed.contains(prereqId);
                    lines.add((met ? "  §a✓ " : "  §c✕ ") + prereq.getDisplayName());
                }
            }
        }

        // Stage
        lines.add("");
        lines.add("§8Stage " + node.getStage().getNumber() + ": " + node.getStage().getDisplayName());

        // Calculate tooltip dimensions
        int tooltipW = 0;
        for (String line : lines) {
            tooltipW = Math.max(tooltipW, font.width(line));
        }
        tooltipW += 12;
        int tooltipH = lines.size() * (font.lineHeight + 1) + 8;

        // Position tooltip (avoid screen edges)
        int tx = mx + 12;
        int ty = my - 4;
        if (tx + tooltipW > width - 4) tx = mx - tooltipW - 8;
        if (ty + tooltipH > height - 4) ty = height - tooltipH - 4;
        if (ty < 4) ty = 4;

        // Render tooltip
        graphics.fill(tx - 2, ty - 2, tx + tooltipW + 2, ty + tooltipH + 2, 0xFF222244);
        graphics.fill(tx - 1, ty - 1, tx + tooltipW + 1, ty + tooltipH + 1, 0xFF444466);
        graphics.fill(tx, ty, tx + tooltipW, ty + tooltipH, 0xF0111122);

        int textY = ty + 4;
        for (String line : lines) {
            graphics.drawString(font, line, tx + 6, textY, 0xFFFFFFFF, true);
            textY += font.lineHeight + 1;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // Coordinate helpers
    // ════════════════════════════════════════════════════════════════════

    private int getScreenX(int gridX, int gridY) {
        float worldX = PADDING + gridX * NODE_SPACING_X + NODE_WIDTH / 2f;
        return (int) (width / 2f + (worldX - width / 2f + panX) * zoom);
    }

    private int getScreenY(int gridY) {
        float worldY = PADDING + STAGE_HEADER_HEIGHT + 10 + (gridY + 1) * NODE_SPACING_Y;
        return (int) (height / 2f + (worldY - height / 2f + panY) * zoom);
    }

    // ════════════════════════════════════════════════════════════════════
    // Input handling
    // ════════════════════════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 2) {
            dragging = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            dragStartPanX = panX;
            dragStartPanY = panY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 2) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            panX = dragStartPanX + (float) (mouseX - dragStartX) / zoom;
            panY = dragStartPanY + (float) (mouseY - dragStartY) / zoom;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        float zoomDelta = (float) scrollY * 0.1f;
        zoom = Math.max(0.3f, Math.min(2.0f, zoom + zoomDelta));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeyBindings.OPEN_PROGRESSION_TREE.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ════════════════════════════════════════════════════════════════════
    // Utility
    // ════════════════════════════════════════════════════════════════════

    private ItemStack getIconItem(String itemId) {
        try {
            ResourceLocation rl = ResourceLocation.parse(itemId);
            var opt = BuiltInRegistries.ITEM.get(rl);
            if (opt.isPresent()) {
                return new ItemStack(opt.get().value());
            }
        } catch (Exception e) {
            // Ignore
        }
        return ItemStack.EMPTY;
    }

    private static int brighten(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
