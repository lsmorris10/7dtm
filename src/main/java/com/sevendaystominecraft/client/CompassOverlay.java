package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.territory.TerritoryCompassRenderer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class CompassOverlay {

    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "compass_hud");

    private static final int STRIP_HEIGHT = 20;
    private static final int BG_COLOR = 0xAA000000;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int TICK_COLOR = 0xFF888888;
    private static final int CARDINAL_COLOR = 0xFFFF4444;
    private static final int INTERCARDINAL_COLOR = 0xFFFFCC00;
    private static final int DEGREE_COLOR = 0xFFAAAAAA;
    private static final int CENTER_LINE_COLOR = 0xCCFFFFFF;

    private static final int HEAT_WARN_COLOR = 0xFFFF6600;

    private static final String[] DIRECTIONS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    static final int COMPASS_BOTTOM_Y = 4 + STRIP_HEIGHT + 2;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, CompassOverlay::render);
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.hideGui) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int minimapRight = screenWidth - MinimapOverlay.MARGIN;
        int minimapLeft = minimapRight - MinimapOverlay.MAP_SIZE - 4;

        int maxStripWidth = Math.min(360, minimapLeft - 20);
        if (maxStripWidth < 100) maxStripWidth = 100;
        int stripWidth = maxStripWidth;

        int halfStrip = stripWidth / 2;
        int stripX = (screenWidth - stripWidth) / 2;
        int stripY = 4;

        float mcYaw = player.getYRot() % 360f;
        if (mcYaw < 0) mcYaw += 360f;
        float compassBearing = (mcYaw + 180f) % 360f;

        graphics.fill(stripX - 1, stripY - 1, stripX + stripWidth + 1, stripY + STRIP_HEIGHT + 1, BORDER_COLOR);
        graphics.fill(stripX, stripY, stripX + stripWidth, stripY + STRIP_HEIGHT, BG_COLOR);

        int centerX = stripX + halfStrip;
        graphics.fill(centerX, stripY, centerX + 1, stripY + STRIP_HEIGHT, CENTER_LINE_COLOR);

        float pixelsPerDegree = (float) stripWidth / 180f;

        for (int deg = 0; deg < 360; deg += 5) {
            float offset = angleDifference(deg, compassBearing) * pixelsPerDegree;
            int tickX = centerX + Math.round(offset);

            if (tickX < stripX || tickX > stripX + stripWidth) continue;

            if (deg % 45 == 0) {
                int dirIndex = deg / 45;
                String label = DIRECTIONS[dirIndex];
                boolean isCardinal = (deg % 90 == 0);
                int color = isCardinal ? CARDINAL_COLOR : INTERCARDINAL_COLOR;
                int textWidth = mc.font.width(label);
                graphics.drawString(mc.font, label, tickX - textWidth / 2, stripY + 2, color, true);
                graphics.fill(tickX, stripY + 12, tickX + 1, stripY + STRIP_HEIGHT - 1, color);
            } else if (deg % 15 == 0) {
                String degStr = String.valueOf(deg);
                int textWidth = mc.font.width(degStr);
                graphics.drawString(mc.font, degStr, tickX - textWidth / 2, stripY + 3, DEGREE_COLOR, false);
                graphics.fill(tickX, stripY + 14, tickX + 1, stripY + STRIP_HEIGHT - 2, TICK_COLOR);
            } else {
                graphics.fill(tickX, stripY + 16, tickX + 1, stripY + STRIP_HEIGHT - 3, TICK_COLOR);
            }
        }

        renderHeatIndicator(graphics, mc, stripX, stripY, stripWidth);
        TerritoryCompassRenderer.render(graphics, stripX, stripY, stripWidth, compassBearing, centerX);
    }

    private static void renderHeatIndicator(GuiGraphics graphics, Minecraft mc, int stripX, int stripY, int stripWidth) {
        float heat = ChunkHeatClientState.getCurrentChunkHeat();
        if (heat <= 25f) return;

        long time = System.currentTimeMillis();
        float pulse = (float) (0.5 + 0.5 * Math.sin(time / 200.0));
        int alpha = (int) (150 + 105 * pulse);
        int color = (alpha << 24) | (0xFF6600);

        int iconX = stripX - 22;
        int iconY = stripY + 2;

        graphics.fill(iconX - 2, iconY - 2, iconX + 18, iconY + 18, 0x88000000);

        graphics.drawString(mc.font, "*", iconX + 1, iconY - 1, color, true);
        graphics.drawString(mc.font, "^", iconX + 5, iconY + 1, color, true);
        graphics.drawString(mc.font, "|", iconX + 5, iconY + 5, (alpha << 24) | 0xCC4400, true);

        String heatLabel = String.format("%.0f", heat);
        int labelWidth = mc.font.width(heatLabel);
        graphics.drawString(mc.font, heatLabel, iconX + (18 - labelWidth) / 2, iconY + 10, HEAT_WARN_COLOR, true);
    }

    private static float angleDifference(float target, float current) {
        float diff = target - current;
        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;
        return diff;
    }
}
