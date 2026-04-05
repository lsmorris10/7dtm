package com.sevendaystominecraft.trader;

import com.sevendaystominecraft.client.QuestClientState;
import com.sevendaystominecraft.item.ModItems;
import com.sevendaystominecraft.network.QuestActionPayload;
import com.sevendaystominecraft.network.SyncQuestPayload.QuestEntry;
import com.sevendaystominecraft.network.SyncTraderQuestsPayload.TraderQuestEntry;
import com.sevendaystominecraft.network.TraderActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import com.sevendaystominecraft.client.TraderDialogueClientState;

import java.util.ArrayList;
import java.util.List;

public class TraderScreen extends AbstractContainerScreen<TraderMenu> {

    // Vanilla-matching colors — same gray as the vanilla inventory background
    private static final int BG_COLOR = 0xFFC6C6C6;
    private static final int HEADER_COLOR = 0xFF8B8B8B;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int SLOT_INNER = 0xFF373737;
    private static final int BUY_COLOR = 0xFF00AA00;
    private static final int SELL_COLOR = 0xFFAAAA00;
    private static final int CYAN = 0xFF00CCCC;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF3F3F3F;
    private static final int GRAY = 0xFF555555;
    private static final int DARK_GRAY = 0xFF666666;
    private static final int RED = 0xFFFF4444;
    private static final int QUEST_COLOR = 0xFFAA6600;
    private static final int READY_COLOR = 0xFF008844;
    private static final int SECRET_COLOR = 0xFF9922CC;

    // Tab row colors on vanilla-light background
    private static final int ROW_BG = 0xFF8B8B8B;
    private static final int ROW_HOVER = 0xFF9B9B9B;
    private static final int QUEST_ROW_BG = 0xFF8B8B8B;
    private static final int QUEST_ROW_HOVER = 0xFF9B9B9B;
    private static final int QUEST_ROW_TRACKED = 0xFF9B9B6B;
    private static final int SECRET_ROW_BG = 0xFF9B7B9B;
    private static final int SECRET_ROW_HOVER = 0xFFAB8BAB;

    private enum Tab { BUY, SELL, QUESTS, SECRET_STASH }
    private Tab currentTab = Tab.BUY;
    private int scrollOffset = 0;
    private int questScrollOffset = 0;
    private int totalQuestContentHeight = 0;
    private static final int VISIBLE_ROWS = 5;

    // Dialogue chat-style state
    private int dialogueIndex = 0;  // which message we're currently showing
    private List<String> wrappedCurrentMessage = null;
    private Button nextDialogueButton;

    private static final int BUBBLE_WIDTH = 160;
    private static final int BUBBLE_PADDING = 8;
    private static final int BUBBLE_GAP = 6;
    private static final int BUBBLE_BG = 0xEE1A1A2E;
    private static final int BUBBLE_BORDER = 0xFF3A3A5E;
    private static final int BUBBLE_TEXT_COLOR = 0xFFDDDDDD;
    private static final int BUBBLE_TITLE_COLOR = 0xFFFFCC00;

    private Button sellButton;

