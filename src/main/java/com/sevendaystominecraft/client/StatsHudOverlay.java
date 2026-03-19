package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.perk.LevelManager;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
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

    private static final int HEALTH_COLOR = 0xFFCC3333;
    private static final int HEALTH_LOW_COLOR = 0xFF881111;
    private static final int STAMINA_COLOR = 0xFF33CC33;
    private static final int STAMINA_LOW_COLOR = 0xFFCC3333;
    private static final int FOOD_COLOR = 0xFFCC8833;
    private static final int FOOD_LOW_COLOR = 0xFFCC3333;
    private static final int WATER_COLOR = 0xFF3388CC;
    private static final int WATER_LOW_COLOR = 0xFF224488;
    private static final int XP_COLOR = 0xFF9933FF;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int DEBUFF_COLOR = 0xFFFF5555;
    private static final int TEMP_COLD_COLOR = 0xFF88CCFF;
    private static final int TEMP_HOT_COLOR = 0xFFFF6633;
    private static final int TEMP_NORMAL_COLOR = 0xFFAAFFAA;
    private static final int LEVEL_COLOR = 0xFFFFDD00;

    private static final float LOW_THRESHOLD = 0.3f;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, StatsHudOverlay::render);
        SevenDaysToMinecraft.LOGGER.info("BZHS: Registered stats HUD overlay (vanilla hearts/armor/food hidden)");
    }

    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        ResourceLocation name = event.getName();
        if (name.equals(VanillaGuiLayers.PLAYER_HEALTH)
                || name.equals(VanillaGuiLayers.ARMOR_LEVEL)
                || name.equals(VanillaGuiLayers.FOOD_LEVEL)) {
            event.setCanceled(true);
        }
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.hideGui) return;

        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) return;
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        renderTopLeftStats(graphics, mc, player, stats);
        renderBottomCenterXp(graphics, mc, stats);
    }

    private static void renderTopLeftStats(GuiGraphics graphics, Minecraft mc, Player player, SevenDaysPlayerStats stats) {
        int x = MARGIN_X;
        int y = MARGIN_Y;

        int currentDay = (int) (mc.level.getDayTime() / SevenDaysConstants.DAY_LENGTH) + 1;
        String dayAndLevel = String.format("Day: %d  |  Lvl: %d", currentDay, stats.getLevel());
        graphics.drawString(mc.font, dayAndLevel, x, y, LEVEL_COLOR, true);
        y += 14;

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPct = (maxHealth > 0) ? health / maxHealth : 0f;
        drawStatBar(graphics, x, y, "Health", healthPct, health, maxHealth,
                healthPct < LOW_THRESHOLD ? HEALTH_LOW_COLOR : HEALTH_COLOR);
        y += BAR_HEIGHT + BAR_SPACING + 2;

        float staminaPct = (stats.getMaxStamina() > 0) ? stats.getStamina() / stats.getMaxStamina() : 0f;
        drawStatBar(graphics, x, y, "Stamina", staminaPct, stats.getStamina(), stats.getMaxStamina(),
                staminaPct < LOW_THRESHOLD ? STAMINA_LOW_COLOR : STAMINA_COLOR);
        y += BAR_HEIGHT + BAR_SPACING + 2;

        float food = stats.getFood();
        float maxFood = stats.getMaxFood();
        float foodPct = (maxFood > 0) ? food / maxFood : 0f;
        drawStatBar(graphics, x, y, "Food", foodPct, food, maxFood,
                foodPct < LOW_THRESHOLD ? FOOD_LOW_COLOR : FOOD_COLOR);
        y += BAR_HEIGHT + BAR_SPACING + 2;

        float water = stats.getWater();
        float maxWater = stats.getMaxWater();
        float waterPct = (maxWater > 0) ? water / maxWater : 0f;
        drawStatBar(graphics, x, y, "Water", waterPct, water, maxWater,
                waterPct < LOW_THRESHOLD ? WATER_LOW_COLOR : WATER_COLOR);
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

    private static void renderBottomCenterXp(GuiGraphics graphics, Minecraft mc, SevenDaysPlayerStats stats) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int xpBarWidth = 182;
        int xpBarX = (screenWidth - xpBarWidth) / 2;
        int skillXpBarHeight = 3;

        int hotbarTop = screenHeight - 22 - 1;
        int skillXpY = hotbarTop - skillXpBarHeight - 4;

        int xpNeeded = LevelManager.xpToNextLevel(stats.getLevel());
        float xpPct = (xpNeeded > 0) ? (float) stats.getXp() / xpNeeded : 0f;

        graphics.fill(xpBarX, skillXpY, xpBarX + xpBarWidth, skillXpY + skillXpBarHeight, 0xFF111111);
        int filledWidth = Math.round(xpBarWidth * Math.max(0f, Math.min(1f, xpPct)));
        if (filledWidth > 0) {
            graphics.fill(xpBarX, skillXpY, xpBarX + filledWidth, skillXpY + skillXpBarHeight, XP_COLOR);
        }

        String skillLabel = String.format("Skill XP: %d/%d", stats.getXp(), xpNeeded);
        int skillLabelWidth = mc.font.width(skillLabel);
        graphics.drawString(mc.font, skillLabel, (screenWidth - skillLabelWidth) / 2, skillXpY - 12, XP_COLOR, true);
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
