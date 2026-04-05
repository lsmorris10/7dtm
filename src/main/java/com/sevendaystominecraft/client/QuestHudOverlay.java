package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.network.SyncQuestPayload.QuestEntry;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

public class QuestHudOverlay {

    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "quest_hud");

    private static final int BG_COLOR = 0xAA000000;
    private static final int BORDER_COLOR = 0xFF333333;
    private static final int TITLE_COLOR = 0xFFFFCC00;
    private static final int OBJECTIVE_COLOR = 0xFFCCCCCC;
    private static final int PROGRESS_COLOR = 0xFF44FF44;
    private static final int READY_COLOR = 0xFF00FFAA;
    private static final int QUEST_WIDTH = 150;
    private static final int QUEST_PADDING = 4;
    private static final int QUEST_SPACING = 2;
    private static final int MARGIN_LEFT = 8;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, QuestHudOverlay::render);
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.hideGui) return;

        List<QuestEntry> quests = QuestClientState.getActiveQuests();
        if (quests.isEmpty()) return;

        int startY = MinimapOverlay.MARGIN + MinimapOverlay.MAP_SIZE + 25;
        int x = MARGIN_LEFT;

        String trackedId = QuestClientState.getTrackedQuestId();
        for (QuestEntry quest : quests) {
            boolean isTracked = quest.questId().equals(trackedId);
            int questHeight = renderQuest(graphics, mc, quest, x, startY, isTracked);
            startY += questHeight + QUEST_SPACING;
        }
    }

    private static int renderQuest(GuiGraphics graphics, Minecraft mc, QuestEntry quest, int x, int y, boolean isTracked) {
        int lineHeight = mc.font.lineHeight;
        int innerHeight = QUEST_PADDING * 2 + lineHeight * 3 + 6;

        graphics.fill(x - 1, y - 1, x + QUEST_WIDTH + 1, y + innerHeight + 1, isTracked ? 0xFFFFCC00 : BORDER_COLOR);
        graphics.fill(x, y, x + QUEST_WIDTH, y + innerHeight, BG_COLOR);

        String name = isTracked ? "\u25B6 " + quest.questName() : quest.questName();
        if (mc.font.width(name) > QUEST_WIDTH - QUEST_PADDING * 2) {
            name = mc.font.plainSubstrByWidth(name, QUEST_WIDTH - QUEST_PADDING * 2 - 6) + "...";
        }
        graphics.drawString(mc.font, name, x + QUEST_PADDING, y + QUEST_PADDING, TITLE_COLOR, true);

        String objective = quest.objectiveDescription();
        if (mc.font.width(objective) > QUEST_WIDTH - QUEST_PADDING * 2) {
            objective = mc.font.plainSubstrByWidth(objective, QUEST_WIDTH - QUEST_PADDING * 2 - 6) + "...";
        }
        graphics.drawString(mc.font, objective, x + QUEST_PADDING, y + QUEST_PADDING + lineHeight + 2, OBJECTIVE_COLOR, true);

        boolean readyToTurnIn = "READY_TO_TURN_IN".equals(quest.stateName());
        String progressText;
        int progressColor;
        if (readyToTurnIn) {
            progressText = "COMPLETE - Return to trader";
            progressColor = READY_COLOR;
        } else {
            progressText = quest.progress() + "/" + quest.targetCount();
            progressColor = PROGRESS_COLOR;
        }
        graphics.drawString(mc.font, progressText, x + QUEST_PADDING, y + QUEST_PADDING + (lineHeight + 2) * 2, progressColor, true);

        if (!readyToTurnIn && quest.targetCount() > 0) {
            int barX = x + QUEST_PADDING;
            int barY = y + innerHeight - QUEST_PADDING - 2;
            int barWidth = QUEST_WIDTH - QUEST_PADDING * 2;
            int barHeight = 2;
            float fillRatio = (float) quest.progress() / quest.targetCount();

            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
            graphics.fill(barX, barY, barX + (int) (barWidth * fillRatio), barY + barHeight, PROGRESS_COLOR);
        }

        return innerHeight;
    }
}