    public TraderScreen(TraderMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = 128;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Buy"), b -> {
            currentTab = Tab.BUY;
            scrollOffset = 0;
        }).bounds(leftPos + 8, topPos + 5, 36, 14).build());

        addRenderableWidget(Button.builder(Component.literal("Sell"), b -> {
            currentTab = Tab.SELL;
            scrollOffset = 0;
        }).bounds(leftPos + 48, topPos + 5, 36, 14).build());

        addRenderableWidget(Button.builder(Component.literal("Quests"), b -> {
            currentTab = Tab.QUESTS;
            questScrollOffset = 0;
        }).bounds(leftPos + 88, topPos + 5, 46, 14).build());

        if (!menu.getSecretStash().isEmpty()) {
            addRenderableWidget(Button.builder(Component.literal("Stash"), b -> {
                currentTab = Tab.SECRET_STASH;
                scrollOffset = 0;
            }).bounds(leftPos + 138, topPos + 5, 34, 14).build());
        }

        sellButton = Button.builder(Component.literal("Sell Items"), b -> {
            PacketDistributor.sendToServer(new TraderActionPayload(menu.getTraderId(), 0, false));
        }).bounds(leftPos + 80, topPos + 108, 60, 16).build();
        addRenderableWidget(sellButton);

        // "Next" button for dialogue — positioned in the dialogue bubble area
        int bubbleX = leftPos - BUBBLE_WIDTH - BUBBLE_GAP;
        if (bubbleX < 2) bubbleX = 2;
        nextDialogueButton = Button.builder(Component.literal("Next ▶"), b -> {
            advanceDialogue();
        }).bounds(bubbleX + BUBBLE_WIDTH - 52, topPos + imageHeight - 20, 48, 16).build();
        addRenderableWidget(nextDialogueButton);

        // Initialize dialogue state
        dialogueIndex = 0;
        wrappedCurrentMessage = null;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Draw vanilla-style container background
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BG_COLOR);
        // Top header bar (slightly darker)
        graphics.fill(leftPos + 4, topPos + 2, leftPos + imageWidth - 4, topPos + 22, HEADER_COLOR);
        // Inner border lines to match vanilla feel
        // Top highlight
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, WHITE);
        graphics.fill(leftPos, topPos, leftPos + 1, topPos + imageHeight, WHITE);
        // Bottom shadow
        graphics.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);
        graphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);

        int tokenCount = countPlayerTokens();
        String tokenText = "Coins: " + tokenCount;
        graphics.drawString(font, tokenText, leftPos + imageWidth - font.width(tokenText) - 8, topPos + 8, 0xFF006666, true);

        sellButton.visible = currentTab == Tab.SELL;

        switch (currentTab) {
            case BUY -> renderBuyTab(graphics, mouseX, mouseY);
            case SELL -> renderSellTab(graphics, mouseX, mouseY);
            case QUESTS -> renderQuestsTab(graphics, mouseX, mouseY);
            case SECRET_STASH -> renderSecretStashTab(graphics, mouseX, mouseY);
        }

        renderDialogueBubble(graphics, mouseX, mouseY);

        // Update next button visibility & position
        List<String> dialogue = TraderDialogueClientState.getDialogue();
        nextDialogueButton.visible = !dialogue.isEmpty() && dialogueIndex < dialogue.size() - 1;
        int bx = leftPos - BUBBLE_WIDTH - BUBBLE_GAP;
        if (bx < 2) bx = 2;
        nextDialogueButton.setX(bx + BUBBLE_WIDTH - 52);

        int playerInvY = topPos + 139;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = leftPos + 7 + col * 18;
                int sy = playerInvY + row * 18;
                graphics.fill(sx, sy, sx + 18, sy + 18, SLOT_BG);
                graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_INNER);
            }
        }
        for (int col = 0; col < 9; col++) {
            int sx = leftPos + 7 + col * 18;
            int sy = playerInvY + 58;
            graphics.fill(sx, sy, sx + 18, sy + 18, SLOT_BG);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_INNER);
        }
    }

    private void renderBuyTab(GuiGraphics graphics, int mouseX, int mouseY) {
        List<TraderInventory.TraderOffer> offers = menu.getOffers();
        int startY = topPos + 24;

        for (int i = 0; i < VISIBLE_ROWS && (i + scrollOffset) < offers.size(); i++) {
            int offerIdx = i + scrollOffset;
            TraderInventory.TraderOffer offer = offers.get(offerIdx);

            int rowY = startY + i * 22;
            boolean hovered = mouseX >= leftPos + 6 && mouseX <= leftPos + 170 &&
                              mouseY >= rowY && mouseY <= rowY + 20;

            graphics.fill(leftPos + 6, rowY, leftPos + 170, rowY + 20, hovered ? ROW_HOVER : ROW_BG);

            graphics.renderItem(offer.item(), leftPos + 8, rowY + 2);
            graphics.renderItemDecorations(font, offer.item(), leftPos + 8, rowY + 2);

            String itemName = offer.item().getHoverName().getString();
            if (font.width(itemName) > 90) {
                itemName = font.plainSubstrByWidth(itemName, 87) + "...";
            }
            graphics.drawString(font, itemName, leftPos + 28, rowY + 2, BLACK, false);

            int adjustedPrice = menu.getAdjustedBuyPrice(offer);
            int stock = menu.getStock(offerIdx);
            String priceText = adjustedPrice + " coins";
            int tokenColor = countPlayerTokens() >= adjustedPrice && stock > 0 ? BUY_COLOR : RED;
            graphics.drawString(font, priceText, leftPos + 28, rowY + 11, tokenColor, false);

            String stockText = stock > 0 ? "x" + stock : "SOLD OUT";
            int stockColor = stock > 0 ? GRAY : RED;
            int stockX = leftPos + 170 - font.width(stockText) - 2;
            graphics.drawString(font, stockText, stockX, rowY + 6, stockColor, false);
        }

        if (offers.size() > VISIBLE_ROWS) {
            String scrollInfo = (scrollOffset + 1) + "-" + Math.min(scrollOffset + VISIBLE_ROWS, offers.size()) + " / " + offers.size();
            graphics.drawString(font, scrollInfo, leftPos + imageWidth / 2 - font.width(scrollInfo) / 2,
                    startY + VISIBLE_ROWS * 22 + 2, DARK_GRAY, false);
        }
    }

    private void renderSecretStashTab(GuiGraphics graphics, int mouseX, int mouseY) {
        List<TraderInventory.TraderOffer> stash = menu.getSecretStash();
        int startY = topPos + 24;

        graphics.drawString(font, "Secret Stash", leftPos + 8, startY - 10, SECRET_COLOR, true);

        for (int i = 0; i < VISIBLE_ROWS && (i + scrollOffset) < stash.size(); i++) {
            int stashIdx = i + scrollOffset;
            TraderInventory.TraderOffer offer = stash.get(stashIdx);
            int globalIdx = menu.getOffers().size() + stashIdx;

            int rowY = startY + i * 22;
            boolean hovered = mouseX >= leftPos + 6 && mouseX <= leftPos + 170 &&
                              mouseY >= rowY && mouseY <= rowY + 20;

            graphics.fill(leftPos + 6, rowY, leftPos + 170, rowY + 20, hovered ? SECRET_ROW_HOVER : SECRET_ROW_BG);

            graphics.renderItem(offer.item(), leftPos + 8, rowY + 2);
            graphics.renderItemDecorations(font, offer.item(), leftPos + 8, rowY + 2);

            String itemName = offer.item().getHoverName().getString();
            if (font.width(itemName) > 90) {
                itemName = font.plainSubstrByWidth(itemName, 87) + "...";
            }
            graphics.drawString(font, itemName, leftPos + 28, rowY + 2, BLACK, false);

            int adjustedPrice = menu.getAdjustedBuyPrice(offer);
            int stock = menu.getStock(globalIdx);
            String priceText = adjustedPrice + " coins";
            int tokenColor = countPlayerTokens() >= adjustedPrice && stock > 0 ? SECRET_COLOR : RED;
            graphics.drawString(font, priceText, leftPos + 28, rowY + 11, tokenColor, false);

            String stockText = stock > 0 ? "x" + stock : "SOLD OUT";
            int stockColor = stock > 0 ? GRAY : RED;
            int stockX = leftPos + 170 - font.width(stockText) - 2;
            graphics.drawString(font, stockText, stockX, rowY + 6, stockColor, false);
        }
    }

    private void renderSellTab(GuiGraphics graphics, int mouseX, int mouseY) {
        int startY = topPos + 24;
        graphics.drawString(font, "Place items to sell:", leftPos + 8, startY, GRAY, false);

        for (int i = 0; i < TraderMenu.SELL_SLOT_COUNT; i++) {
            int sx = leftPos + 7 + i * 18;
            int sy = topPos + 109;
            graphics.fill(sx, sy, sx + 18, sy + 18, SLOT_BG);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_INNER);
        }

        int totalValue = menu.getSellSlotsValue();
        String valueText = "Value: " + totalValue + " coins";
        int valueColor = totalValue > 0 ? SELL_COLOR : DARK_GRAY;
        graphics.drawString(font, valueText, leftPos + 8, topPos + 97, valueColor, false);
    }

    private void renderQuestsTab(GuiGraphics graphics, int mouseX, int mouseY) {
        int clipTop = topPos + 24;
        int clipBottom = topPos + 130;
        int clipHeight = clipBottom - clipTop;

        // Combine active quests and available quests into a scrollable view
        List<QuestEntry> activeQuests = QuestClientState.getActiveQuests();
        List<TraderQuestEntry> traderQuests = QuestClientState.getTraderQuests();
        boolean hasTraderQuests = QuestClientState.getCurrentTraderId() == menu.getTraderId() && !traderQuests.isEmpty();

        // Calculate total content height for scroll bounds
        int contentHeight = 0;
        if (!activeQuests.isEmpty()) {
            contentHeight += 12; // "Active Quests:" header
            contentHeight += activeQuests.size() * 22;
            contentHeight += 4; // gap
        }
        if (hasTraderQuests) {
            contentHeight += 12; // "Available Quests:" header
            contentHeight += traderQuests.size() * 28;
        } else if (traderQuests.isEmpty()) {
            contentHeight += 12; // "No quests available."
        }
        totalQuestContentHeight = contentHeight;

        // Clamp scroll offset
        int maxScroll = Math.max(0, totalQuestContentHeight - clipHeight);
        questScrollOffset = Math.max(0, Math.min(maxScroll, questScrollOffset));

        // Enable scissor to clip the quest area
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        int scissorX = (int)(leftPos * scale);
        int scissorY = (int)((Minecraft.getInstance().getWindow().getGuiScaledHeight() - clipBottom) * scale);
        int scissorW = (int)(imageWidth * scale);
        int scissorH = (int)(clipHeight * scale);
        com.mojang.blaze3d.systems.RenderSystem.enableScissor(scissorX, scissorY, scissorX + scissorW, scissorY + scissorH);

        int startY = clipTop - questScrollOffset;

        // Render active quests section
        if (!activeQuests.isEmpty()) {
            graphics.drawString(font, "Active Quests:", leftPos + 8, startY, QUEST_COLOR, false);
            startY += 12;

            for (int qi = 0; qi < activeQuests.size(); qi++) {
                QuestEntry quest = activeQuests.get(qi);

                boolean readyToTurnIn = "READY_TO_TURN_IN".equals(quest.stateName());
                boolean isThisTrader = quest.traderId() == menu.getTraderId();

                boolean isTracked = quest.questId().equals(QuestClientState.getTrackedQuestId());
                graphics.fill(leftPos + 6, startY, leftPos + 170, startY + 20, isTracked ? QUEST_ROW_TRACKED : QUEST_ROW_BG);

                String name = quest.questName();
                if (font.width(name) > 100) {
                    name = font.plainSubstrByWidth(name, 97) + "...";
                }
                graphics.drawString(font, name, leftPos + 8, startY + 2, QUEST_COLOR, false);

                if (readyToTurnIn && isThisTrader) {
                    String turnIn = "[Turn In]";
                    int turnInX = leftPos + 170 - font.width(turnIn) - 2;
                    boolean hovered = mouseX >= turnInX && mouseX <= leftPos + 170 &&
                                      mouseY >= startY && mouseY <= startY + 20 &&
                                      mouseY >= clipTop && mouseY < clipBottom;
                    graphics.drawString(font, turnIn, turnInX, startY + 2, hovered ? WHITE : READY_COLOR, false);
                }

                String progress = quest.progress() + "/" + quest.targetCount();
                if (readyToTurnIn) progress = "COMPLETE";
                graphics.drawString(font, progress, leftPos + 8, startY + 11, readyToTurnIn ? READY_COLOR : GRAY, false);

                String trackLabel = isTracked ? "[*]" : "[Track]";
                int trackX = leftPos + 80;
                boolean trackHovered = mouseX >= trackX && mouseX <= trackX + font.width(trackLabel) &&
                                       mouseY >= startY && mouseY <= startY + 20 &&
                                       mouseY >= clipTop && mouseY < clipBottom;
                graphics.drawString(font, trackLabel, trackX, startY + 11, trackHovered ? BLACK : 0xFF006666, false);

                String abandon = "[X]";
                int abandonX = leftPos + 170 - font.width(abandon) - 2;
                if (readyToTurnIn && isThisTrader) {
                    abandonX -= font.width("[Turn In]") + 4;
                }
                boolean abandonHovered = mouseX >= abandonX && mouseX <= abandonX + font.width(abandon) &&
                                         mouseY >= startY && mouseY <= startY + 20 &&
                                         mouseY >= clipTop && mouseY < clipBottom;
                graphics.drawString(font, abandon, abandonX, startY + 11, abandonHovered ? BLACK : RED, false);

                startY += 22;
            }

            startY += 4;
        }

        // Render available quests from this trader
        if (hasTraderQuests) {
            graphics.drawString(font, "Available Quests:", leftPos + 8, startY, 0xFF006666, false);
            startY += 12;

            for (int i = 0; i < traderQuests.size(); i++) {
                TraderQuestEntry quest = traderQuests.get(i);

                boolean hovered = mouseX >= leftPos + 6 && mouseX <= leftPos + 170 &&
                                  mouseY >= startY && mouseY <= startY + 26 &&
                                  mouseY >= clipTop && mouseY < clipBottom;

                graphics.fill(leftPos + 6, startY, leftPos + 170, startY + 26, hovered ? QUEST_ROW_HOVER : QUEST_ROW_BG);

                String name = quest.questName();
                if (font.width(name) > 120) {
                    name = font.plainSubstrByWidth(name, 117) + "...";
                }
                graphics.drawString(font, name, leftPos + 8, startY + 2, BLACK, false);

                String desc = quest.objectiveDescription();
                if (font.width(desc) > 130) {
                    desc = font.plainSubstrByWidth(desc, 127) + "...";
                }
                graphics.drawString(font, desc, leftPos + 8, startY + 11, GRAY, false);

                String reward = quest.rewardXp() + "xp " + quest.rewardTokens() + "c";
                int rewardX = leftPos + 170 - font.width(reward) - 2;
                graphics.drawString(font, reward, rewardX, startY + 17, BUY_COLOR, false);

                String accept = "[Accept]";
                graphics.drawString(font, accept, leftPos + 8, startY + 17, hovered ? BLACK : 0xFF006666, false);

                startY += 28;
            }
        } else if (traderQuests.isEmpty()) {
            graphics.drawString(font, "No quests available.", leftPos + 8, startY, DARK_GRAY, false);
        }

        com.mojang.blaze3d.systems.RenderSystem.disableScissor();

        // Draw scroll indicator if content overflows
        if (totalQuestContentHeight > clipHeight) {
            int barHeight = Math.max(8, clipHeight * clipHeight / totalQuestContentHeight);
            int barTravel = clipHeight - barHeight;
            int barY = clipTop + (maxScroll > 0 ? questScrollOffset * barTravel / maxScroll : 0);
            graphics.fill(leftPos + 171, clipTop, leftPos + 174, clipBottom, 0xFF555555);
            graphics.fill(leftPos + 171, barY, leftPos + 174, barY + barHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentTab == Tab.BUY) {
            List<TraderInventory.TraderOffer> offers = menu.getOffers();
            int startY = topPos + 24;
            for (int i = 0; i < VISIBLE_ROWS && (i + scrollOffset) < offers.size(); i++) {
                int offerIdx = i + scrollOffset;
                int rowY = startY + i * 22;
                if (mouseX >= leftPos + 6 && mouseX <= leftPos + 170 &&
                    mouseY >= rowY && mouseY <= rowY + 20) {
                    PacketDistributor.sendToServer(new TraderActionPayload(menu.getTraderId(), offerIdx, true));
                    return true;
                }
            }
        } else if (currentTab == Tab.SECRET_STASH) {
            List<TraderInventory.TraderOffer> stash = menu.getSecretStash();
            int startY = topPos + 24;
            for (int i = 0; i < VISIBLE_ROWS && (i + scrollOffset) < stash.size(); i++) {
                int globalIdx = menu.getOffers().size() + i + scrollOffset;
                int rowY = startY + i * 22;
                if (mouseX >= leftPos + 6 && mouseX <= leftPos + 170 &&
                    mouseY >= rowY && mouseY <= rowY + 20) {
                    PacketDistributor.sendToServer(new TraderActionPayload(menu.getTraderId(), globalIdx, true));
                    return true;
                }
            }
        } else if (currentTab == Tab.QUESTS) {
            if (handleQuestClick(mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleQuestClick(double mouseX, double mouseY) {
        int clipTop = topPos + 24;
        int clipBottom = topPos + 130;

        // Ignore clicks outside the clipped quest area
        if (mouseY < clipTop || mouseY >= clipBottom) return false;

        int startY = clipTop - questScrollOffset;

        List<QuestEntry> activeQuests = QuestClientState.getActiveQuests();
        if (!activeQuests.isEmpty()) {
            startY += 12;

            for (QuestEntry quest : activeQuests) {
                boolean readyToTurnIn = "READY_TO_TURN_IN".equals(quest.stateName());
                boolean isThisTrader = quest.traderId() == menu.getTraderId();

                // Only process clicks for rows visible in the clip area
                if (startY + 20 > clipTop && startY < clipBottom) {
                    if (readyToTurnIn && isThisTrader) {
                        String turnIn = "[Turn In]";
                        int turnInX = leftPos + 170 - font.width(turnIn) - 2;
                        if (mouseX >= turnInX && mouseX <= leftPos + 170 &&
                            mouseY >= startY && mouseY <= startY + 20) {
                            PacketDistributor.sendToServer(new QuestActionPayload(
                                    menu.getTraderId(), quest.questId(), QuestActionPayload.ACTION_TURN_IN));
                            return true;
                        }
                    }

                    String trackLabel = quest.questId().equals(QuestClientState.getTrackedQuestId()) ? "[*]" : "[Track]";
                    int trackX = leftPos + 80;
                    if (mouseX >= trackX && mouseX <= trackX + font.width(trackLabel) &&
                        mouseY >= startY && mouseY <= startY + 20) {
                        PacketDistributor.sendToServer(new QuestActionPayload(
                                menu.getTraderId(), quest.questId(), QuestActionPayload.ACTION_TRACK));
                        return true;
                    }

                    String abandon = "[X]";
                    int abandonX = leftPos + 170 - font.width(abandon) - 2;
                    if (readyToTurnIn && isThisTrader) {
                        abandonX -= font.width("[Turn In]") + 4;
                    }
                    if (mouseX >= abandonX && mouseX <= abandonX + font.width(abandon) &&
                        mouseY >= startY && mouseY <= startY + 20) {
                        PacketDistributor.sendToServer(new QuestActionPayload(
                                menu.getTraderId(), quest.questId(), QuestActionPayload.ACTION_ABANDON));
                        return true;
                    }
                }

                startY += 22;
            }

            startY += 4;
        }

        List<TraderQuestEntry> traderQuests = QuestClientState.getTraderQuests();
        if (QuestClientState.getCurrentTraderId() == menu.getTraderId() && !traderQuests.isEmpty()) {
            startY += 12;

            for (int i = 0; i < traderQuests.size(); i++) {
                TraderQuestEntry quest = traderQuests.get(i);

                if (startY + 26 > clipTop && startY < clipBottom) {
                    if (mouseX >= leftPos + 6 && mouseX <= leftPos + 170 &&
                        mouseY >= startY && mouseY <= startY + 26) {
                        PacketDistributor.sendToServer(new QuestActionPayload(
                                menu.getTraderId(), quest.questId(), QuestActionPayload.ACTION_ACCEPT));
                        return true;
                    }
                }

                startY += 28;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (currentTab == Tab.BUY) {
            int maxScroll = Math.max(0, menu.getOffers().size() - VISIBLE_ROWS);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
            return true;
        } else if (currentTab == Tab.SECRET_STASH) {
            int maxScroll = Math.max(0, menu.getSecretStash().size() - VISIBLE_ROWS);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
            return true;
        } else if (currentTab == Tab.QUESTS) {
            // Scroll entire quest content by pixel offset
            int clipHeight = 130 - 24; // clipBottom - clipTop relative
            int maxScroll = Math.max(0, totalQuestContentHeight - clipHeight);
            int scrollAmount = (int) scrollY * 10; // 10px per scroll tick
            questScrollOffset = Math.max(0, Math.min(maxScroll, questScrollOffset - scrollAmount));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private int countPlayerTokens() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        int total = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.SURVIVORS_COIN.get()) {
                total += stack.getCount();
            }
        }
        return total;
    }

    // ─── Dialogue Bubble (Chat-style with Next button) ──────────────────

    private void advanceDialogue() {
        List<String> dialogue = TraderDialogueClientState.getDialogue();
        if (dialogueIndex < dialogue.size() - 1) {
            dialogueIndex++;
            wrappedCurrentMessage = null; // force re-wrap
        }
    }

    private void renderDialogueBubble(GuiGraphics graphics, int mouseX, int mouseY) {
        List<String> dialogue = TraderDialogueClientState.getDialogue();
        if (dialogue.isEmpty()) return;

        // Clamp index
        if (dialogueIndex >= dialogue.size()) {
            dialogueIndex = dialogue.size() - 1;
        }

        // Get the current message and wrap it
        String currentMessage = dialogue.get(dialogueIndex);
        int maxTextWidth = BUBBLE_WIDTH - BUBBLE_PADDING * 2;
        if (wrappedCurrentMessage == null) {
            wrappedCurrentMessage = wrapLine(currentMessage, maxTextWidth);
        }

        int bubbleX = leftPos - BUBBLE_WIDTH - BUBBLE_GAP;
        if (bubbleX < 2) bubbleX = 2;

        int lineHeight = 11;
        int titleHeight = 16;
        int textLines = wrappedCurrentMessage.size();
        int bubbleContentHeight = titleHeight + textLines * lineHeight + BUBBLE_PADDING * 2;
        // Add space for page indicator / next button
        int footerHeight = 16;
        int bubbleHeight = bubbleContentHeight + footerHeight;
        int bubbleY = topPos;

        // Draw bubble background with border
        graphics.fill(bubbleX - 1, bubbleY - 1,
                bubbleX + BUBBLE_WIDTH + 1, bubbleY + bubbleHeight + 1, BUBBLE_BORDER);
        graphics.fill(bubbleX, bubbleY,
                bubbleX + BUBBLE_WIDTH, bubbleY + bubbleHeight, BUBBLE_BG);

        // Draw small triangle pointer on the right side pointing at the trader menu
        int triY = bubbleY + 20;
        for (int i = 0; i < 5; i++) {
            graphics.fill(bubbleX + BUBBLE_WIDTH + 1, triY + i,
                    bubbleX + BUBBLE_WIDTH + 1 + (5 - Math.abs(i - 2)), triY + i + 1, BUBBLE_BORDER);
        }

        // Title: trader name
        String title = menu.getTraderName();
        graphics.drawString(font, title, bubbleX + BUBBLE_PADDING, bubbleY + BUBBLE_PADDING, BUBBLE_TITLE_COLOR, true);

        // Separator line
        int sepY = bubbleY + BUBBLE_PADDING + 12;
        graphics.fill(bubbleX + BUBBLE_PADDING, sepY,
                bubbleX + BUBBLE_WIDTH - BUBBLE_PADDING, sepY + 1, BUBBLE_BORDER);

        // Draw current dialogue message
        int textStartY = bubbleY + titleHeight + BUBBLE_PADDING;
        for (int i = 0; i < textLines; i++) {
            String line = wrappedCurrentMessage.get(i);
            graphics.drawString(font, line, bubbleX + BUBBLE_PADDING, textStartY + i * lineHeight, BUBBLE_TEXT_COLOR, false);
        }

        // Page indicator at bottom
        if (dialogue.size() > 1) {
            String pageText = (dialogueIndex + 1) + " / " + dialogue.size();
            graphics.drawString(font, pageText, bubbleX + BUBBLE_PADDING,
                    bubbleY + bubbleHeight - footerHeight + 2, DARK_GRAY, false);
        }
    }

    private List<String> wrapLine(String text, int maxWidth) {
        List<String> result = new ArrayList<>();
        String currentColor = "";
        StringBuilder current = new StringBuilder();
        String[] words = text.split(" ");

        for (String word : words) {
            String test = current.length() == 0 ? word : current + " " + word;
            String stripped = test.replaceAll("§.", "");
            if (font.width(stripped) > maxWidth && current.length() > 0) {
                result.add(current.toString());
                current = new StringBuilder(currentColor + word);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(word);
            }
            for (int ci = 0; ci < word.length() - 1; ci++) {
                if (word.charAt(ci) == '§') {
                    currentColor = "§" + word.charAt(ci + 1);
                }
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        if (result.isEmpty()) {
            result.add(text);
        }
        return result;
    }

    @Override
    public void onClose() {
        TraderDialogueClientState.clear();
        super.onClose();
    }
}
