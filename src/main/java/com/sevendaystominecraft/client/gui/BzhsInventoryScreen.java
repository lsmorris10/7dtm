package com.sevendaystominecraft.client.gui;

import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.item.armor.ArmorSetBonusHandler;
import com.sevendaystominecraft.perk.Attribute;
import com.sevendaystominecraft.perk.LevelManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Map;

public class BzhsInventoryScreen extends AbstractContainerScreen<InventoryMenu> {

    private static final int BG_COLOR = 0xFF1A1A1A;
    private static final int PANEL_BORDER = 0xFF3A3A3A;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int DARK_GRAY = 0xFF666666;

    private static final int HEALTH_COLOR = 0xFFCC3333;
    private static final int STAMINA_COLOR = 0xFF33CC33;
    private static final int FOOD_COLOR = 0xFFCC8833;
    private static final int WATER_COLOR = 0xFF3388CC;
    private static final int XP_COLOR = 0xFF9933FF;
    private static final int LEVEL_COLOR = 0xFFFFDD00;
    private static final int DEBUFF_COLOR = 0xFFFF5555;
    private static final int TEMP_COLD_COLOR = 0xFF88CCFF;
    private static final int TEMP_HOT_COLOR = 0xFFFF6633;
    private static final int TEMP_NORMAL_COLOR = 0xFFAAFFAA;

    private static final int LIGHT_ARMOR_COLOR = 0xFF55FF55;
    private static final int MEDIUM_ARMOR_COLOR = 0xFFFFAA00;
    private static final int HEAVY_ARMOR_COLOR = 0xFFFF5555;

    private static final int STR_COLOR = 0xFFFF4444;
    private static final int PER_COLOR = 0xFF44AAFF;
    private static final int FOR_COLOR = 0xFF44FF44;
    private static final int AGI_COLOR = 0xFFFFFF44;
    private static final int INT_COLOR = 0xFFDD44FF;

    private static final int STAT_BAR_WIDTH = 90;
    private static final int STAT_BAR_HEIGHT = 6;
    private static final float LOW_THRESHOLD = 0.3f;

    private static final int[] ATTRIBUTE_COLORS = { STR_COLOR, PER_COLOR, FOR_COLOR, AGI_COLOR, INT_COLOR };

    private static final ResourceLocation[] ATTRIBUTE_ICONS = {
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/attribute_strength.png"),
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/attribute_perception.png"),
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/attribute_fortitude.png"),
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/attribute_agility.png"),
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/attribute_intellect.png")
    };

    private static final ResourceLocation DEBUFF_ICON_GENERIC =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/heart_low.png");

    private static final ResourceLocation INVENTORY_BG =
            ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    public BzhsInventoryScreen(InventoryMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        SevenDaysPlayerStats stats = null;
        if (player.hasData(ModAttachments.PLAYER_STATS.get())) {
            stats = player.getData(ModAttachments.PLAYER_STATS.get());
        }

        if (stats != null) {
            renderStatsPanel(graphics, mc, player, stats);
            renderDebuffs(graphics, stats, mouseX, mouseY);
            renderXpBar(graphics, stats);
            renderPerkSummary(graphics, stats);
        }

        renderArmorSetBonus(graphics, player);

        renderTooltip(graphics, mouseX, mouseY);

        if (stats != null) {
            renderDebuffTooltips(graphics, stats, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int i = leftPos;
        int j = topPos;

        graphics.fill(i - 2, j - 2, i + imageWidth + 2, j + imageHeight + 2, BG_COLOR);
        drawBorder(graphics, i - 2, j - 2, imageWidth + 4, imageHeight + 4, PANEL_BORDER);

        graphics.blit(RenderType::guiTextured, INVENTORY_BG, i, j, 0, 0, imageWidth, imageHeight, 256, 256);

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    leftPos + 26, topPos + 8,
                    leftPos + 75, topPos + 78,
                    30, 0.0625f,
                    (float) (leftPos + 51) - mouseX,
                    (float) (topPos + 25) - mouseY,
                    player
            );
        }
    }

