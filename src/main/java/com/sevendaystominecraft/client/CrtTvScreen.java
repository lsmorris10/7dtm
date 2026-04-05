package com.sevendaystominecraft.client;

import com.sevendaystominecraft.block.CrtTvBlockEntity;
import com.sevendaystominecraft.network.CrtTvActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class CrtTvScreen extends Screen {

    private final BlockPos blockPos;
    private final CrtTvBlockEntity blockEntity;

    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 2;
    private static final int HEADER_HEIGHT = 30;

    public CrtTvScreen(BlockPos pos, CrtTvBlockEntity be) {
        super(Component.translatable("screen.sevendaystominecraft.crt_tv.title"));
        this.blockPos = pos;
        this.blockEntity = be;
    }

    @Override
    protected void init() {
        super.init();

        TvChannelManager.rescan();
        List<String> channels = TvChannelManager.getChannelNames();

        int leftX = (this.width - PANEL_WIDTH) / 2;
        int topY = (this.height - PANEL_HEIGHT) / 2;

        int buttonY = topY + HEADER_HEIGHT;

        if (channels.isEmpty()) {
            // No channels found - just show a label, no buttons
            return;
        }

        for (String channelName : channels) {
            if (buttonY + BUTTON_HEIGHT > topY + PANEL_HEIGHT - BUTTON_HEIGHT - BUTTON_SPACING) break;

            addRenderableWidget(Button.builder(Component.literal(channelName), btn -> {
                PacketDistributor.sendToServer(
                        new CrtTvActionPayload(blockPos.getX(), blockPos.getY(), blockPos.getZ(), channelName));
                this.onClose();
            }).bounds(leftX + 10, buttonY, PANEL_WIDTH - 20, BUTTON_HEIGHT).build());

            buttonY += BUTTON_HEIGHT + BUTTON_SPACING;
        }

        // Power Off button at the bottom
        addRenderableWidget(Button.builder(
                Component.translatable("screen.sevendaystominecraft.crt_tv.power_off"), btn -> {
                    PacketDistributor.sendToServer(
                            new CrtTvActionPayload(blockPos.getX(), blockPos.getY(), blockPos.getZ(), "__off__"));
                    this.onClose();
                }).bounds(leftX + 10, topY + PANEL_HEIGHT - BUTTON_HEIGHT - 5, PANEL_WIDTH - 20, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int leftX = (this.width - PANEL_WIDTH) / 2;
        int topY = (this.height - PANEL_HEIGHT) / 2;

        // Dark panel background
        guiGraphics.fill(leftX, topY, leftX + PANEL_WIDTH, topY + PANEL_HEIGHT, 0xCC1a1a2e);
        guiGraphics.fill(leftX + 1, topY + 1, leftX + PANEL_WIDTH - 1, topY + PANEL_HEIGHT - 1, 0xCC16213e);

        // Title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, topY + 10, 0xFF00FF88);

        // "No channels" message
        List<String> channels = TvChannelManager.getChannelNames();
        if (channels.isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("screen.sevendaystominecraft.crt_tv.no_channels"),
                    this.width / 2, topY + PANEL_HEIGHT / 2 - 5, 0xFFAAAAAA);
            guiGraphics.drawCenteredString(this.font,
                    Component.literal("config/sevendaystominecraft/tv_videos/"),
                    this.width / 2, topY + PANEL_HEIGHT / 2 + 10, 0xFF666666);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
