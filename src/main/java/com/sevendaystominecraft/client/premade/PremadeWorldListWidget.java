package com.sevendaystominecraft.client.premade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PremadeWorldListWidget extends ObjectSelectionList<PremadeWorldListWidget.Entry> {

    public PremadeWorldListWidget(Minecraft mc, int width, int height, int y, int itemHeight) {
        super(mc, width, height, y, itemHeight);
    }

    public void updateEntries(List<PremadeWorldInfo> worlds) {
        clearEntries();
        for (PremadeWorldInfo world : worlds) {
            addEntry(new Entry(world));
        }
    }

    public PremadeWorldInfo getSelectedWorld() {
        Entry entry = getSelected();
        return entry != null ? entry.worldInfo : null;
    }

    @Override
    public int getRowWidth() {
        return this.width - 40;
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        final PremadeWorldInfo worldInfo;
        private final Component nameComponent;
        private final Component descComponent;

        public Entry(PremadeWorldInfo worldInfo) {
            this.worldInfo = worldInfo;
            this.nameComponent = Component.literal(worldInfo.name());
            String desc = worldInfo.description();
            if (desc.isEmpty()) {
                desc = worldInfo.source() == PremadeWorldInfo.PremadeWorldSource.BUNDLED
                        ? "Bundled premade world"
                        : "External premade world";
            }
            this.descComponent = Component.literal(desc);
        }

        @Override
        public Component getNarration() {
            return nameComponent;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left,
                           int width, int height, int mouseX, int mouseY,
                           boolean hovered, float partialTick) {
            guiGraphics.drawString(
                    PremadeWorldListWidget.this.minecraft.font,
                    nameComponent,
                    left + 5, top + 2, 0xFFFFFF
            );
            guiGraphics.drawString(
                    PremadeWorldListWidget.this.minecraft.font,
                    descComponent,
                    left + 5, top + 14, 0xAAAAAA
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            PremadeWorldListWidget.this.setSelected(this);
            return true;
        }
    }
}
