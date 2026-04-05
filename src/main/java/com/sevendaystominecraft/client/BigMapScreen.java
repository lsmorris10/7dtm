package com.sevendaystominecraft.client;

import com.sevendaystominecraft.network.AddWaypointPayload;
import com.sevendaystominecraft.network.RemoveWaypointPayload;
import com.sevendaystominecraft.network.SyncNearbyPlayersPayload.NearbyPlayerEntry;
import com.sevendaystominecraft.network.SyncQuestPayload.QuestEntry;
import com.sevendaystominecraft.network.SyncTerritoryPayload.TerritoryEntry;
import com.sevendaystominecraft.network.SyncTraderPayload.TraderEntry;
import com.sevendaystominecraft.network.SyncWaypointsPayload.WaypointData;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class BigMapScreen extends Screen {

    private static final int MIN_MAP_RADIUS = 64;
    private static final int MAX_MAP_RADIUS = 1024;
    private static final int DEFAULT_MAP_RADIUS = 256;
    private int mapRadius = DEFAULT_MAP_RADIUS;
    private int sampleStep = 2;
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
    private static final int COLOR_TRADER = 0xFF00CCCC;
    private static final int COLOR_QUEST = 0xFFFFFF00;
    private static final int COLOR_GROUP_MEMBER = 0xFF55FFAA;
    private static final int COLOR_WAYPOINT = 0xFFFF6600;
    private static final int BOUNDARY_PADDING = 12;
    private static final int BOUNDARY_ALPHA = 0x88;

    private int[] terrainCache = null;
    private int cachedPlayerX = Integer.MIN_VALUE;
    private int cachedPlayerZ = Integer.MIN_VALUE;
    private long cacheTime = 0;
    private static final long CACHE_DURATION_MS = 2000;

    private boolean namingWaypoint = false;
    private int pendingWaypointWorldX;
    private int pendingWaypointWorldZ;
    private EditBox waypointNameBox;

    private int lastMapX;
    private int lastMapY;
    private int lastMapDisplaySize;
    private float lastScale;

    public BigMapScreen() {
        super(Component.translatable("screen.sevendaystominecraft.big_map"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        waypointNameBox = new EditBox(Minecraft.getInstance().font, width / 2 - 60, height / 2 - 10, 120, 20, Component.literal("Waypoint Name"));
        waypointNameBox.setMaxLength(32);
        waypointNameBox.setVisible(false);
        waypointNameBox.setFocused(false);
        addWidget(waypointNameBox);
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

        lastMapX = mapX;
        lastMapY = mapY;
        lastMapDisplaySize = mapDisplaySize;

        graphics.fill(mapX - 2, mapY - 2, mapX + mapDisplaySize + 2, mapBottom + 2, BORDER_COLOR);
        graphics.fill(mapX, mapY, mapX + mapDisplaySize, mapBottom, 0xFF1A1A1A);

        int playerBlockX = (int) Math.floor(player.getX());
        int playerBlockZ = (int) Math.floor(player.getZ());

        int[] terrain = getTerrainColors(mc.level, playerBlockX, playerBlockZ);

        sampleStep = mapRadius <= 128 ? 1 : (mapRadius <= 256 ? 2 : 4);
        int samplesPerSide = mapRadius / sampleStep;
        int totalSamples = samplesPerSide * 2;
        float scale = (float) mapDisplaySize / (mapRadius * 2);
        lastScale = scale;

        for (int sx = -samplesPerSide; sx < samplesPerSide; sx++) {
            for (int sz = -samplesPerSide; sz < samplesPerSide; sz++) {
                int idx = (sx + samplesPerSide) * totalSamples + (sz + samplesPerSide);
                int color = terrain[idx];

                int pixelX = mapX + (int) ((sx + samplesPerSide) * scale * sampleStep);
                int pixelY = mapY + (int) ((sz + samplesPerSide) * scale * sampleStep);
                int pixelEndX = mapX + (int) ((sx + samplesPerSide + 1) * scale * sampleStep);
                int pixelEndY = mapY + (int) ((sz + samplesPerSide + 1) * scale * sampleStep);

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

        renderTerritoryBoundaries(graphics, mc, player, centerX, centerY, mapX, mapY, mapDisplaySize, scale);

        renderOtherPlayers(graphics, mc, player, centerX, centerY, mapX, mapY, mapDisplaySize, scale);

        renderTerritories(graphics, mc, player, centerX, centerY, mapX, mapY, mapDisplaySize, scale);
        renderTraders(graphics, mc, player, centerX, centerY, mapX, mapY, mapDisplaySize, scale);
        renderQuestMarkers(graphics, mc, player, centerX, centerY, mapX, mapY, mapDisplaySize, scale);
        renderWaypoints(graphics, mc, player, centerX, centerY, mapX, mapY, mapDisplaySize, scale);

        renderPlayerMarker(graphics, centerX, centerY, player.getYRot());

        String titleText = this.title.getString();
        int titleWidth = mc.font.width(titleText);
        graphics.drawString(mc.font, titleText, (screenWidth - titleWidth) / 2, 10, TITLE_COLOR, true);

        Component coordsComponent = Component.translatable("gui.sevendaystominecraft.map_position",
                playerBlockX, playerBlockZ, getCardinalDirection(player.getYRot()));
        String coordsText = coordsComponent.getString();
        String zoomText = "  [Zoom: " + (int)(100.0 * DEFAULT_MAP_RADIUS / mapRadius) + "%]";
        coordsText += zoomText;
        int coordsWidth = mc.font.width(coordsText);
        graphics.drawString(mc.font, coordsText, (screenWidth - coordsWidth) / 2, mapBottom + 8, COORD_COLOR, true);

        String hintText = Component.translatable("gui.sevendaystominecraft.map_close_hint").getString();
        int hintWidth = mc.font.width(hintText);
        graphics.drawString(mc.font, hintText, (screenWidth - hintWidth) / 2, mapBottom + 22, 0xFF666666, true);

        if (namingWaypoint && waypointNameBox != null) {
            int boxBgX = waypointNameBox.getX() - 5;
            int boxBgY = waypointNameBox.getY() - 15;
            graphics.fill(boxBgX, boxBgY, boxBgX + 130, boxBgY + 45, 0xDD000000);
            graphics.drawString(mc.font, "Waypoint name:", boxBgX + 2, boxBgY + 2, 0xFFCCCCCC, true);
            waypointNameBox.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (namingWaypoint && waypointNameBox != null) {
            if (waypointNameBox.isMouseOver(mouseX, mouseY)) {
                return waypointNameBox.mouseClicked(mouseX, mouseY, button);
            }
            cancelWaypointNaming();
            return true;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return super.mouseClicked(mouseX, mouseY, button);

        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (mx >= lastMapX && mx < lastMapX + lastMapDisplaySize &&
            my >= lastMapY && my < lastMapY + lastMapDisplaySize) {

            int centerX = lastMapX + lastMapDisplaySize / 2;
            int centerY = lastMapY + lastMapDisplaySize / 2;
            double worldOffX = (mx - centerX) / (double) lastScale;
            double worldOffZ = (my - centerY) / (double) lastScale;
            int worldX = (int) Math.floor(player.getX() + worldOffX);
            int worldZ = (int) Math.floor(player.getZ() + worldOffZ);

            if (button == 0) {
                pendingWaypointWorldX = worldX;
                pendingWaypointWorldZ = worldZ;
                namingWaypoint = true;
                waypointNameBox.setVisible(true);
                waypointNameBox.setFocused(true);
                waypointNameBox.setValue("");
                setFocused(waypointNameBox);
                return true;
            } else if (button == 1) {
                List<WaypointData> waypoints = WaypointClientState.getWaypoints();
                for (WaypointData wp : waypoints) {
                    double dx = wp.x() - player.getX();
                    double dz = wp.z() - player.getZ();
                    int wpScreenX = centerX + (int) (dx * lastScale);
                    int wpScreenY = centerY + (int) (dz * lastScale);
                    if (Math.abs(mx - wpScreenX) < 8 && Math.abs(my - wpScreenY) < 8) {
                        PacketDistributor.sendToServer(new RemoveWaypointPayload(wp.x(), wp.z()));
                        return true;
                    }
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (namingWaypoint && waypointNameBox != null) {
            if (keyCode == 256) {
                cancelWaypointNaming();
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                String name = waypointNameBox.getValue().trim();
                if (!name.isEmpty()) {
                    PacketDistributor.sendToServer(new AddWaypointPayload(name, pendingWaypointWorldX, pendingWaypointWorldZ));
                }
                cancelWaypointNaming();
                return true;
            }
            return waypointNameBox.keyPressed(keyCode, scanCode, modifiers);
        }
        if (ModKeyBindings.OPEN_MAP.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (namingWaypoint && waypointNameBox != null) {
            return waypointNameBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (namingWaypoint) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        if (scrollY > 0) {
            // Scroll up = zoom in
            mapRadius = Math.max(MIN_MAP_RADIUS, mapRadius - 32);
        } else if (scrollY < 0) {
            // Scroll down = zoom out
            mapRadius = Math.min(MAX_MAP_RADIUS, mapRadius + 32);
        }
        // Invalidate terrain cache on zoom
        terrainCache = null;
        return true;
    }

    private void cancelWaypointNaming() {
        namingWaypoint = false;
        if (waypointNameBox != null) {
            waypointNameBox.setVisible(false);
            waypointNameBox.setFocused(false);
        }
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

    private void renderTerritoryBoundaries(GuiGraphics graphics, Minecraft mc, Player player,
                                            int centerX, int centerY, int mapX, int mapY,
                                            int mapDisplaySize, float scale) {
        List<TerritoryEntry> territories = TerritoryClientState.getTerritories();
        if (territories.isEmpty()) return;

        int mapRight = mapX + mapDisplaySize;
        int mapBottom = mapY + mapDisplaySize;

        for (TerritoryEntry entry : territories) {
            if (entry.buildings().isEmpty()) continue;

            int minBX = Integer.MAX_VALUE, maxBX = Integer.MIN_VALUE;
            int minBZ = Integer.MAX_VALUE, maxBZ = Integer.MIN_VALUE;
            for (var b : entry.buildings()) {
                if (b.x() < minBX) minBX = b.x();
                if (b.x() > maxBX) maxBX = b.x();
                if (b.z() < minBZ) minBZ = b.z();
                if (b.z() > maxBZ) maxBZ = b.z();
            }
            minBX -= BOUNDARY_PADDING;
            minBZ -= BOUNDARY_PADDING;
            maxBX += BOUNDARY_PADDING;
            maxBZ += BOUNDARY_PADDING;

            int left = centerX + (int) ((minBX - player.getX()) * scale);
            int right = centerX + (int) ((maxBX - player.getX()) * scale);
            int top = centerY + (int) ((minBZ - player.getZ()) * scale);
            int bottom = centerY + (int) ((maxBZ - player.getZ()) * scale);

            if (right < mapX || left > mapRight || bottom < mapY || top > mapBottom) continue;

            int clLeft = Math.max(left, mapX);
            int clRight = Math.min(right, mapRight);
            int clTop = Math.max(top, mapY);
            int clBottom = Math.min(bottom, mapBottom);

            boolean cleared = entry.label().endsWith("[Cleared]");
            int tierColor = cleared ? COLOR_CLEARED : getTierColor(entry.tier());
            int color = (BOUNDARY_ALPHA << 24) | (tierColor & 0x00FFFFFF);

            if (top >= mapY && top < mapBottom) {
                graphics.fill(clLeft, top, clRight, top + 1, color);
            }
            if (bottom >= mapY && bottom < mapBottom) {
                graphics.fill(clLeft, bottom, clRight, bottom + 1, color);
            }
            if (left >= mapX && left < mapRight) {
                graphics.fill(left, clTop, left + 1, clBottom, color);
            }
            if (right >= mapX && right < mapRight) {
                graphics.fill(right, clTop, right + 1, clBottom, color);
            }
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


            graphics.fill(boxLeft, boxTop, boxRight, boxBottom, 0x88000000);

            graphics.drawString(mc.font, displayLabel, labelX, labelY, color, true);
        }
    }

    private void renderTraders(GuiGraphics graphics, Minecraft mc, Player player,
                                int centerX, int centerY, int mapX, int mapY,
                                int mapDisplaySize, float scale) {
        List<TraderEntry> traders = TraderClientState.getTraders();
        if (traders.isEmpty()) return;

        int iconSize = 5;

        for (TraderEntry entry : traders) {
            double dx = entry.x() - player.getX();
            double dz = entry.z() - player.getZ();

            int screenX = centerX + (int) (dx * scale);
            int screenY = centerY + (int) (dz * scale);

            if (screenX - iconSize < mapX || screenX + iconSize > mapX + mapDisplaySize ||
                screenY - iconSize < mapY || screenY + iconSize > mapY + mapDisplaySize) {
                continue;
            }

            int playerZoneRadius = PLAYER_DOT_SIZE + 12;
            if (Math.abs(screenX - centerX) < playerZoneRadius &&
                Math.abs(screenY - centerY) < playerZoneRadius) {
                continue;
            }

            graphics.fill(screenX, screenY - iconSize, screenX + 1, screenY + iconSize, COLOR_TRADER);
            graphics.fill(screenX - iconSize, screenY, screenX + iconSize, screenY + 1, COLOR_TRADER);
            for (int d = 1; d < iconSize; d++) {
                int w = iconSize - d;
                graphics.fill(screenX - w, screenY - d, screenX + w + 1, screenY - d + 1, COLOR_TRADER);
                graphics.fill(screenX - w, screenY + d, screenX + w + 1, screenY + d + 1, COLOR_TRADER);
            }

            String nameLabel = entry.name();
            int labelWidth = mc.font.width(nameLabel);
            int labelX = screenX - labelWidth / 2;
            int labelY = screenY + iconSize + 2;

            if (labelX >= mapX && labelX + labelWidth <= mapX + mapDisplaySize &&
                labelY + mc.font.lineHeight <= mapY + mapDisplaySize) {
                graphics.fill(labelX - 1, labelY - 1, labelX + labelWidth + 1, labelY + mc.font.lineHeight + 1, 0x88000000);
                graphics.drawString(mc.font, nameLabel, labelX, labelY, COLOR_TRADER, true);
            }
        }
    }

    private void renderQuestMarkers(GuiGraphics graphics, Minecraft mc, Player player,
                                      int centerX, int centerY, int mapX, int mapY,
                                      int mapDisplaySize, float scale) {
        QuestEntry quest = QuestClientState.getTrackedQuest();
        if (quest == null || !quest.hasLocation() || "COMPLETED".equals(quest.stateName())) return;

        int iconSize = 6;

        double dx = quest.locX() - player.getX();
        double dz = quest.locZ() - player.getZ();

        int screenX = centerX + (int) (dx * scale);
        int screenY = centerY + (int) (dz * scale);

        if (screenX - iconSize < mapX || screenX + iconSize > mapX + mapDisplaySize ||
            screenY - iconSize < mapY || screenY + iconSize > mapY + mapDisplaySize) {
            return;
        }

        graphics.fill(screenX - iconSize / 2, screenY - iconSize / 2,
                screenX + iconSize / 2, screenY + iconSize / 2, COLOR_QUEST);

        String label = "Q " + quest.questName();
        if (mc.font.width(label) > 80) {
            label = mc.font.plainSubstrByWidth(label, 77) + "...";
        }
        int labelWidth = mc.font.width(label);
        int labelX = screenX - labelWidth / 2;
        int labelY = screenY + iconSize / 2 + 2;

        if (labelX >= mapX && labelX + labelWidth <= mapX + mapDisplaySize &&
            labelY + mc.font.lineHeight <= mapY + mapDisplaySize) {
            graphics.fill(labelX - 1, labelY - 1, labelX + labelWidth + 1, labelY + mc.font.lineHeight + 1, 0x88000000);
            graphics.drawString(mc.font, label, labelX, labelY, COLOR_QUEST, true);
        }
    }

    private void renderWaypoints(GuiGraphics graphics, Minecraft mc, Player player,
                                  int centerX, int centerY, int mapX, int mapY,
                                  int mapDisplaySize, float scale) {
        List<WaypointData> waypoints = WaypointClientState.getWaypoints();
        if (waypoints.isEmpty()) return;

        int iconSize = 5;

        for (WaypointData wp : waypoints) {
            double dx = wp.x() - player.getX();
            double dz = wp.z() - player.getZ();

            int screenX = centerX + (int) (dx * scale);
            int screenY = centerY + (int) (dz * scale);

            if (screenX - iconSize < mapX || screenX + iconSize > mapX + mapDisplaySize ||
                screenY - iconSize < mapY || screenY + iconSize > mapY + mapDisplaySize) {
                continue;
            }

            graphics.fill(screenX, screenY - iconSize, screenX + 1, screenY + iconSize + 1, COLOR_WAYPOINT);
            graphics.fill(screenX - iconSize, screenY, screenX + iconSize + 1, screenY + 1, COLOR_WAYPOINT);
            for (int d = 1; d <= iconSize; d++) {
                int w = iconSize - d;
                graphics.fill(screenX - w, screenY - d, screenX + w + 1, screenY - d + 1, COLOR_WAYPOINT);
                graphics.fill(screenX - w, screenY + d, screenX + w + 1, screenY + d + 1, COLOR_WAYPOINT);
            }

            String label = wp.name();
            int labelWidth = mc.font.width(label);
            int labelX = screenX - labelWidth / 2;
            int labelY = screenY + iconSize + 3;

            if (labelX >= mapX && labelX + labelWidth <= mapX + mapDisplaySize &&
                labelY + mc.font.lineHeight <= mapY + mapDisplaySize) {
                graphics.fill(labelX - 1, labelY - 1, labelX + labelWidth + 1, labelY + mc.font.lineHeight + 1, 0x88000000);
                graphics.drawString(mc.font, label, labelX, labelY, COLOR_WAYPOINT, true);
            }
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

            int halfDot = OTHER_PLAYER_DOT_SIZE / 2;
            int dotColor = entry.groupMember() ? COLOR_GROUP_MEMBER : dotColors[colorIndex % dotColors.length];

            boolean inside = dotScreenX >= mapX && dotScreenX < mapX + mapDisplaySize &&
                             dotScreenY >= mapY && dotScreenY < mapY + mapDisplaySize;

            if (inside) {
                graphics.fill(dotScreenX - halfDot, dotScreenY - halfDot,
                        dotScreenX + halfDot, dotScreenY + halfDot, dotColor);

                String name = entry.name();
                int nameWidth = mc.font.width(name);
                graphics.drawString(mc.font, name, dotScreenX - nameWidth / 2,
                        dotScreenY - halfDot - 10, dotColor, true);
            } else if (entry.groupMember()) {
                int halfMapDisplay = mapDisplaySize / 2;
                double relX = dx * scale;
                double relZ = dz * scale;
                double dist = Math.sqrt(relX * relX + relZ * relZ);
                if (dist >= 1) {
                    double normX = relX / dist;
                    double normZ = relZ / dist;
                    int edgeDist = halfMapDisplay - 12;
                    int edgeX = centerX + (int) (normX * edgeDist);
                    int edgeY = centerY + (int) (normZ * edgeDist);

                    edgeX = Math.max(mapX + 6, Math.min(mapX + mapDisplaySize - 6, edgeX));
                    edgeY = Math.max(mapY + 6, Math.min(mapY + mapDisplaySize - 6, edgeY));

                    int arrowSize = 4;
                    graphics.fill(edgeX - arrowSize, edgeY - arrowSize,
                            edgeX + arrowSize + 1, edgeY + arrowSize + 1, dotColor);

                    String name = entry.name();
                    int nameWidth = mc.font.width(name);
                    int nameX = edgeX - nameWidth / 2;
                    int nameY = edgeY - arrowSize - 10;
                    nameX = Math.max(mapX + 2, Math.min(mapX + mapDisplaySize - nameWidth - 2, nameX));
                    nameY = Math.max(mapY + 2, nameY);
                    graphics.fill(nameX - 1, nameY - 1, nameX + nameWidth + 1, nameY + mc.font.lineHeight + 1, 0x88000000);
                    graphics.drawString(mc.font, name, nameX, nameY, dotColor, true);
                }
            }

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

        terrainCache = TerrainColorHelper.sampleTerrain(level, playerX, playerZ, mapRadius, sampleStep);
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
