package com.sevendaystominecraft.territory;

import com.sevendaystominecraft.client.CompassOverlay;
import com.sevendaystominecraft.client.TerritoryClientState;
import com.sevendaystominecraft.network.SyncTerritoryPayload.TerritoryEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class TerritoryCompassRenderer {

    private static final int COLOR_EASY    = 0xFF44FF44;
    private static final int COLOR_MEDIUM  = 0xFFFFCC00;
    private static final int COLOR_HARD    = 0xFFFF4444;
    private static final int ICON_WIDTH    = 6;
    private static final int MARKER_HEIGHT = 10;

    public static void render(GuiGraphics graphics, int stripX, int stripY, int stripWidth,
                               float compassBearing, int centerX) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        List<TerritoryEntry> territories = TerritoryClientState.getTerritories();
        if (territories.isEmpty()) return;

        float pixelsPerDegree = (float) stripWidth / 180f;

        for (TerritoryEntry entry : territories) {
            double dx = entry.x() - player.getX();
            double dz = entry.z() - player.getZ();
            if (dx == 0 && dz == 0) continue;

            double worldYaw = Math.toDegrees(Math.atan2(dx, -dz));
            if (worldYaw < 0) worldYaw += 360.0;

            float diff = angleDifference((float) worldYaw, compassBearing);
            float pixelOffset = diff * pixelsPerDegree;
            int markerX = centerX + Math.round(pixelOffset);

            if (markerX < stripX + ICON_WIDTH || markerX > stripX + stripWidth - ICON_WIDTH) continue;

            int color = getTierColor(entry.tier());

            int markerY = stripY + MARKER_HEIGHT - 2;

            graphics.fill(markerX - 1, markerY - 3, markerX + 2, markerY + 1, color);
            graphics.fill(markerX, markerY + 1, markerX + 1, markerY + 3, color);

            String stars = "★".repeat(entry.tier());
            int textWidth = mc.font.width(stars);
            graphics.drawString(mc.font, stars, markerX - textWidth / 2, stripY - 11, color, true);
        }
    }

    private static int getTierColor(int tier) {
        if (tier <= 2) return COLOR_EASY;
        if (tier == 3) return COLOR_MEDIUM;
        return COLOR_HARD;
    }

    private static float angleDifference(float target, float current) {
        float diff = target - current;
        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;
        return diff;
    }
}
