package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.network.SyncNearbyPlayersPayload.NearbyPlayerEntry;
import com.sevendaystominecraft.network.SyncQuestPayload.QuestEntry;
import com.sevendaystominecraft.network.SyncTerritoryPayload.TerritoryEntry;
import com.sevendaystominecraft.network.SyncTraderPayload.TraderEntry;
import com.sevendaystominecraft.network.SyncWaypointsPayload.WaypointData;

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

    static final int MAP_SIZE = 160;
    private static final int MAP_RADIUS = 80;
    static final int MARGIN = 8;
    private static final int CORNER_RADIUS = 10;
    private static final int BG_COLOR = 0xAA000000;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int PLAYER_DOT_COLOR = 0xFFFFFFFF;
    private static final int PLAYER_DOT_SIZE = 4;
    private static final int OTHER_PLAYER_DOT_SIZE = 3;
    private static final int SAMPLE_STEP = 2;
    private static final int PIXEL_SIZE = SAMPLE_STEP;
    private static final int COLOR_EASY   = 0xFF44FF44;
    private static final int COLOR_MEDIUM = 0xFFFFCC00;
    private static final int COLOR_HARD   = 0xFFFF4444;
    private static final int COLOR_CLEARED = 0xFF888888;
    private static final int TERRITORY_DOT_SIZE = 4;
    private static final int COLOR_TRADER = 0xFF00CCCC;
    private static final int TRADER_DOT_SIZE = 5;
    private static final int COLOR_QUEST = 0xFFFFFF00;
    private static final int QUEST_DOT_SIZE = 5;
    private static final int COLOR_GROUP_MEMBER = 0xFF55FFAA;
    private static final int COLOR_WAYPOINT = 0xFFFF6600;
    private static final int WAYPOINT_DOT_SIZE = 4;

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

        renderTraders(graphics, mc, player, centerX, centerY, mapX, mapY, cosYaw, sinYaw);
        renderQuestMarkers(graphics, mc, player, centerX, centerY, mapX, mapY, cosYaw, sinYaw);
        renderWaypoints(graphics, mc, player, centerX, centerY, mapX, mapY, cosYaw, sinYaw);

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
            int color = getTierColor(entry.tier());

            graphics.fill(dotX - halfDot, dotY - halfDot,
                    dotX + halfDot, dotY + halfDot, color);

            boolean iconFits = isInsideRoundedRect((dotX + 3) - mapX, (dotY + 3) - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) &&
                               isInsideRoundedRect((dotX - 2) - mapX, (dotY - 2) - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1);

            if (iconFits) {
                if (cleared) {
                    graphics.fill(dotX - 2, dotY + 1, dotX - 1, dotY + 2, color);
                    graphics.fill(dotX - 1, dotY + 2, dotX, dotY + 3, color);
                    graphics.fill(dotX, dotY + 1, dotX + 1, dotY + 2, color);
                    graphics.fill(dotX + 1, dotY, dotX + 2, dotY + 1, color);
                    graphics.fill(dotX + 2, dotY - 1, dotX + 3, dotY, color);
                } else {
                    graphics.fill(dotX - 2, dotY + 2, dotX + 3, dotY + 3, color);
                    graphics.fill(dotX - 1, dotY + 1, dotX + 2, dotY + 2, color);
                    graphics.fill(dotX, dotY - 1, dotX + 1, dotY + 1, color);
                    graphics.fill(dotX - 1, dotY, dotX + 2, dotY + 1, color);
                    graphics.fill(dotX, dotY - 2, dotX + 1, dotY - 1, color);
                }
            }
        }
    }

    private static void renderTraders(GuiGraphics graphics, Minecraft mc, Player player,
                                       int centerX, int centerY, int mapX, int mapY,
                                       double cosYaw, double sinYaw) {
        List<TraderEntry> traders = TraderClientState.getTraders();
        if (traders.isEmpty()) return;

        for (TraderEntry entry : traders) {
            double dx = entry.x() - player.getX();
            double dz = entry.z() - player.getZ();

            double screenDx = -(dx * cosYaw + dz * sinYaw);
            double screenDy = dx * sinYaw - dz * cosYaw;

            int dotX = centerX + (int) screenDx;
            int dotY = centerY + (int) screenDy;

            int halfDot = TRADER_DOT_SIZE / 2;

            int playerZoneRadius = PLAYER_DOT_SIZE + 8;
            if (Math.abs(dotX - centerX) < playerZoneRadius &&
                Math.abs(dotY - centerY) < playerZoneRadius) {
                continue;
            }

            if (!isInsideRoundedRect(dotX - halfDot - mapX, dotY - halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) ||
                !isInsideRoundedRect(dotX + halfDot - mapX, dotY + halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                continue;
            }

            graphics.fill(dotX, dotY - halfDot, dotX + 1, dotY + halfDot, COLOR_TRADER);
            graphics.fill(dotX - halfDot, dotY, dotX + halfDot, dotY + 1, COLOR_TRADER);
            graphics.fill(dotX - halfDot + 1, dotY - 1, dotX + halfDot - 1, dotY + 2, COLOR_TRADER);
            graphics.fill(dotX - 1, dotY - halfDot + 1, dotX + 2, dotY + halfDot - 1, COLOR_TRADER);

            int coinY = dotY + halfDot + 2;
            int coinBottom = coinY + 5;
            if (isInsideRoundedRect((dotX - 2) - mapX, coinY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) &&
                isInsideRoundedRect((dotX + 3) - mapX, coinBottom - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                graphics.fill(dotX - 1, coinY, dotX + 2, coinY + 1, COLOR_TRADER);
                graphics.fill(dotX - 2, coinY + 1, dotX + 3, coinY + 2, COLOR_TRADER);
                graphics.fill(dotX - 2, coinY + 2, dotX + 3, coinY + 3, COLOR_TRADER);
                graphics.fill(dotX, coinY + 1, dotX + 1, coinY + 3, 0xFF008888);
                graphics.fill(dotX - 1, coinY + 3, dotX + 2, coinY + 4, COLOR_TRADER);
                graphics.fill(dotX - 2, coinY + 3, dotX + 3, coinY + 4, COLOR_TRADER);
                graphics.fill(dotX - 1, coinY + 4, dotX + 2, coinY + 5, COLOR_TRADER);
            }
        }
    }

    private static void renderQuestMarkers(GuiGraphics graphics, Minecraft mc, Player player,
                                               int centerX, int centerY, int mapX, int mapY,
                                               double cosYaw, double sinYaw) {
        QuestEntry quest = QuestClientState.getTrackedQuest();
        if (quest == null || !quest.hasLocation() || "COMPLETED".equals(quest.stateName())) return;

        double dx = quest.locX() - player.getX();
        double dz = quest.locZ() - player.getZ();

        double screenDx = -(dx * cosYaw + dz * sinYaw);
        double screenDy = dx * sinYaw - dz * cosYaw;

        int dotX = centerX + (int) screenDx;
        int dotY = centerY + (int) screenDy;

        int halfDot = QUEST_DOT_SIZE / 2;

        if (!isInsideRoundedRect(dotX - halfDot - mapX, dotY - halfDot - mapY, MAP_SIZE, MAP_SIZE, 9) ||
            !isInsideRoundedRect(dotX + halfDot - mapX, dotY + halfDot - mapY, MAP_SIZE, MAP_SIZE, 9)) {
            return;
        }

        graphics.fill(dotX - halfDot, dotY - halfDot + 1, dotX + halfDot, dotY + halfDot - 1, COLOR_QUEST);
        graphics.fill(dotX - halfDot + 1, dotY - halfDot, dotX + halfDot - 1, dotY + halfDot, COLOR_QUEST);

        int exX = dotX;
        int exY = dotY + halfDot + 2;
        int exBottom = exY + 6;
        if (isInsideRoundedRect(exX - mapX, exY - mapY, MAP_SIZE, MAP_SIZE, 9) &&
            isInsideRoundedRect((exX + 1) - mapX, exBottom - mapY, MAP_SIZE, MAP_SIZE, 9)) {
            graphics.fill(exX, exY, exX + 1, exY + 4, COLOR_QUEST);
            graphics.fill(exX, exY + 5, exX + 1, exY + 6, COLOR_QUEST);
        }
    }

    private static void renderWaypoints(GuiGraphics graphics, Minecraft mc, Player player,
                                          int centerX, int centerY, int mapX, int mapY,
                                          double cosYaw, double sinYaw) {
        List<WaypointData> waypoints = WaypointClientState.getWaypoints();
        if (waypoints.isEmpty()) return;

        int halfMap = MAP_SIZE / 2;

        for (WaypointData wp : waypoints) {
            double dx = wp.x() - player.getX();
            double dz = wp.z() - player.getZ();

            double screenDx = -(dx * cosYaw + dz * sinYaw);
            double screenDy = dx * sinYaw - dz * cosYaw;

            int dotX = centerX + (int) screenDx;
            int dotY = centerY + (int) screenDy;

            int halfDot = WAYPOINT_DOT_SIZE / 2;

            boolean inside = isInsideRoundedRect(dotX - halfDot - mapX, dotY - halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) &&
                             isInsideRoundedRect(dotX + halfDot - mapX, dotY + halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1);

            if (inside) {
                graphics.fill(dotX, dotY - halfDot, dotX + 1, dotY + halfDot + 1, COLOR_WAYPOINT);
                graphics.fill(dotX - halfDot, dotY, dotX + halfDot + 1, dotY + 1, COLOR_WAYPOINT);
                for (int d = 1; d <= halfDot; d++) {
                    int w = halfDot - d;
                    if (w >= 0) {
                        graphics.fill(dotX - w, dotY - d, dotX + w + 1, dotY - d + 1, COLOR_WAYPOINT);
                        graphics.fill(dotX - w, dotY + d, dotX + w + 1, dotY + d + 1, COLOR_WAYPOINT);
                    }
                }
            } else {
                double dist = Math.sqrt(screenDx * screenDx + screenDy * screenDy);
                if (dist < 1) continue;

                double normX = screenDx / dist;
                double normY = screenDy / dist;

                int arrowDist = halfMap - 8;
                int arrowX = centerX + (int) (normX * arrowDist);
                int arrowY = centerY + (int) (normY * arrowDist);

                if (!isInsideRoundedRect(arrowX - mapX, arrowY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                    arrowDist -= 5;
                    arrowX = centerX + (int) (normX * arrowDist);
                    arrowY = centerY + (int) (normY * arrowDist);
                }

                int arrowSize = 3;
                graphics.fill(arrowX - arrowSize, arrowY - arrowSize,
                        arrowX + arrowSize + 1, arrowY + arrowSize + 1, COLOR_WAYPOINT);

                int tipX = arrowX + (int) (normX * 5);
                int tipY = arrowY + (int) (normY * 5);
                if (isInsideRoundedRect(tipX - mapX, tipY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                    graphics.fill(tipX, tipY, tipX + 1, tipY + 1, COLOR_WAYPOINT);
                    graphics.fill(tipX - 1, tipY, tipX + 2, tipY + 1, COLOR_WAYPOINT);
                }
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
            int dotColor = entry.groupMember() ? COLOR_GROUP_MEMBER : dotColors[colorIndex % dotColors.length];

            boolean inside = isInsideRoundedRect(dotScreenX - halfDot - mapX, dotScreenY - halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1) &&
                isInsideRoundedRect(dotScreenX + halfDot - mapX, dotScreenY + halfDot - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1);

            if (inside) {
                graphics.fill(dotScreenX - halfDot, dotScreenY - halfDot,
                        dotScreenX + halfDot, dotScreenY + halfDot, dotColor);

                if (entry.groupMember()) {
                    String name = entry.name();
                    int nameWidth = mc.font.width(name);
                    int nameX = dotScreenX - nameWidth / 2;
                    int nameY = dotScreenY - halfDot - 9;
                    if (isInsideRoundedRect(nameX - mapX, nameY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                        graphics.drawString(mc.font, name, nameX, nameY, dotColor, true);
                    }
                }
            } else if (entry.groupMember()) {
                int halfMapSize = MAP_SIZE / 2;
                double dist = Math.sqrt(screenDx * screenDx + screenDy * screenDy);
                if (dist >= 1) {
                    double normX = screenDx / dist;
                    double normY = screenDy / dist;
                    int arrowDist = halfMapSize - 10;
                    int arrowX = centerX + (int) (normX * arrowDist);
                    int arrowY = centerY + (int) (normY * arrowDist);

                    if (!isInsideRoundedRect(arrowX - mapX, arrowY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                        arrowDist -= 5;
                        arrowX = centerX + (int) (normX * arrowDist);
                        arrowY = centerY + (int) (normY * arrowDist);
                    }

                    int arrowSize = 3;
                    graphics.fill(arrowX - arrowSize, arrowY - arrowSize,
                            arrowX + arrowSize + 1, arrowY + arrowSize + 1, dotColor);

                    int tipX = arrowX + (int) (normX * 5);
                    int tipY = arrowY + (int) (normY * 5);
                    if (isInsideRoundedRect(tipX - mapX, tipY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                        graphics.fill(tipX - 1, tipY - 1, tipX + 2, tipY + 2, dotColor);
                    }

                    String name = entry.name();
                    int nameWidth = mc.font.width(name);
                    int nameX = arrowX - nameWidth / 2;
                    int nameY = arrowY - arrowSize - 9;
                    nameX = Math.max(mapX + 2, Math.min(mapX + MAP_SIZE - nameWidth - 2, nameX));
                    nameY = Math.max(mapY + 2, nameY);
                    if (isInsideRoundedRect(nameX - mapX, nameY - mapY, MAP_SIZE, MAP_SIZE, CORNER_RADIUS - 1)) {
                        graphics.drawString(mc.font, name, nameX, nameY, dotColor, true);
                    }
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

        if (terrainCache != null && movedX < 4 && movedZ < 4 && (now - cacheTime) < CACHE_DURATION_MS) {
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
