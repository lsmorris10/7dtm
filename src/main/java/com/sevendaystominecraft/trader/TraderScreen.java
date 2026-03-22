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

import java.util.List;

public class TraderScreen extends AbstractContainerScreen<TraderMenu> {

    private static final int BG_COLOR = 0xFF2A2A2A;
    private static final int HEADER_COLOR = 0xFF444444;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int SLOT_INNER = 0xFF373737;
    private static final int BUY_COLOR = 0xFF00AA00;
    private static final int SELL_COLOR = 0xFFAAAA00;
    private static final int CYAN = 0xFF00CCCC;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int DARK_GRAY = 0xFF666666;
    private static final int RED = 0xFFFF4444;
    private static final int QUEST_COLOR = 0xFFFFCC00;
    private static final int READY_COLOR = 0xFF00FFAA;
    private static final int SECRET_COLOR = 0xFFDD44FF;

    private enum Tab { BUY, SELL, QUESTS, SECRET_STASH }
    private Tab currentTab = Tab.BUY;
    private int scrollOffset = 0;
    private int questScrollOffset = 0;
    private static final int VISIBLE_ROWS = 5;
    private static final int VISIBLE_QUEST_ROWS = 4;

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
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BG_COLOR);
        graphics.fill(leftPos + 4, topPos + 2, leftPos + imageWidth - 4, topPos + 22, HEADER_COLOR);

        int tokenCount = countPlayerTokens();
        String tokenText = "Tokens: " + tokenCount;
        graphics.drawString(font, tokenText, leftPos + imageWidth - font.width(tokenText) - 8, topPos + 8, CYAN, true);

        sellButton.visible = currentTab == Tab.SELL;

        switch (currentTab) {
            case BUY -> renderBuyTab(graphics, mouseX, mouseY);
            case SELL -> renderSellTab(graphics, mouseX, mouseY);
            case QUESTS -> renderQuestsTab(graphics, mouseX, mouseY);
            case SECRET_STASH -> renderSecretStashTab(graphics, mouseX, mouseY);
        }

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

            graphics.fill(leftPos + 6, rowY, leftPos + 170, rowY + 20, hovered ? 0xFF3A3A3A : 0xFF333333);

            graphics.renderItem(offer.item(), leftPos + 8, rowY + 2);
            graphics.renderItemDecorations(font, offer.item(), leftPos + 8, rowY + 2);

            String itemName = offer.item().getHoverName().getString();
            if (font.width(itemName) > 90) {
                itemName = font.plainSubstrByWidth(itemName, 87) + "...";
            }
            graphics.drawString(font, itemName, leftPos + 28, rowY + 2, WHITE, false);

            int adjustedPrice = menu.getAdjustedBuyPrice(offer);
            int stock = menu.getStock(offerIdx);
            String priceText = adjustedPrice + " tokens";
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

            graphics.fill(leftPos + 6, rowY, leftPos + 170, rowY + 20, hovered ? 0xFF4A2A4A : 0xFF3A2A3A);

            graphics.renderItem(offer.item(), leftPos + 8, rowY + 2);
            graphics.renderItemDecorations(font, offer.item(), leftPos + 8, rowY + 2);

            String itemName = offer.item().getHoverName().getString();
            if (font.width(itemName) > 90) {
                itemName = font.plainSubstrByWidth(itemName, 87) + "...";
            }
            graphics.drawString(font, itemName, leftPos + 28, rowY + 2, WHITE, false);

            int adjustedPrice = menu.getAdjustedBuyPrice(offer);
            int stock = menu.getStock(globalIdx);
            String priceText = adjustedPrice + " tokens";
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
        String valueText = "Value: " + totalValue + " tokens";
        int valueColor = totalValue > 0 ? SELL_COLOR : DARK_GRAY;
        graphics.drawString(font, valueText, leftPos + 8, topPos + 97, valueColor, false);
    }

    private void renderQuestsTab(GuiGraphics graphics, int mouseX, int mouseY) {
        int startY = topPos + 24;

        List<QuestEntry> activeQuests = QuestClientState.getActiveQuests();
        if (!activeQuests.isEmpty()) {
            graphics.drawString(font, "Active Quests:", leftPos + 8, startY, QUEST_COLOR, false);
            startY += 12;

            for (QuestEntry quest : activeQuests) {
                if (startY > topPos + 120) break;

                boolean readyToTurnIn = "READY_TO_TURN_IN".equals(quest.stateName());
                boolean isThisTrader = quest.traderId() == menu.getTraderId();

                boolean isTracked = quest.questId().equals(QuestClientState.getTrackedQuestId());
                graphics.fill(leftPos + 6, startY, leftPos + 170, startY + 20, isTracked ? 0xFF444422 : 0xFF333333);

                String name = quest.questName();
                if (font.width(name) > 100) {
                    name = font.plainSubstrByWidth(name, 97) + "...";
                }
                graphics.drawString(font, name, leftPos + 8, startY + 2, QUEST_COLOR, false);

                if (readyToTurnIn && isThisTrader) {
                    String turnIn = "[Turn In]";
                    int turnInX = leftPos + 170 - font.width(turnIn) - 2;
                    boolean hovered = mouseX >= turnInX && mouseX <= leftPos + 170 &&
                                      mouseY >= startY && mouseY <= startY + 20;
                    graphics.drawString(font, turnIn, turnInX, startY + 2, hovered ? WHITE : READY_COLOR, false);
                }

                String progress = quest.progress() + "/" + quest.targetCount();
                if (readyToTurnIn) progress = "COMPLETE";
                graphics.drawString(font, progress, leftPos + 8, startY + 11, readyToTurnIn ? READY_COLOR : GRAY, false);

                String trackLabel = isTracked ? "[*]" : "[Track]";
                int trackX = leftPos + 80;
                boolean trackHovered = mouseX >= trackX && mouseX <= trackX + font.width(trackLabel) &&
                                       mouseY >= startY && mouseY <= startY + 20;
                graphics.drawString(font, trackLabel, trackX, startY + 11, trackHovered ? WHITE : CYAN, false);

                String abandon = "[X]";
                int abandonX = leftPos + 170 - font.width(abandon) - 2;
                if (readyToTurnIn && isThisTrader) {
                    abandonX -= font.width("[Turn In]") + 4;
                }
                boolean abandonHovered = mouseX >= abandonX && mouseX <= abandonX + font.width(abandon) &&
                                         mouseY >= startY && mouseY <= startY + 20;
                graphics.drawString(font, abandon, abandonX, startY + 11, abandonHovered ? WHITE : RED, false);

                startY += 22;
            }

            startY += 4;
        }

        List<TraderQuestEntry> traderQuests = QuestClientState.getTraderQuests();
        if (QuestClientState.getCurrentTraderId() == menu.getTraderId() && !traderQuests.isEmpty()) {
            graphics.drawString(font, "Available Quests:", leftPos + 8, startY, CYAN, false);
            startY += 12;

            for (int i = questScrollOffset; i < traderQuests.size() && startY <= topPos + 120; i++) {
                TraderQuestEntry quest = traderQuests.get(i);

                boolean hovered = mouseX >= leftPos + 6 && mouseX <= leftPos + 170 &&
                                  mouseY >= startY && mouseY <= startY + 26;

                graphics.fill(leftPos + 6, startY, leftPos + 170, startY + 26, hovered ? 0xFF3A3A3A : 0xFF333333);

                String name = quest.questName();
                if (font.width(name) > 120) {
                    name = font.plainSubstrByWidth(name, 117) + "...";
                }
                graphics.drawString(font, name, leftPos + 8, startY + 2, WHITE, false);

                String desc = quest.objectiveDescription();
                if (font.width(desc) > 130) {
                    desc = font.plainSubstrByWidth(desc, 127) + "...";
                }
                graphics.drawString(font, desc, leftPos + 8, startY + 11, GRAY, false);

                String reward = quest.rewardXp() + "xp " + quest.rewardTokens() + "t";
                int rewardX = leftPos + 170 - font.width(reward) - 2;
                graphics.drawString(font, reward, rewardX, startY + 17, BUY_COLOR, false);

                String accept = "[Accept]";
                graphics.drawString(font, accept, leftPos + 8, startY + 17, hovered ? WHITE : CYAN, false);

                startY += 28;
            }
        } else if (traderQuests.isEmpty()) {
            graphics.drawString(font, "No quests available.", leftPos + 8, startY, DARK_GRAY, false);
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
            return handleQuestClick(mouseX, mouseY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleQuestClick(double mouseX, double mouseY) {
        int startY = topPos + 24;

        List<QuestEntry> activeQuests = QuestClientState.getActiveQuests();
        if (!activeQuests.isEmpty()) {
            startY += 12;

            for (QuestEntry quest : activeQuests) {
                if (startY > topPos + 120) break;

                boolean readyToTurnIn = "READY_TO_TURN_IN".equals(quest.stateName());
                boolean isThisTrader = quest.traderId() == menu.getTraderId();

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

                startY += 22;
            }

            startY += 4;
        }

        List<TraderQuestEntry> traderQuests = QuestClientState.getTraderQuests();
        if (QuestClientState.getCurrentTraderId() == menu.getTraderId() && !traderQuests.isEmpty()) {
            startY += 12;

            for (int i = questScrollOffset; i < traderQuests.size() && startY <= topPos + 120; i++) {
                TraderQuestEntry quest = traderQuests.get(i);

                if (mouseX >= leftPos + 6 && mouseX <= leftPos + 170 &&
                    mouseY >= startY && mouseY <= startY + 26) {
                    PacketDistributor.sendToServer(new QuestActionPayload(
                            menu.getTraderId(), quest.questId(), QuestActionPayload.ACTION_ACCEPT));
                    return true;
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
            List<TraderQuestEntry> traderQuests = QuestClientState.getTraderQuests();
            int maxScroll = Math.max(0, traderQuests.size() - VISIBLE_QUEST_ROWS);
            questScrollOffset = Math.max(0, Math.min(maxScroll, questScrollOffset - (int) scrollY));
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
            if (!stack.isEmpty() && stack.getItem() == ModItems.DUKE_TOKEN.get()) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
