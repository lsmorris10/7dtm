package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StatsHudOverlay {

    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "stats_hud");

    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 8;
    private static final int BAR_SPACING = 3;
    private static final int MARGIN_X = 10;
    private static final int MARGIN_Y = 35;
    private static final int TEXT_OFFSET_X = 4;
    private static final int LABEL_WIDTH = 55;

    private static final int FOOD_COLOR = 0xFFFF8C00;
    private static final int FOOD_LOW_COLOR = 0xFFFF3300;
    private static final int WATER_COLOR = 0xFF3399FF;
    private static final int WATER_LOW_COLOR = 0xFF0055AA;
    private static final int STAMINA_COLOR = 0xFF33CC33;
    private static final int STAMINA_LOW_COLOR = 0xFFCC3333;
    private static final int HP_COLOR = 0xFFCC0000;
    private static final int HP_LOW_COLOR = 0xFF880000;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int TEXT_SHADOW_COLOR = 0xFF000000;
    private static final int DEBUFF_COLOR = 0xFFFF5555;
    private static final int TEMP_COLD_COLOR = 0xFF88CCFF;
    private static final int TEMP_HOT_COLOR = 0xFFFF6633;
    private static final int TEMP_NORMAL_COLOR = 0xFFAAFFAA;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, StatsHudOverlay::render);
        SevenDaysToMinecraft.LOGGER.info("7DTM: Registered stats HUD overlay (vanilla hunger bar hidden)");
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.hideGui) return;

        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) return;
        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());

        int x = MARGIN_X;
        int y = MARGIN_Y;

        int currentDay = (int) (mc.level.getDayTime() / SevenDaysConstants.DAY_LENGTH) + 1;
        graphics.drawString(mc.font, "Day: " + currentDay, x, y, TEXT_COLOR, true);
        y += 14;

        float foodPct = (stats.getMaxFood() > 0) ? stats.getFood() / stats.getMaxFood() : 0f;
        drawStatBar(graphics, x, y, "Food", foodPct, stats.getFood(), stats.getMaxFood(),
                foodPct < 0.3f ? FOOD_LOW_COLOR : FOOD_COLOR);
        y += BAR_HEIGHT + BAR_SPACING;

        float waterPct = (stats.getMaxWater() > 0) ? stats.getWater() / stats.getMaxWater() : 0f;
        drawStatBar(graphics, x, y, "Water", waterPct, stats.getWater(), stats.getMaxWater(),
                waterPct < 0.3f ? WATER_LOW_COLOR : WATER_COLOR);
        y += BAR_HEIGHT + BAR_SPACING;

        float staminaPct = (stats.getMaxStamina() > 0) ? stats.getStamina() / stats.getMaxStamina() : 0f;
        drawStatBar(graphics, x, y, "Stamina", staminaPct, stats.getStamina(), stats.getMaxStamina(),
                staminaPct < 0.3f ? STAMINA_LOW_COLOR : STAMINA_COLOR);
        y += BAR_HEIGHT + BAR_SPACING;

        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();
        float hpPct = (maxHp > 0) ? hp / maxHp : 0f;
        drawStatBar(graphics, x, y, "HP", hpPct, hp, maxHp,
                hpPct < 0.3f ? HP_LOW_COLOR : HP_COLOR);
        y += BAR_HEIGHT + BAR_SPACING + 2;

        float temp = stats.getCoreTemperature();
        int tempColor = (temp < 50f) ? TEMP_COLD_COLOR : (temp > 90f) ? TEMP_HOT_COLOR : TEMP_NORMAL_COLOR;
        String tempText = String.format("Temp: %.0f°F", temp);
        graphics.drawString(Minecraft.getInstance().font, tempText, x, y, tempColor, true);
        y += 12;

        var debuffs = stats.getDebuffs();
        if (!debuffs.isEmpty()) {
            StringBuilder debuffText = new StringBuilder("Debuffs: ");
            for (var entry : debuffs.entrySet()) {
                debuffText.append(entry.getKey())
                          .append(" (").append(entry.getValue() / 20).append("s) ");
            }
            graphics.drawString(Minecraft.getInstance().font, debuffText.toString(), x, y, DEBUFF_COLOR, true);
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

    @EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class VanillaHealthHider {

        @SubscribeEvent
        public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
            ResourceLocation layerName = event.getName();
            if (layerName.equals(VanillaGuiLayers.PLAYER_HEALTH)) {
                event.setCanceled(true);
            }
        }
    }
}