    private void renderStatsPanel(GuiGraphics graphics, Minecraft mc, Player player, SevenDaysPlayerStats stats) {
        int panelX = leftPos + imageWidth + 6;
        int panelY = topPos;
        int panelW = 145;
        int panelH = 105;

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_COLOR);
        drawBorder(graphics, panelX, panelY, panelW, panelH, PANEL_BORDER);

        int sx = panelX + 5;
        int sy = panelY + 5;

        int currentDay = 1;
        if (mc.level != null) {
            currentDay = (int) (mc.level.getDayTime() / SevenDaysConstants.DAY_LENGTH) + 1;
        }

        String header = String.format("Day %d | Level %d", currentDay, stats.getLevel());
        graphics.drawString(font, header, sx, sy, LEVEL_COLOR, true);
        sy += 13;

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPct = maxHealth > 0 ? health / maxHealth : 0f;
        drawStatBar(graphics, sx, sy, "HP", healthPct, health, maxHealth,
                healthPct < LOW_THRESHOLD ? 0xFF881111 : HEALTH_COLOR);
        sy += STAT_BAR_HEIGHT + 4;

        float staminaPct = stats.getMaxStamina() > 0 ? stats.getStamina() / stats.getMaxStamina() : 0f;
        drawStatBar(graphics, sx, sy, "STA", staminaPct, stats.getStamina(), stats.getMaxStamina(),
                staminaPct < LOW_THRESHOLD ? HEALTH_COLOR : STAMINA_COLOR);
        sy += STAT_BAR_HEIGHT + 4;

        float food = stats.getFood();
        float maxFood = stats.getMaxFood();
        float foodPct = maxFood > 0 ? food / maxFood : 0f;
        drawStatBar(graphics, sx, sy, "Food", foodPct, food, maxFood,
                foodPct < LOW_THRESHOLD ? HEALTH_COLOR : FOOD_COLOR);
        sy += STAT_BAR_HEIGHT + 4;

        float water = stats.getWater();
        float maxWater = stats.getMaxWater();
        float waterPct = maxWater > 0 ? water / maxWater : 0f;
        drawStatBar(graphics, sx, sy, "Water", waterPct, water, maxWater,
                waterPct < LOW_THRESHOLD ? 0xFF224488 : WATER_COLOR);
        sy += STAT_BAR_HEIGHT + 4;

