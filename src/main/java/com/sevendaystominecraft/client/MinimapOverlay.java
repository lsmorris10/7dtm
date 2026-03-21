package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.network.SyncNearbyPlayersPayload.NearbyPlayerEntry;
import com.sevendaystominecraft.network.SyncTerritoryPayload.TerritoryEntry;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

public class MinimapOverlay {

    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "minimap_hud");

    static final int MAP_SIZE = 128;
    private static final int MAP_RADIUS = 64;
    static final int MARGIN = 8;
    private static final int CORNER_RADIUS = 10;
    private static final int BG_COLOR = 0xAA000000;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int PLAYER_DOT_COLOR = 0xFFFFFFFF;
    private static final int PLAYER_DOT_SIZE = 4;
    private static final int OTHER_PLAYER_DOT_SIZE = 3;
    private static final int SAMPLE_STEP = 4;
    private static final int PIXEL_SIZE = SAMPLE_STEP;
    private static final int COLOR_EASY   = 0xFF44FF44;
    private static final int COLOR_MEDIUM = 0xFFFFCC00;
    private static final int COLOR_HARD   = 0xFFFF4444;
    private static final int COLOR_CLEARED = 0xFF888888;
    private static final int TERRITORY_DOT_SIZE = 4;

    private static int[] terrainCache = null;
    private static int cachedPlayerX = Integer.MIN_VALUE;
    private static int cachedPlayerZ = Integer.MIN_VALUE;
    private static long cacheTime = 0;
    private static final long CACHE_DURATION_MS = 1000;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, MinimapOverlay::render);
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.hideGui) return;
        if (mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int mapX = screenWidth - MAP_SIZE - MARGIN;
        int mapY = MARGIN;

        drawRoundedRect(graphics, mapX - 2, mapY - 2, MAP_SIZE + 4, MAP_SIZE + 4, CORNER_RADIUS, BORDER_COLOR);
        drawRoundedRect(graphics, mapX, mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1, BG_COLOR);

        float yaw = player.getYRot() % 360f;
        if (yaw < 0) yaw += 360f;
        double radians = Math.toRadians(yaw);
        double cosYaw = Math.cos(radians);
        double sinYaw = Math.sin(radians);

        int playerBlockX = (int) Math.floor(player.getX());
        int playerBlockZ = (int) Math.floor(player.getZ());

        int[] terrain = getTerrainColors(mc.level, playerBlockX, playerBlockZ);

        int halfMap = MAP_SIZE / 2;
        int samplesPerSide = MAP_RADIUS / SAMPLE_STEP;

        for (int sx = -samplesPerSide; sx < samplesPerSide; sx++) {
            for (int sz = -samplesPerSide; sz < samplesPerSide; sz++) {
                int idx = (sx + samplesPerSide) * (samplesPerSide * 2) + (sz + samplesPerSide);
                int color = terrain[idx];

                double worldOffX = sx * SAMPLE_STEP;
                double worldOffZ = sz * SAMPLE_STEP;

                double screenDx = -(worldOffX * cosYaw + worldOffZ * sinYaw);
                double screenDy = worldOffX * sinYaw - worldOffZ * cosYaw;

                int screenPxX = mapX + halfMap + (int) screenDx;
                int screenPxY = mapY + halfMap + (int) screenDy;

                if (isInsideRoundedRect(screenPxX - mapX, screenPxY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) &&
                    isInsideRoundedRect(screenPxX - mapX + PIXEL_SIZE, screenPxY - mapY + PIXEL_SIZE, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                    graphics.fill(screenPxX, screenPxY, screenPxX + PIXEL_SIZE, screenPxY + PIXEL_SIZE, color);
                }
            }
        }

        int centerX = mapX + halfMap;
        int centerY = mapY + halfMap;

        renderOtherPlayers(graphics, mc, player, centerX, centerY, mapX, mapY, cosYaw, sinYaw);

        renderTerritories(graphics, mc, player, centerX, centerY, mapX, mapY, cosYaw, sinYaw);

        int halfDot = PLAYER_DOT_SIZE / 2;
        graphics.fill(centerX - halfDot, centerY - halfDot,
                centerX + halfDot, centerY + halfDot, PLAYER_DOT_COLOR);
        graphics.fill(centerX, centerY - halfDot - 1, centerX + 1, centerY - halfDot - 5, PLAYER_DOT_COLOR);

        String coordsText = String.format("(%d, %d)", playerBlockX, playerBlockZ);
        int coordsWidth = mc.font.width(coordsText);
        graphics.drawString(mc.font, coordsText, mapX + (MAP_SIZE - coordsWidth) / 2, mapY + MAP_SIZE + 3, 0xFFCCCCCC, true);
    }

    private static void renderTerritories(GuiGraphics graphics, Minecraft mc, Player player,
                                            int centerX, int centerY, int mapX, int mapY,
                                            double cosYaw, double sinYaw) {
        List<TerritoryEntry> territories = TerritoryClientState.getTerritories();
        if (territories.isEmpty()) return;

        for (TerritoryEntry entry : territories) {
            double dx = entry.x() - player.getX();
            double dz = entry.z() - player.getZ();

            double screenDx = -(dx * cosYaw + dz * sinYaw);
            double screenDy = dx * sinYaw - dz * cosYaw;

            int dotX = centerX + (int) screenDx;
            int dotY = centerY + (int) screenDy;

            int halfDot = TERRITORY_DOT_SIZE / 2;

            int playerZoneRadius = PLAYER_DOT_SIZE + 8;
            if (Math.abs(dotX - centerX) < playerZoneRadius &&
                Math.abs(dotY - centerY) < playerZoneRadius) {
                continue;
            }

            if (!isInsideRoundedRect(dotX - halfDot - mapX, dotY - halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) ||
                !isInsideRoundedRect(dotX + halfDot - mapX, dotY + halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                continue;
            }

            boolean cleared = entry.label().endsWith("[Cleared]");
            int color = cleared ? COLOR_CLEARED : getTierColor(entry.tier());

            graphics.fill(dotX - halfDot, dotY - halfDot,
                    dotX + halfDot, dotY + halfDot, color);

            String stars = "\u2605".repeat(entry.tier());
            String labelName = entry.label();
            if (cleared) {
                labelName = labelName.replace(" [Cleared]", "");
            }
            String[] parts = labelName.split(" ");
            String shortName = parts.length > 0 ? parts[0] : labelName;
            if (cleared) {
                shortName = shortName + "\u2713";
            }
            String abbrevLabel = stars + " " + shortName;

            int textWidth = mc.font.width(abbrevLabel);
            int textX = dotX - textWidth / 2;
            int textY = dotY + halfDot + 1;

            if (isInsideRoundedRect(textX - mapX, textY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) &&
                isInsideRoundedRect(textX + textWidth - mapX, textY + mc.font.lineHeight - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                graphics.drawString(mc.font, abbrevLabel, textX, textY, color, true);
            }
        }
    }

    private static int getTierColor(int tier) {
        if (tier <= 2) return COLOR_EASY;
        if (tier == 3) return COLOR_MEDIUM;
        return COLOR_HARD;
    }

    private static void renderOtherPlayers(GuiGraphics graphics, Minecraft mc, Player localPlayer,
                                            int centerX, int centerY, int mapX, int mapY,
                                            double cosYaw, double sinYaw) {
        List<NearbyPlayerEntry> nearbyPlayers = NearbyPlayersClientState.getNearbyPlayers();
        if (nearbyPlayers.isEmpty()) return;

        int colorIndex = 0;
        int[] dotColors = {0xFF44FF44, 0xFF4488FF, 0xFFFF44FF, 0xFFFFFF44, 0xFFFF8844, 0xFF44FFFF};

        for (NearbyPlayerEntry entry : nearbyPlayers) {
            double dx = entry.x() - localPlayer.getX();
            double dz = entry.z() - localPlayer.getZ();

            double screenDx = -(dx * cosYaw + dz * sinYaw);
            double screenDy = dx * sinYaw - dz * cosYaw;

            int dotScreenX = centerX + (int) screenDx;
            int dotScreenY = centerY + (int) screenDy;

            int halfDot = OTHER_PLAYER_DOT_SIZE / 2;
            int dotColor = dotColors[colorIndex % dotColors.length];

            if (isInsideRoundedRect(dotScreenX - halfDot - mapX, dotScreenY - halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) &&
                isInsideRoundedRect(dotScreenX + halfDot - mapX, dotScreenY + halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                graphics.fill(dotScreenX - halfDot, dotScreenY - halfDot,
                        dotScreenX + halfDot, dotScreenY + halfDot, dotColor);

                int nameWidth = mc.font.width(entry.name());
                int nameX = dotScreenX - nameWidth / 2;
                int nameY = dotScreenY - halfDot - 9;
                if (nameY >= mapY) {
                    graphics.drawString(mc.font, entry.name(), nameX, nameY, dotColor, true);
                }
            }

            colorIndex++;
        }
    }

    private static void drawRoundedRect(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        graphics.fill(x + radius, y, x + width - radius, y + height, color);
        graphics.fill(x, y + radius, x + radius, y + height - radius, color);
        graphics.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        fillCorner(graphics, x + radius, y + radius, radius, 0, color);
        fillCorner(graphics, x + width - radius, y + radius, radius, 1, color);
        fillCorner(graphics, x + radius, y + height - radius, radius, 2, color);
        fillCorner(graphics, x + width - radius, y + height - radius, radius, 3, color);
    }

    private static void fillCorner(GuiGraphics graphics, int cx, int cy, int radius, int quadrant, int color) {
        for (int dy = 0; dy < radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            int x1, x2, y;
            switch (quadrant) {
                case 0: x1 = cx - dx; x2 = cx; y = cy - dy - 1; break;
                case 1: x1 = cx; x2 = cx + dx; y = cy - dy - 1; break;
                case 2: x1 = cx - dx; x2 = cx; y = cy + dy; break;
                case 3: x1 = cx; x2 = cx + dx; y = cy + dy; break;
                default: return;
            }
            graphics.fill(x1, y, x2, y + 1, color);
        }
    }

    private static boolean isInsideRoundedRect(int px, int py, int width, int height, int radius) {
        if (px < 0 || py < 0 || px >= width || py >= height) return false;

        if (px < radius && py < radius) {
            int dx = radius - px;
            int dy = radius - py;
            return dx * dx + dy * dy <= radius * radius;
        }
        if (px >= width - radius && py < radius) {
            int dx = px - (width - radius);
            int dy = radius - py;
            return dx * dx + dy * dy <= radius * radius;
        }
        if (px < radius && py >= height - radius) {
            int dx = radius - px;
            int dy = py - (height - radius);
            return dx * dx + dy * dy <= radius * radius;
        }
        if (px >= width - radius && py >= height - radius) {
            int dx = px - (width - radius);
            int dy = py - (height - radius);
            return dx * dx + dy * dy <= radius * radius;
        }

        return true;
    }

    private static int[] getTerrainColors(Level level, int playerX, int playerZ) {
        long now = System.currentTimeMillis();
        int movedX = Math.abs(playerX - cachedPlayerX);
        int movedZ = Math.abs(playerZ - cachedPlayerZ);

        if (terrainCache != null && movedX < 8 && movedZ < 8 && (now - cacheTime) < CACHE_DURATION_MS) {
            return terrainCache;
        }

        int samplesPerSide = MAP_RADIUS / SAMPLE_STEP;
        int totalSamples = samplesPerSide * 2;
        int[] colors = new int[totalSamples * totalSamples];

        for (int sx = -samplesPerSide; sx < samplesPerSide; sx++) {
            for (int sz = -samplesPerSide; sz < samplesPerSide; sz++) {
                int worldX = playerX + sx * SAMPLE_STEP;
                int worldZ = playerZ + sz * SAMPLE_STEP;
                int idx = (sx + samplesPerSide) * totalSamples + (sz + samplesPerSide);
                colors[idx] = getTopBlockColor(level, worldX, worldZ);
            }
        }

        terrainCache = colors;
        cachedPlayerX = playerX;
        cachedPlayerZ = playerZ;
        cacheTime = now;
        return colors;
    }

    private static int getTopBlockColor(Level level, int x, int z) {
        return TerrainColorHelper.getTopBlockColor(level, x, z);
    }
}
