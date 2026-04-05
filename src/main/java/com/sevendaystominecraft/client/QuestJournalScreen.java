package com.sevendaystominecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sevendaystominecraft.network.QuestActionPayload;
import com.sevendaystominecraft.network.SyncQuestPayload.QuestEntry;
import com.sevendaystominecraft.quest.ProgressionNode;
import com.sevendaystominecraft.quest.ProgressionStage;
import com.sevendaystominecraft.quest.ProgressionTreeRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class QuestJournalScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int QUEST_ENTRY_HEIGHT = 60;
    private static final int PROG_ENTRY_HEIGHT = 36;
    private static final int PADDING = 8;
    private static final int HEADER_HEIGHT = 30;

    private static final int BG_COLOR = 0xCC111111;
    private static final int BORDER_COLOR = 0xFF444444;
    private static final int TITLE_COLOR = 0xFFFFCC00;
    private static final int OBJECTIVE_COLOR = 0xFFCCCCCC;
    private static final int PROGRESS_COLOR = 0xFF44FF44;
    private static final int READY_COLOR = 0xFF00FFAA;
    private static final int TRACKED_BORDER = 0xFFFFCC00;
    private static final int HEADER_BG = 0xDD1A1A1A;
    private static final int PROG_SECTION_COLOR = 0xFF88AAFF;
    private static final int PROG_ENTRY_BG = 0xCC1A1A3E;
    private static final int PROG_COMPLETE_COLOR = 0xFF44FF44;
    private static final int PROG_AVAILABLE_COLOR = 0xFFCCDDFF;
    private static final int PROG_LOCKED_COLOR = 0xFF666666;

    private int scrollOffset = 0;

    public QuestJournalScreen() {
        super(Component.literal("Quest Journal"));
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();

        List<QuestEntry> quests = QuestClientState.getActiveQuests();
        String trackedId = QuestClientState.getTrackedQuestId();
        int panelX = (width - PANEL_WIDTH) / 2;

        // Calculate progression section height
        int progSectionHeight = getProgressionSectionHeight();

        int totalContentHeight = progSectionHeight + quests.size() * (QUEST_ENTRY_HEIGHT + 4) + HEADER_HEIGHT + PADDING * 2;
        int panelHeight = Math.min(totalContentHeight, height - 40);
        int startY = (height - panelHeight) / 2;
        int contentY = startY + HEADER_HEIGHT + PADDING;

        // Quest buttons start after progression section
        int questStartY = contentY + progSectionHeight;

        for (int i = 0; i < quests.size(); i++) {
            QuestEntry quest = quests.get(i);
            int entryY = questStartY + i * (QUEST_ENTRY_HEIGHT + 4) - scrollOffset;
            boolean isTracked = quest.questId().equals(trackedId);

            // Track button
            if (!isTracked) {
                int btnY = entryY + QUEST_ENTRY_HEIGHT - 18;
                Button trackBtn = Button.builder(Component.literal("Track"), btn -> {
                    PacketDistributor.sendToServer(
                            new QuestActionPayload(quest.traderId(), quest.questId(), QuestActionPayload.ACTION_TRACK));
                    QuestClientState.updateActiveQuests(QuestClientState.getActiveQuests(), quest.questId());
                    rebuildButtons();
                }).bounds(panelX + PANEL_WIDTH - 56 - PADDING, btnY, 50, 14).build();
                addRenderableWidget(trackBtn);
            }

            // Abandon button
            int abandonBtnY = entryY + QUEST_ENTRY_HEIGHT - 18;
            Button abandonBtn = Button.builder(Component.literal("Abandon"), btn -> {
                PacketDistributor.sendToServer(
                        new QuestActionPayload(quest.traderId(), quest.questId(), QuestActionPayload.ACTION_ABANDON));
            }).bounds(panelX + PADDING, abandonBtnY, 56, 14).build();
            addRenderableWidget(abandonBtn);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dim background
        graphics.fill(0, 0, width, height, 0x88000000);

        List<QuestEntry> quests = QuestClientState.getActiveQuests();
        String trackedId = QuestClientState.getTrackedQuestId();

        int progSectionHeight = getProgressionSectionHeight();
        int totalContentHeight = progSectionHeight + quests.size() * (QUEST_ENTRY_HEIGHT + 4) + HEADER_HEIGHT + PADDING * 2;
        int panelHeight = Math.min(totalContentHeight, height - 40);
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - panelHeight) / 2;

        // Panel background
        graphics.fill(panelX - 2, panelY - 2, panelX + PANEL_WIDTH + 2, panelY + panelHeight + 2, BORDER_COLOR);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, BG_COLOR);

        // Header
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, HEADER_BG);
        int totalQuests = quests.size() + getAvailableProgressionNodes().size();
        String headerText = "Quest Journal (" + totalQuests + " active)";
        graphics.drawString(font, headerText,
                panelX + (PANEL_WIDTH - font.width(headerText)) / 2,
                panelY + (HEADER_HEIGHT - font.lineHeight) / 2,
                TITLE_COLOR, true);

        int contentY = panelY + HEADER_HEIGHT + PADDING;

        // ─── Survival Progression Section ─────────────────────────────────
        contentY = renderProgressionSection(graphics, panelX, contentY);

        // ─── Trader Quests Section ────────────────────────────────────────
        if (quests.isEmpty() && getAvailableProgressionNodes().isEmpty()) {
            String noQuests = "No active quests";
            graphics.drawString(font, noQuests,
                    panelX + (PANEL_WIDTH - font.width(noQuests)) / 2,
                    contentY + 20,
                    0xFF888888, true);
        }

        if (!quests.isEmpty()) {
            // Section header for trader quests
            graphics.drawString(font, "§eTrader Quests", panelX + PADDING, contentY, TITLE_COLOR, true);
            contentY += 14;
        }

        for (int i = 0; i < quests.size(); i++) {
            QuestEntry quest = quests.get(i);
            int entryY = contentY + i * (QUEST_ENTRY_HEIGHT + 4) - scrollOffset;
            boolean isTracked = quest.questId().equals(trackedId);

            // Entry border
            int borderColor = isTracked ? TRACKED_BORDER : BORDER_COLOR;
            graphics.fill(panelX + PADDING - 1, entryY - 1,
                    panelX + PANEL_WIDTH - PADDING + 1, entryY + QUEST_ENTRY_HEIGHT + 1, borderColor);
            graphics.fill(panelX + PADDING, entryY,
                    panelX + PANEL_WIDTH - PADDING, entryY + QUEST_ENTRY_HEIGHT, 0xCC1A1A2E);

            // Quest name
            String name = (isTracked ? "\u25B6 " : "") + quest.questName();
            if (font.width(name) > PANEL_WIDTH - PADDING * 4) {
                name = font.plainSubstrByWidth(name, PANEL_WIDTH - PADDING * 4 - 6) + "...";
            }
            graphics.drawString(font, name, panelX + PADDING + 4, entryY + 4, TITLE_COLOR, true);

            // Objective
            String objective = quest.objectiveDescription();
            if (font.width(objective) > PANEL_WIDTH - PADDING * 4) {
                objective = font.plainSubstrByWidth(objective, PANEL_WIDTH - PADDING * 4 - 6) + "...";
            }
            graphics.drawString(font, objective, panelX + PADDING + 4, entryY + 4 + font.lineHeight + 2, OBJECTIVE_COLOR, true);

            // Progress
            boolean readyToTurnIn = "READY_TO_TURN_IN".equals(quest.stateName());
            String progressText;
            int progressColor;
            if (readyToTurnIn) {
                progressText = "COMPLETE - Return to trader";
                progressColor = READY_COLOR;
            } else {
                progressText = "Progress: " + quest.progress() + "/" + quest.targetCount();
                progressColor = PROGRESS_COLOR;
            }
            graphics.drawString(font, progressText, panelX + PADDING + 4,
                    entryY + 4 + (font.lineHeight + 2) * 2, progressColor, true);

            // Rewards
            String rewards = "Rewards: " + quest.rewardXp() + " XP, " + quest.rewardTokens() + " Coins";
            graphics.drawString(font, rewards, panelX + PADDING + 4,
                    entryY + 4 + (font.lineHeight + 2) * 3, 0xFF999999, true);
        }

        // Render buttons on top
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // ─── Progression Section Logic ───────────────────────────────────────

    private List<ProgressionNode> getAvailableProgressionNodes() {
        Set<String> completed = ProgressionClientState.getCompletedNodes();
        List<ProgressionNode> available = new ArrayList<>();

        for (ProgressionNode node : ProgressionTreeRegistry.getAllNodes()) {
            if (completed.contains(node.getId())) continue;  // skip completed
            // Only show nodes whose prerequisites are met
            if (ProgressionTreeRegistry.arePrerequisitesMet(node, completed)) {
                available.add(node);
            }
        }
        return available;
    }

    private int getProgressionSectionHeight() {
        List<ProgressionNode> available = getAvailableProgressionNodes();
        if (available.isEmpty()) return 0;
        // Section header (14) + entries + gap
        return 14 + available.size() * (PROG_ENTRY_HEIGHT + 3) + 8;
    }

    private int renderProgressionSection(GuiGraphics graphics, int panelX, int startY) {
        List<ProgressionNode> available = getAvailableProgressionNodes();
        if (available.isEmpty()) return startY;

        Set<String> completed = ProgressionClientState.getCompletedNodes();
        int completedTotal = completed.size();
        int totalNodes = ProgressionTreeRegistry.getTotalNodeCount();

        // Section header
        String sectionTitle = "Survival Progression (" + completedTotal + "/" + totalNodes + ")";
        graphics.drawString(font, sectionTitle, panelX + PADDING, startY, PROG_SECTION_COLOR, true);
        startY += 14;

        // Current stage label
        int highestStage = ProgressionClientState.getHighestCompletedStage();
        ProgressionStage currentStage;
        if (highestStage >= ProgressionStage.values().length) {
            currentStage = ProgressionStage.SELF_SUFFICIENT;
        } else {
            currentStage = ProgressionStage.fromNumber(highestStage + 1);
        }

        for (ProgressionNode node : available) {
            int entryY = startY;

            // Entry background
            graphics.fill(panelX + PADDING - 1, entryY - 1,
                    panelX + PANEL_WIDTH - PADDING + 1, entryY + PROG_ENTRY_HEIGHT + 1, 0xFF334466);
            graphics.fill(panelX + PADDING, entryY,
                    panelX + PANEL_WIDTH - PADDING, entryY + PROG_ENTRY_HEIGHT, PROG_ENTRY_BG);

            // Stage badge
            String stageBadge = "[" + node.getStage().getDisplayName() + "]";
            graphics.drawString(font, stageBadge, panelX + PADDING + 4, entryY + 3, 0xFF6688CC, false);

            // Node name
            String nodeName = node.getDisplayName();
            int nameX = panelX + PADDING + 4 + font.width(stageBadge) + 4;
            if (font.width(nodeName) > PANEL_WIDTH - PADDING * 4 - font.width(stageBadge) - 8) {
                nodeName = font.plainSubstrByWidth(nodeName, PANEL_WIDTH - PADDING * 4 - font.width(stageBadge) - 14) + "...";
            }
            graphics.drawString(font, nodeName, nameX, entryY + 3, PROG_AVAILABLE_COLOR, true);

            // Description / objective
            String desc = node.getDescription();
            if (font.width(desc) > PANEL_WIDTH - PADDING * 4) {
                desc = font.plainSubstrByWidth(desc, PANEL_WIDTH - PADDING * 4 - 6) + "...";
            }
            graphics.drawString(font, desc, panelX + PADDING + 4, entryY + 3 + font.lineHeight + 2, OBJECTIVE_COLOR, false);

            // Reward on the right
            String reward = node.getRewardText();
            if (!reward.isEmpty()) {
                int rewardX = panelX + PANEL_WIDTH - PADDING - font.width(reward) - 4;
                graphics.drawString(font, reward, rewardX, entryY + 3 + font.lineHeight + 2, 0xFF88CC88, false);
            }

            startY += PROG_ENTRY_HEIGHT + 3;
        }

        startY += 5; // gap before trader quests
        return startY;
    }

    // ─── Input Handling ──────────────────────────────────────────────────

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<QuestEntry> quests = QuestClientState.getActiveQuests();
        int progHeight = getProgressionSectionHeight();
        int totalHeight = progHeight + quests.size() * (QUEST_ENTRY_HEIGHT + 4);
        int visibleHeight = height - 80;

        if (totalHeight > visibleHeight) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int)(scrollY * 20), totalHeight - visibleHeight));
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeyBindings.OPEN_QUEST_JOURNAL.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