        float temp = stats.getCoreTemperature();
        int tempColor = temp < 50f ? TEMP_COLD_COLOR : temp > 90f ? TEMP_HOT_COLOR : TEMP_NORMAL_COLOR;
        graphics.drawString(font, String.format("Temp: %.0f\u00B0F", temp), sx, sy, tempColor, true);
    }

    private void renderDebuffs(GuiGraphics graphics, SevenDaysPlayerStats stats, int mouseX, int mouseY) {
        Map<String, Integer> debuffs = stats.getDebuffs();

        int panelX = leftPos + imageWidth + 6;
        int panelY = topPos + 108;
        int panelW = 145;
        int panelH = 58;

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_COLOR);
        drawBorder(graphics, panelX, panelY, panelW, panelH, PANEL_BORDER);

        int dx = panelX + 5;
        int dy = panelY + 5;

        graphics.drawString(font, "Active Debuffs", dx, dy, DEBUFF_COLOR, false);
        dy += 11;

        if (debuffs.isEmpty()) {
            graphics.drawString(font, "None", dx, dy, DARK_GRAY, false);
            return;
        }

        int count = 0;
        for (Map.Entry<String, Integer> entry : debuffs.entrySet()) {
            if (count >= 4) break;
            String name = formatDebuffName(entry.getKey());
            int seconds = entry.getValue() / 20;

            ResourceLocation icon = getDebuffIcon(entry.getKey());
            if (icon == null) {
                icon = DEBUFF_ICON_GENERIC;
            }
            graphics.blit(RenderType::guiTextured, icon, dx, dy, 0, 0, 8, 8, 8, 8);
            graphics.drawString(font, name + " (" + seconds + "s)", dx + 10, dy, DEBUFF_COLOR, false);
            dy += 10;
            count++;
        }
    }

    private void renderDebuffTooltips(GuiGraphics graphics, SevenDaysPlayerStats stats, int mouseX, int mouseY) {
        Map<String, Integer> debuffs = stats.getDebuffs();
        if (debuffs.isEmpty()) return;

        int panelX = leftPos + imageWidth + 6;
        int dy = topPos + 108 + 16;

        int count = 0;
        for (Map.Entry<String, Integer> entry : debuffs.entrySet()) {
            if (count >= 4) break;
            int rowY = dy + count * 10;
            if (mouseX >= panelX + 5 && mouseX <= panelX + 140
                    && mouseY >= rowY && mouseY <= rowY + 9) {
                String description = getDebuffDescription(entry.getKey());
                int seconds = entry.getValue() / 20;
                graphics.renderTooltip(font,
                        java.util.List.of(
                                Component.literal(formatDebuffName(entry.getKey())).withStyle(s -> s.withColor(DEBUFF_COLOR)),
                                Component.literal(description).withStyle(s -> s.withColor(GRAY)),
                                Component.literal("Time remaining: " + seconds + "s").withStyle(s -> s.withColor(DARK_GRAY))
                        ),
                        java.util.Optional.empty(),
                        mouseX, mouseY);
                break;
            }
            count++;
        }
    }

    private String getDebuffDescription(String debuffId) {
        return switch (debuffId) {
            case "bleeding" -> "You are losing blood. Use a bandage to stop.";
            case "infection_1" -> "Stage 1 infection. Cure with antibiotics.";
            case "infection_2" -> "Stage 2 infection! Find a cure quickly.";
            case "burn" -> "You are on fire. Seek water.";
            case "electrocuted" -> "Electrical shock. Movement impaired.";
            case "radiation" -> "Radiation poisoning. Leave the area.";
            case "concussion" -> "Concussed. Vision impaired.";
            case "sprain" -> "Sprained limb. Movement reduced.";
            case "fracture" -> "Broken bone. Splint required.";
            default -> "A harmful condition is affecting you.";
        };
    }

    private void renderXpBar(GuiGraphics graphics, SevenDaysPlayerStats stats) {
        int panelX = leftPos - 151;
        int panelY = topPos;
        int panelW = 145;
        int panelH = 30;

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_COLOR);
        drawBorder(graphics, panelX, panelY, panelW, panelH, PANEL_BORDER);

        int xpBarX = panelX + 5;
        int xpBarY = panelY + 18;
        int xpBarW = panelW - 10;
        int xpBarH = 5;

        int xpNeeded = LevelManager.xpToNextLevel(stats.getLevel());
        float xpPct = xpNeeded > 0 ? (float) stats.getXp() / xpNeeded : 0f;

        graphics.drawString(font, "XP", xpBarX, xpBarY - 12, XP_COLOR, false);
        String xpText = String.format("%d / %d", stats.getXp(), xpNeeded);
        int xpTextWidth = font.width(xpText);
        graphics.drawString(font, xpText, xpBarX + xpBarW - xpTextWidth, xpBarY - 12, GRAY, false);

        graphics.fill(xpBarX, xpBarY, xpBarX + xpBarW, xpBarY + xpBarH, 0xFF111111);
        drawBorder(graphics, xpBarX, xpBarY, xpBarW, xpBarH, PANEL_BORDER);

        int filledW = Math.round(xpBarW * Math.max(0f, Math.min(1f, xpPct)));
        if (filledW > 0) {
            graphics.fill(xpBarX, xpBarY, xpBarX + filledW, xpBarY + xpBarH, XP_COLOR);
        }
    }

    private void renderPerkSummary(GuiGraphics graphics, SevenDaysPlayerStats stats) {
        int panelX = leftPos - 151;
        int panelY = topPos + 34;
        int panelW = 145;
        int rowHeight = 14;
        Attribute[] attributes = Attribute.values();
        int panelH = 5 + 12 + attributes.length * rowHeight + 5;

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_COLOR);
        drawBorder(graphics, panelX, panelY, panelW, panelH, PANEL_BORDER);

        int px = panelX + 5;
        int py = panelY + 5;

        graphics.drawString(font, "Attributes", px, py, GRAY, false);
        py += 12;

        for (int i = 0; i < attributes.length; i++) {
            Attribute attr = attributes[i];
            int rowY = py + i * rowHeight;
            int level = stats.getAttributeLevel(attr);

            graphics.blit(RenderType::guiTextured, ATTRIBUTE_ICONS[i], px, rowY, 0, 0, 12, 12, 12, 12);

            String displayName = attr.getDisplayName();
            graphics.drawString(font, displayName, px + 15, rowY + 2, ATTRIBUTE_COLORS[i], false);

            String levelStr = String.valueOf(level);
            int levelW = font.width(levelStr);
            graphics.drawString(font, levelStr, panelX + panelW - 5 - levelW, rowY + 2, ATTRIBUTE_COLORS[i], true);
        }
    }

    private void renderArmorSetBonus(GuiGraphics graphics, Player player) {
        ArmorSetBonusHandler.ArmorCounts counts = ArmorSetBonusHandler.computeArmorCounts(player);

        int panelX = leftPos - 151;
        int panelY = topPos + 130;
        int panelW = 145;
        int panelH = 30;

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_COLOR);
        drawBorder(graphics, panelX, panelY, panelW, panelH, PANEL_BORDER);

        int tx = panelX + 5;
        int ty = panelY + 5;

        String bonusText;
        int bonusColor;

        if (counts.heavy() >= 4) {
            bonusText = "Heavy (4): -25% DMG";
            bonusColor = HEAVY_ARMOR_COLOR;
        } else if (counts.heavy() >= 2) {
            bonusText = "Heavy (2): -12% DMG";
            bonusColor = HEAVY_ARMOR_COLOR;
        } else if (counts.medium() >= 4) {
            bonusText = "Medium (4): +20% STA";
            bonusColor = MEDIUM_ARMOR_COLOR;
        } else if (counts.medium() >= 2) {
            bonusText = "Medium (2): +10% STA";
            bonusColor = MEDIUM_ARMOR_COLOR;
        } else if (counts.light() >= 4) {
            bonusText = "Light (4): 0% Noise";
            bonusColor = LIGHT_ARMOR_COLOR;
        } else if (counts.light() >= 2) {
            bonusText = "Light (2): -50% Noise";
            bonusColor = LIGHT_ARMOR_COLOR;
        } else {
            bonusText = "No set bonus";
            bonusColor = DARK_GRAY;
        }

        graphics.drawString(font, "Armor Set:", tx, ty, GRAY, false);
        graphics.drawString(font, bonusText, tx, ty + 11, bonusColor, false);
    }

    private void drawStatBar(GuiGraphics graphics, int x, int y, String label, float pct, float current, float max, int color) {
        int labelWidth = 32;
        graphics.drawString(font, label, x, y, WHITE, true);

        int barX = x + labelWidth;
        graphics.fill(barX - 1, y - 1, barX + STAT_BAR_WIDTH + 1, y + STAT_BAR_HEIGHT + 1, PANEL_BORDER);
        graphics.fill(barX, y, barX + STAT_BAR_WIDTH, y + STAT_BAR_HEIGHT, 0xFF111111);

        int filledW = Math.round(STAT_BAR_WIDTH * Math.max(0f, Math.min(1f, pct)));
        if (filledW > 0) {
            graphics.fill(barX, y, barX + filledW, y + STAT_BAR_HEIGHT, color);
        }

        String valText = String.format("%.0f/%.0f", current, max);
        graphics.drawString(font, valText, barX + STAT_BAR_WIDTH + 3, y - 1, WHITE, true);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private ResourceLocation getDebuffIcon(String debuffId) {
        return switch (debuffId) {
            case "bleeding" -> ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/vignette_bleeding.png");
            case "infection_1", "infection_2" -> ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/vignette_infection.png");
            case "burn" -> ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/vignette_burn.png");
            case "electrocuted" -> ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/vignette_electrocuted.png");
            case "radiation" -> ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "textures/gui/vignette_radiation.png");
            default -> null;
        };
    }

    private String formatDebuffName(String id) {
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
