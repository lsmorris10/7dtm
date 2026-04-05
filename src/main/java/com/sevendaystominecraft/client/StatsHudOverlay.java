package com.sevendaystominecraft.client;
//penis test 
import com.sevendaystominecraft.SevenDaysConstants;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;
import com.sevendaystominecraft.item.armor.ArmorSetBonusHandler;
import com.sevendaystominecraft.item.armor.ArmorSetBonusHandler.ArmorCounts;
import com.sevendaystominecraft.item.armor.ArmorTier;
import com.sevendaystominecraft.perk.LevelManager;

import com.sevendaystominecraft.network.SyncTerritoryPayload.BuildingEntry;
import com.sevendaystominecraft.network.SyncTerritoryPayload.TerritoryEntry;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
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
    private static final int ARMOR_LIGHT_COLOR = 0xFF55AA55;
    private static final int ARMOR_MEDIUM_COLOR = 0xFFCC8833;
    private static final int ARMOR_HEAVY_COLOR = 0xFFCC3333;
    private static final int ARMOR_DEFAULT_COLOR = 0xFF6688AA;
    private static final float ARMOR_MAX = 19.0f;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int DEBUFF_COLOR = 0xFFFF5555;
    private static final int TEMP_COLD_COLOR = 0xFF88CCFF;
    private static final int TEMP_HOT_COLOR = 0xFFFF6633;
    private static final int TEMP_NORMAL_COLOR = 0xFFAAFFAA;
    private static final int LEVEL_COLOR = 0xFFFFDD00;
    private static final int SPEED_POSITIVE_COLOR = 0xFF55FF55;
    private static final int SPEED_NEGATIVE_COLOR = 0xFFFF5555;
    private static final int SPEED_NEUTRAL_COLOR = 0xFFFFFFFF;
    private static final double BASE_MOVEMENT_SPEED = 0.1;

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
        renderBottomLeftArea(graphics, mc, player);

        // Quest Journal hint
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        String questHint = "§7[J] Quest Journal";
        graphics.drawString(mc.font, questHint, MARGIN_X, screenHeight - 56, 0xFFAAAAAA, true);

        // Progression Tree hint
        String progressionHint = "§7[K] Survival Progression";
        graphics.drawString(mc.font, progressionHint, MARGIN_X, screenHeight - 42, 0xFFAAAAAA, true);
    }

    private static void renderTopLeftStats(GuiGraphics graphics, Minecraft mc, Player player, SevenDaysPlayerStats stats) {
        int x = MARGIN_X;
        int y = MARGIN_Y;

        int currentDay = (int) (mc.level.getDayTime() / SevenDaysConstants.DAY_LENGTH) + 1;
        long timeOfDay = mc.level.getDayTime() % SevenDaysConstants.DAY_LENGTH;
        // Minecraft dayTime 0 = 6:00 AM, so offset by 6000 ticks
        long adjustedTime = (timeOfDay + 6000) % SevenDaysConstants.DAY_LENGTH;
        int totalMinutes = (int) (adjustedTime * 1440 / SevenDaysConstants.DAY_LENGTH);
        int hour24 = totalMinutes / 60;
        int minute = totalMinutes % 60;
        int hour12 = hour24 % 12;
        if (hour12 == 0) hour12 = 12;
        String amPm = hour24 < 12 ? "AM" : "PM";
        String dayAndLevel = String.format("Day: %d  |  Lvl: %d  |  %d:%02d %s", currentDay, stats.getLevel(), hour12, minute, amPm);
        graphics.drawString(mc.font, dayAndLevel, x, y, LEVEL_COLOR, true);
        y += 14;

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPct = (maxHealth > 0) ? health / maxHealth : 0f;
        drawStatBar(graphics, x, y, "Health", healthPct, health, maxHealth,
                healthPct < LOW_THRESHOLD ? HEALTH_LOW_COLOR : HEALTH_COLOR);
        y += BAR_HEIGHT + BAR_SPACING + 2;

        float armorValue = (float) player.getAttributeValue(Attributes.ARMOR);
        float armorMax = Math.max(ARMOR_MAX, armorValue);
        float armorPct = (armorMax > 0) ? armorValue / armorMax : 0f;
        ArmorCounts counts = ArmorSetBonusHandler.computeArmorCounts(player);
        int armorColor = getArmorBarColor(counts);
        drawStatBar(graphics, x, y, "Armor", armorPct, armorValue, armorMax, armorColor);
        String setLabel = getArmorSetLabel(counts);
        if (!setLabel.isEmpty()) {
            int labelX = x + LABEL_WIDTH + BAR_WIDTH + 4 + mc.font.width(String.format("%.1f/%.1f", armorValue, armorMax)) + 6;
            graphics.drawString(mc.font, setLabel, labelX, y, TEXT_COLOR, true);
        }
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

        double currentSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        int speedPctDiff = (int) Math.round(((currentSpeed - BASE_MOVEMENT_SPEED) / BASE_MOVEMENT_SPEED) * 100);
        int speedColor;
        String speedPrefix;
        if (speedPctDiff > 0) {
            speedColor = SPEED_POSITIVE_COLOR;
            speedPrefix = "+";
        } else if (speedPctDiff < 0) {
            speedColor = SPEED_NEGATIVE_COLOR;
            speedPrefix = "";
        } else {
            speedColor = SPEED_NEUTRAL_COLOR;
            speedPrefix = "";
        }
        String speedText = String.format("Speed: %s%d%%", speedPrefix, speedPctDiff);
        graphics.drawString(mc.font, speedText, x, y, speedColor, true);
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

    private static String cachedAreaText = "";
    private static long lastAreaUpdateTick = 0;
    private static final int AREA_UPDATE_INTERVAL = 20;
    private static int lastNearestTerritoryEntityId = -1;

    public static void resetAreaState() {
        cachedAreaText = "";
        lastAreaUpdateTick = 0;
        lastNearestTerritoryEntityId = -1;
    }

    private static final double TERRITORY_DISPLAY_RANGE = 64.0;
    private static final double TERRITORY_DISPLAY_RANGE_SQ = TERRITORY_DISPLAY_RANGE * TERRITORY_DISPLAY_RANGE;
    private static final double BUILDING_DISPLAY_RANGE_SQ = 12.0 * 12.0;
    private static final int BOUNDARY_PADDING = 12;

    private static void renderBottomLeftArea(GuiGraphics graphics, Minecraft mc, Player player) {
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int x = MARGIN_X;
        int y = screenHeight - 30;

        long currentTick = player.tickCount;
        if (currentTick - lastAreaUpdateTick >= AREA_UPDATE_INTERVAL || cachedAreaText.isEmpty()) {
            lastAreaUpdateTick = currentTick;
            StringBuilder sb = new StringBuilder();

            if (mc.level != null) {
                Holder<Biome> biomeHolder = mc.level.getBiome(player.blockPosition());
                String biomeKey = biomeHolder.unwrapKey()
                        .map(k -> k.location().getPath())
                        .orElse("unknown");
                String biomeName = formatBiomeName(biomeKey);
                sb.append(biomeName);
            }

            double playerX = player.getX();
            double playerZ = player.getZ();

            java.util.List<TerritoryEntry> territories = TerritoryClientState.getTerritories();
            TerritoryEntry nearest = null;
            double nearestDistSq = Double.MAX_VALUE;
            for (TerritoryEntry entry : territories) {
                double dx = playerX - entry.x();
                double dz = playerZ - entry.z();
                double distSq = dx * dx + dz * dz;
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    nearest = entry;
                }
            }

            boolean insideTerritory = false;
            if (nearest != null) {
                if (!nearest.buildings().isEmpty()) {
                    // Use bounding box around buildings with padding — matches map boundary
                    int minBX = Integer.MAX_VALUE, maxBX = Integer.MIN_VALUE;
                    int minBZ = Integer.MAX_VALUE, maxBZ = Integer.MIN_VALUE;
                    for (var b : nearest.buildings()) {
                        if (b.x() < minBX) minBX = b.x();
                        if (b.x() > maxBX) maxBX = b.x();
                        if (b.z() < minBZ) minBZ = b.z();
                        if (b.z() > maxBZ) maxBZ = b.z();
                    }
                    minBX -= BOUNDARY_PADDING;
                    minBZ -= BOUNDARY_PADDING;
                    maxBX += BOUNDARY_PADDING;
                    maxBZ += BOUNDARY_PADDING;

                    insideTerritory = playerX >= minBX && playerX <= maxBX
                                  && playerZ >= minBZ && playerZ <= maxBZ;
                } else {
                    // Legacy fallback: circle from origin
                    insideTerritory = nearestDistSq <= TERRITORY_DISPLAY_RANGE_SQ;
                }
            }

            if (nearest != null && insideTerritory) {
                sb.append("  |  ").append(nearest.label());

                int territoryId = nearest.id();
                if (territoryId != lastNearestTerritoryEntityId) {
                    lastNearestTerritoryEntityId = territoryId;
                    TerritoryAnnouncement.trigger(
                            nearest.label(),
                            nearest.tier(),
                            territoryId
                    );
                }
            } else {
                lastNearestTerritoryEntityId = -1;
                TerritoryAnnouncement.clearCurrentTerritory();
            }

            cachedAreaText = sb.toString();
        }

        if (!cachedAreaText.isEmpty()) {
            graphics.drawString(mc.font, cachedAreaText, x, y, 0xFFCCCCCC, true);
        }
    }


    private static String formatBiomeName(String path) {
        String[] parts = path.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (result.length() > 0) result.append(" ");
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) result.append(part.substring(1));
            }
        }
        return result.toString();
    }

    private static int getArmorBarColor(ArmorCounts counts) {
        int total = counts.light() + counts.medium() + counts.heavy();
        if (total == 0) return ARMOR_DEFAULT_COLOR;

        if (counts.heavy() > 0 && counts.light() == 0 && counts.medium() == 0) return ARMOR_HEAVY_COLOR;
        if (counts.medium() > 0 && counts.light() == 0 && counts.heavy() == 0) return ARMOR_MEDIUM_COLOR;
        if (counts.light() > 0 && counts.medium() == 0 && counts.heavy() == 0) return ARMOR_LIGHT_COLOR;

        if (counts.heavy() >= counts.medium() && counts.heavy() >= counts.light()) return ARMOR_HEAVY_COLOR;
        if (counts.medium() >= counts.light()) return ARMOR_MEDIUM_COLOR;
        return ARMOR_LIGHT_COLOR;
    }

    private static String getArmorSetLabel(ArmorCounts counts) {
        if (counts.light() >= 4) {
            return "4pc Light";
        }
        if (counts.medium() >= 4) {
            return "4pc Medium";
        }
        if (counts.heavy() >= 4) {
            return "4pc Heavy";
        }

        int total = counts.light() + counts.medium() + counts.heavy();
        if (total == 0) return "";

        int tiersWorn = 0;
        ArmorTier singleTier = null;
        if (counts.light() > 0) { tiersWorn++; singleTier = ArmorTier.LIGHT; }
        if (counts.medium() > 0) { tiersWorn++; singleTier = ArmorTier.MEDIUM; }
        if (counts.heavy() > 0) { tiersWorn++; singleTier = ArmorTier.HEAVY; }
        if (tiersWorn == 1 && singleTier != null) {
            return singleTier.name().charAt(0) + singleTier.name().substring(1).toLowerCase();
        }
        return "";
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

        String pctText = String.format("%.1f/%.1f", current, max);
        graphics.drawString(mc.font, pctText, barX + BAR_WIDTH + 4, y, TEXT_COLOR, true);
    }


}
