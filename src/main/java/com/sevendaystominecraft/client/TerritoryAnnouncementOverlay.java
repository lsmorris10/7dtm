package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class TerritoryAnnouncementOverlay {

    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "territory_announcement");

    private static final int COLOR_EASY   = 0xFF44FF44;
    private static final int COLOR_MEDIUM = 0xFFFFCC00;
    private static final int COLOR_HARD   = 0xFFFF4444;

    private static final float START_SCALE = 3.0f;
    private static final float END_SCALE = 1.0f;

    private static final int BOTTOM_LEFT_X = 10;
    private static final int BOTTOM_LEFT_Y_OFFSET = 30;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, TerritoryAnnouncementOverlay::render);
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        if (!TerritoryAnnouncement.isActive()) return;

        String name = TerritoryAnnouncement.getTerritoryName();
        int tier = TerritoryAnnouncement.getTerritoryTier();
        String stars = "★".repeat(Math.max(1, tier));
        String displayText = name + " " + stars;
        int color = getTierColor(tier);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;

        float targetX = BOTTOM_LEFT_X;
        float targetY = screenHeight - BOTTOM_LEFT_Y_OFFSET;

        float scale;
        float drawX;
        float drawY;

        int textWidth = mc.font.width(displayText);

        if (TerritoryAnnouncement.getPhase() == TerritoryAnnouncement.Phase.HOLD) {
            scale = START_SCALE;
            drawX = centerX - (textWidth * scale) / 2.0f;
            drawY = centerY - (mc.font.lineHeight * scale) / 2.0f;
        } else {
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
            float smoothT = Math.min(1.0f, (TerritoryAnnouncement.getTicksInPhase() + partialTick) / TerritoryAnnouncement.getAnimateTicks());
            smoothT = smoothT * smoothT * (3.0f - 2.0f * smoothT);

            scale = START_SCALE + (END_SCALE - START_SCALE) * smoothT;

            float startX = centerX - (textWidth * START_SCALE) / 2.0f;
            float startY = centerY - (mc.font.lineHeight * START_SCALE) / 2.0f;

            drawX = startX + (targetX - startX) * smoothT;
            drawY = startY + (targetY - startY) * smoothT;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(drawX, drawY, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(mc.font, displayText, 0, 0, color, true);
        graphics.pose().popPose();
    }

    private static int getTierColor(int tier) {
        if (tier <= 2) return COLOR_EASY;
        if (tier == 3) return COLOR_MEDIUM;
        return COLOR_HARD;
    }
}
