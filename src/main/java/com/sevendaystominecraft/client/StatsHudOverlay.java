package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.perk.LevelManager;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class StatsHudOverlay {

    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "stats_hud");

    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 8;
    private static final int BAR_SPACING = 3;
    private static final int MARGIN_X = 10;
    private static final int MARGIN_Y = 35;
    private static final int LABEL_WIDTH = 55;

    private static final int STAMINA_COLOR = 0xFF33CC33;
    private static final int STAMINA_LOW_COLOR = 0xFFCC3333;
    private static final int XP_COLOR = 0xFF9933FF;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int DEBUFF_COLOR = 0xFFFF5555;
    private static final int TEMP_COLD_COLOR = 0xFF88CCFF;
    private static final int TEMP_HOT_COLOR = 0xFFFF6633;
    private static final int TEMP_NORMAL_COLOR = 0xFFAAFFAA;
    private static final int LEVEL_COLOR = 0xFFFFDD00;

    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = 1;
    private static final int ICONS_PER_ROW = 10;
    private static final int ICON_STEP = ICON_SIZE + ICON_SPACING;

    private static final float LOW_THRESHOLD = 0.3f;

    private static final ResourceLocation WATER_FULL = guiTexture("water_full");
    private static final ResourceLocation WATER_HALF = guiTexture("water_half");
    private static final ResourceLocation WATER_EMPTY = guiTexture("water_empty");
    private static final ResourceLocation WATER_LOW = guiTexture("water_low");
    private static final ResourceLocation WATER_HALF_LOW = guiTexture("water_half_low");

    private static ResourceLocation guiTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/" + name + ".png");
    }

    private static void blitIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(RenderType::guiTextured, texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, StatsHudOverlay::render);
        SevenDaysToMinecraft.LOGGER.info("BZHS: Registered stats HUD overlay (vanilla hearts/armor/food hidden)");
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.hideGui) return;

        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) return;
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        renderTopLeftStats(graphics, mc, stats);
        renderIconHud(graphics, mc, player, stats);
    }

    private static void renderTopLeftStats(GuiGraphics graphics, Minecraft mc, SevenDaysPlayerStats stats) {
        int x = MARGIN_X;
        int y = MARGIN_Y;

        int currentDay = (int) (mc.level.getDayTime() / SevenDaysConstants.DAY_LENGTH) + 1;
        String dayAndLevel = String.format("Day: %d  |  Lvl: %d", currentDay, stats.getLevel());
        graphics.drawString(mc.font, dayAndLevel, x, y, LEVEL_COLOR, true);
        y += 14;

        float staminaPct = (stats.getMaxStamina() > 0) ? stats.getStamina() / stats.getMaxStamina() : 0f;
        drawStatBar(graphics, x, y, "Stamina", staminaPct, stats.getStamina(), stats.getMaxStamina(),
                staminaPct < LOW_THRESHOLD ? STAMINA_LOW_COLOR : STAMINA_COLOR);
        y += BAR_HEIGHT + BAR_SPACING + 2;

        float temp = stats.getCoreTemperature();
        int tempColor = (temp < 50f) ? TEMP_COLD_COLOR : (temp > 90f) ? TEMP_HOT_COLOR : TEMP_NORMAL_COLOR;
        String tempText = String.format("Temp: %.0f°F", temp);
        graphics.drawString(mc.font, tempText, x, y, tempColor, true);
        y += 12;

        var debuffs = stats.getDebuffs();
        if (!debuffs.isEmpty()) {
            StringBuilder debuffText = new StringBuilder("Debuffs: ");
            for (var entry : debuffs.entrySet()) {
                debuffText.append(entry.getKey())
                          .append(" (").append(entry.getValue() / 20).append("s) ");
            }
            graphics.drawString(mc.font, debuffText.toString(), x, y, DEBUFF_COLOR, true);
        }
    }

    private static void renderIconHud(GuiGraphics graphics, Minecraft mc, Player player, SevenDaysPlayerStats stats) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int hotbarTop = screenHeight - 22 - 1;

        int vanillaXpBarY = screenHeight - 29;
        int skillXpBarHeight = 3;
        int skillXpY = vanillaXpBarY - skillXpBarHeight - 1;
        int xpBarWidth = 182;
        int xpBarX = (screenWidth - xpBarWidth) / 2;

        int xpNeeded = LevelManager.xpToNextLevel(stats.getLevel());
        float xpPct = (xpNeeded > 0) ? (float) stats.getXp() / xpNeeded : 0f;

        graphics.fill(xpBarX, skillXpY, xpBarX + xpBarWidth, skillXpY + skillXpBarHeight, 0xFF111111);
        int filledWidth = Math.round(xpBarWidth * Math.max(0f, Math.min(1f, xpPct)));
        if (filledWidth > 0) {
            graphics.fill(xpBarX, skillXpY, xpBarX + filledWidth, skillXpY + skillXpBarHeight, XP_COLOR);
        }

        String skillLabel = String.format("Skill XP: %d/%d", stats.getXp(), xpNeeded);
        int skillLabelWidth = mc.font.width(skillLabel);
        graphics.drawString(mc.font, skillLabel, (screenWidth - skillLabelWidth) / 2, skillXpY - 10, XP_COLOR, true);

        int rightBaseX = screenWidth / 2 + 91;

        float water = stats.getWater();
        float maxWater = stats.getMaxWater();
        float waterPct = (maxWater > 0) ? water / maxWater : 0f;
        boolean waterLow = waterPct < LOW_THRESHOLD;
        int waterY = hotbarTop - 2 - 3 * (ICON_SIZE + 1);
        for (int i = 0; i < ICONS_PER_ROW; i++) {
            int iconIndex = ICONS_PER_ROW - 1 - i;
            int iconX = rightBaseX - (i + 1) * ICON_STEP;
            float iconMinPct = iconIndex * 0.1f;

            ResourceLocation icon;
            if (waterPct >= iconMinPct + 0.1f) {
                icon = waterLow ? WATER_LOW : WATER_FULL;
            } else if (waterPct >= iconMinPct + 0.05f) {
                icon = waterLow ? WATER_HALF_LOW : WATER_HALF;
            } else {
                icon = WATER_EMPTY;
            }
            blitIcon(graphics, icon, iconX, waterY);
        }
    }

    private static void drawStatBar(GuiGraphics graphics, int x, int y,
                                     String label, float pct, float current, float max, int barColor) {
        Minecraft mc = Minecraft.getInstance();

        graphics.drawString(mc.font, label + ":", x, y, TEXT_COLOR, true);

        int barX = x + LABEL_WIDTH;

        graphics.fill(barX - 1, y - 1, barX + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, BORDER_COLOR);
        graphics.fill(barX, y, barX + BAR_WIDTH, y + BAR_HEIGHT, 0xFF111111);

        int filledWidth = Math.round(BAR_WIDTH * Math.max(0f, Math.min(1f, pct)));
        if (filledWidth > 0) {
            graphics.fill(barX, y, barX + filledWidth, y + BAR_HEIGHT, barColor);
        }

        String pctText = String.format("%.0f/%.0f", current, max);
        graphics.drawString(mc.font, pctText, barX + BAR_WIDTH + 4, y, TEXT_COLOR, true);
    }


}
