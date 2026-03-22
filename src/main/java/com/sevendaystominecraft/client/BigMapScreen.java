package com.sevendaystominecraft.client;

import com.sevendaystominecraft.network.SyncNearbyPlayersPayload.NearbyPlayerEntry;
import com.sevendaystominecraft.network.SyncTerritoryPayload.TerritoryEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class BigMapScreen extends Screen {

    private static final int MAP_RADIUS = 256;
    private static final int SAMPLE_STEP = 4;
    private static final int BG_COLOR = 0xEE111111;
    private static final int BORDER_COLOR = 0xFF444444;
    private static final int PLAYER_DOT_COLOR = 0xFFFFFFFF;
    private static final int PLAYER_DOT_SIZE = 6;
    private static final int OTHER_PLAYER_DOT_SIZE = 5;
    private static final int TITLE_COLOR = 0xFFCCCCCC;
    private static final int COORD_COLOR = 0xFFAAAAAA;
    private static final int COLOR_EASY   = 0xFF44FF44;
    private static final int COLOR_MEDIUM = 0xFFFFCC00;
    private static final int COLOR_HARD   = 0xFFFF4444;
    private static final int COLOR_CLEARED = 0xFF888888;

    private int[] terrainCache = null;
    private int cachedPlayerX = Integer.MIN_VALUE;
    private int cachedPlayerZ = Integer.MIN_VALUE;
    private long cacheTime = 0;
    private static final long CACHE_DURATION_MS = 2000;

    public BigMapScreen() {
        super(Component.translatable("screen.sevendaystominecraft.big_map"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        int screenWidth = this.width;
        int screenHeight = this.height;

        graphics.fill(0, 0, screenWidth, screenHeight, BG_COLOR);

        int mapDisplaySize = Math.min(screenWidth, screenHeight) - 60;
        int mapX = (screenWidth - mapDisplaySize) / 2;
        int mapY = 30;
        int mapBottom = mapY + mapDisplaySize;

        graphics.fill(mapX - 2, mapY - 2, mapX + mapDisplaySize + 2, mapBottom + 2, BORDER_COLOR);
        graphics.fill(mapX, mapY, mapX + mapDisplaySize, mapBottom, 0xFF1A1A1A);

        int playerBlockX = (int) Math.floor(player.getX());
        int playerBlockZ = (int) Math.floor(player.getZ());

        int[] terrain = getTerrainColors(mc.level, playerBlockX, playerBlockZ);

        int samplesPerSide = MAP_RADIUS / SAMPLE_STEP;
        int totalSamples = samplesPerSide * 2;
        float scale = (float) mapDisplaySize / (MAP_RADIUS * 2);

        for (int sx = -samplesPerSide; sx < samplesPerSide; sx++) {
            for (int sz = -samplesPerSide; sz < samplesPerSide; sz++) {
                int idx = (sx + samplesPerSide) * totalSamples + (sz + samplesPerSide);
                int color = terrain[idx];

                int pixelX = mapX + (int) ((sx + samplesPerSide) * scale * SAMPLE_STEP);
                int pixelY = mapY + (int) ((sz + samplesPerSide) * scale * SAMPLE_STEP);
                int pixelEndX = mapX + (int) ((sx + samplesPerSide + 1) * scale * SAMPLE_STEP);
                int pixelEndY = mapY + (int) ((sz + samplesPerSide + 1) * scale * SAMPLE_STEP);

                pixelEndX = Math.min(pixelEndX, mapX + mapDisplaySize);
                pixelEndY = Math.min(pixelEndY, mapBottom);

                if (pixelX < mapX + mapDisplaySize && pixelY < mapBottom &&
                    pixelEndX > mapX && pixelEndY > mapY) {
                    graphics.fill(
                            Math.max(pixelX, mapX), Math.max(pixelY, mapY),
                            pixelEndX, pixelEndY, color);
                }
            }
        }

        int centerX = mapX + mapDisplaySize / 2;
        int centerY = mapY + mapDisplaySize / 2;

        renderOtherPlayers(graphics, mc, player, centerX, centerY, mapX, mapY, mapDisplaySize, scale);

        renderTerritories(graphics, mc, player, centerX, centerY, mapX, mapY, mapDisplaySize, scale);

        renderPlayerMarker(graphics, centerX, centerY, player.getYRot());

        String titleText = this.title.getString();
        int titleWidth = mc.font.width(titleText);
        graphics.drawString(mc.font, titleText, (screenWidth - titleWidth) / 2, 10, TITLE_COLOR, true);

        Component coordsComponent = Component.translatable("gui.sevendaystominecraft.map_position",
                playerBlockX, playerBlockZ, getCardinalDirection(player.getYRot()));
        String coordsText = coordsComponent.getString();
        int coordsWidth = mc.font.width(coordsText);
        graphics.drawString(mc.font, coordsText, (screenWidth - coordsWidth) / 2, mapBottom + 8, COORD_COLOR, true);

        String hintText = Component.translatable("gui.sevendaystominecraft.map_close_hint").getString();
        int hintWidth = mc.font.width(hintText);
        graphics.drawString(mc.font, hintText, (screenWidth - hintWidth) / 2, mapBottom + 22, 0xFF666666, true);
    }

    private void renderPlayerMarker(GuiGraphics graphics, int cx, int cy, float yaw) {
        int halfDot = PLAYER_DOT_SIZE / 2;
        graphics.fill(cx - halfDot, cy - halfDot, cx + halfDot, cy + halfDot, PLAYER_DOT_COLOR);

        double radians = Math.toRadians(yaw);
        int arrowLen = 8;
        int arrowEndX = cx - (int) (Math.sin(radians) * arrowLen);
        int arrowEndY = cy + (int) (Math.cos(radians) * arrowLen);

        drawLine(graphics, cx, cy, arrowEndX, arrowEndY, PLAYER_DOT_COLOR);
    }

    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }

    private void renderTerritories(GuiGraphics graphics, Minecraft mc, Player player,
                                    int centerX, int centerY, int mapX, int mapY,
                                    int mapDisplaySize, float scale) {
        List<TerritoryEntry> territories = TerritoryClientState.getTerritories();
        if (territories.isEmpty()) return;

        for (TerritoryEntry entry : territories) {
            double dx = entry.x() - player.getX();
            double dz = entry.z() - player.getZ();

            int screenX = centerX + (int) (dx * scale);
            int screenY = centerY + (int) (dz * scale);

            boolean cleared = entry.label().endsWith("[Cleared]");
            int color = cleared ? COLOR_CLEARED : getTierColor(entry.tier());

            String displayLabel = entry.label();
            if (cleared) {
                displayLabel = displayLabel.replace(" [Cleared]", "") + " \u2713";
            }

            int labelWidth = mc.font.width(displayLabel);
            int labelX = screenX - labelWidth / 2;
            int labelY = screenY - 5;
            int padding = 2;

            int boxLeft = labelX - padding;
            int boxTop = labelY - padding;
            int boxRight = labelX + labelWidth + padding;
            int boxBottom = labelY + mc.font.lineHeight + padding;

            if (boxLeft < mapX || boxRight > mapX + mapDisplaySize ||
                boxTop < mapY || boxBottom > mapY + mapDisplaySize) {
                continue;
            }

            int playerZoneRadius = PLAYER_DOT_SIZE + 12;
            if (Math.abs(screenX - centerX) < playerZoneRadius + labelWidth / 2 &&
                Math.abs(screenY - centerY) < playerZoneRadius + mc.font.lineHeight / 2) {
                continue;
            }

            graphics.fill(boxLeft, boxTop, boxRight, boxBottom, 0x88000000);

            graphics.drawString(mc.font, displayLabel, labelX, labelY, color, true);
        }
    }

    private static int getTierColor(int tier) {
        if (tier <= 2) return COLOR_EASY;
        if (tier == 3) return COLOR_MEDIUM;
        return COLOR_HARD;
    }

    private void renderOtherPlayers(GuiGraphics graphics, Minecraft mc, Player localPlayer,
                                     int centerX, int centerY, int mapX, int mapY,
                                     int mapDisplaySize, float scale) {
        List<NearbyPlayerEntry> nearbyPlayers = NearbyPlayersClientState.getNearbyPlayers();
        if (nearbyPlayers.isEmpty()) return;

        int[] dotColors = {0xFF44FF44, 0xFF4488FF, 0xFFFF44FF, 0xFFFFFF44, 0xFFFF8844, 0xFF44FFFF};
        int colorIndex = 0;

        for (NearbyPlayerEntry entry : nearbyPlayers) {
            double dx = entry.x() - localPlayer.getX();
            double dz = entry.z() - localPlayer.getZ();

            int dotScreenX = centerX + (int) (dx * scale);
            int dotScreenY = centerY + (int) (dz * scale);

            if (dotScreenX < mapX || dotScreenX >= mapX + mapDisplaySize ||
                dotScreenY < mapY || dotScreenY >= mapY + mapDisplaySize) {
                colorIndex++;
                continue;
            }

            int halfDot = OTHER_PLAYER_DOT_SIZE / 2;
            int dotColor = dotColors[colorIndex % dotColors.length];

            graphics.fill(dotScreenX - halfDot, dotScreenY - halfDot,
                    dotScreenX + halfDot, dotScreenY + halfDot, dotColor);

            String name = entry.name();
            int nameWidth = mc.font.width(name);
            graphics.drawString(mc.font, name, dotScreenX - nameWidth / 2,
                    dotScreenY - halfDot - 10, dotColor, true);

            colorIndex++;
        }
    }

    private int[] getTerrainColors(Level level, int playerX, int playerZ) {
        long now = System.currentTimeMillis();
        int movedX = Math.abs(playerX - cachedPlayerX);
        int movedZ = Math.abs(playerZ - cachedPlayerZ);

        if (terrainCache != null && movedX < 16 && movedZ < 16 && (now - cacheTime) < CACHE_DURATION_MS) {
            return terrainCache;
        }

        terrainCache = TerrainColorHelper.sampleTerrain(level, playerX, playerZ, MAP_RADIUS, SAMPLE_STEP);
        cachedPlayerX = playerX;
        cachedPlayerZ = playerZ;
        cacheTime = now;
        return terrainCache;
    }

    private String getCardinalDirection(float yaw) {
        float adjusted = ((yaw % 360f) + 360f) % 360f;
        if (adjusted >= 337.5 || adjusted < 22.5) return "South";
        if (adjusted < 67.5) return "Southwest";
        if (adjusted < 112.5) return "West";
        if (adjusted < 157.5) return "Northwest";
        if (adjusted < 202.5) return "North";
        if (adjusted < 247.5) return "Northeast";
        if (adjusted < 292.5) return "East";
        return "Southeast";
    }

}
