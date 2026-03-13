package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.network.SyncNearbyPlayersPayload.NearbyPlayerEntry;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

    private static int[] terrainCache = null;
    private static int cachedPlayerX = Integer.MIN_VALUE;
    private static int cachedPlayerZ = Integer.MIN_VALUE;
    private static long cacheTime = 0;
    private static final long CACHE_DURATION_MS = 1000;

    @SubscribeEvent
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

                double screenDx = worldOffX * cosYaw + worldOffZ * sinYaw;
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

        int halfDot = PLAYER_DOT_SIZE / 2;
        graphics.fill(centerX - halfDot, centerY - halfDot,
                centerX + halfDot, centerY + halfDot, PLAYER_DOT_COLOR);
        graphics.fill(centerX, centerY - halfDot - 1, centerX + 1, centerY - halfDot - 5, PLAYER_DOT_COLOR);

        String coordsText = String.format("(%d, %d)", playerBlockX, playerBlockZ);
        int coordsWidth = mc.font.width(coordsText);
        graphics.drawString(mc.font, coordsText, mapX + (MAP_SIZE - coordsWidth) / 2, mapY + MAP_SIZE + 3, 0xFFCCCCCC, true);
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

            double screenDx = dx * cosYaw + dz * sinYaw;
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
        int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
        if (topY <= -64) return 0xFF222222;

        BlockPos pos = new BlockPos(x, topY - 1, z);
        BlockState state = level.getBlockState(pos);
        MapColor mapColor = state.getMapColor(level, pos);

        if (mapColor == MapColor.NONE) return 0xFF222222;

        int col = mapColor.col;
        if (col == 0) return 0xFF222222;

        return 0xFF000000 | col;
    }
}
