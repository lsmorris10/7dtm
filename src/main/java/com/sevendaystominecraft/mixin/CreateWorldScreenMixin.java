package com.sevendaystominecraft.mixin;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.client.premade.CreateWorldScreenHandler;
import com.sevendaystominecraft.client.premade.PremadeWorldInfo;
import com.sevendaystominecraft.client.premade.PremadeWorldListWidget;
import com.sevendaystominecraft.client.premade.PremadeWorldManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private boolean sevendaystominecraft$premadeMode = false;

    @Unique
    private PremadeWorldListWidget sevendaystominecraft$worldListWidget = null;

    @Unique
    private StringWidget sevendaystominecraft$emptyLabel = null;

    @Unique
    private Button sevendaystominecraft$toggleButton = null;

    @Unique
    private EditBox sevendaystominecraft$worldNameBox = null;

    @Unique
    private final List<AbstractWidget> sevendaystominecraft$vanillaWidgets = new ArrayList<>();

    @Inject(method = "init", at = @At("TAIL"))
    private void sevendaystominecraft$onInit(CallbackInfo ci) {
        sevendaystominecraft$premadeMode = false;
        sevendaystominecraft$vanillaWidgets.clear();
        PremadeWorldManager.invalidateCache();

        for (GuiEventListener child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                sevendaystominecraft$vanillaWidgets.add(widget);
            }
        }

        int screenWidth = this.width;
        int buttonWidth = 150;
        int buttonX = screenWidth / 2 - buttonWidth / 2;
        int buttonY = 33;

        sevendaystominecraft$toggleButton = this.addRenderableWidget(Button.builder(
                Component.literal("World Type: Generated"),
                btn -> {
                    sevendaystominecraft$premadeMode = !sevendaystominecraft$premadeMode;
                    btn.setMessage(Component.literal(
                            sevendaystominecraft$premadeMode ? "World Type: Premade" : "World Type: Generated"));
                    sevendaystominecraft$updateVisibility();
                }
        ).bounds(buttonX, buttonY, buttonWidth, 20).build());

        List<PremadeWorldInfo> worlds = PremadeWorldManager.getAvailableWorlds();

        int listY = 80;
        int listHeight = this.height - 100;
        sevendaystominecraft$worldListWidget = this.addRenderableWidget(
                new PremadeWorldListWidget(Minecraft.getInstance(), screenWidth, listHeight, listY, 30)
        );
        sevendaystominecraft$worldListWidget.updateEntries(worlds);
        sevendaystominecraft$worldListWidget.visible = false;
        sevendaystominecraft$worldListWidget.active = false;

        sevendaystominecraft$emptyLabel = this.addRenderableWidget(new StringWidget(
                screenWidth / 2 - 100, this.height / 2 - 5, 200, 10,
                Component.literal("No premade worlds found"),
                Minecraft.getInstance().font
        ));
        sevendaystominecraft$emptyLabel.visible = false;

        sevendaystominecraft$worldNameBox = this.addRenderableWidget(new EditBox(
                Minecraft.getInstance().font,
                screenWidth / 2 - 100, 58, 200, 18,
                Component.literal("World Name")
        ));
        sevendaystominecraft$worldNameBox.setMaxLength(64);
        sevendaystominecraft$worldNameBox.setValue("New Premade World");
        sevendaystominecraft$worldNameBox.visible = false;

        CreateWorldScreenHandler.linkScreen(
                sevendaystominecraft$worldListWidget,
                sevendaystominecraft$worldNameBox
        );

        SevenDaysToMinecraft.LOGGER.info("BZHS: Injected world type selector ({} premade worlds found)", worlds.size());
    }

    @Unique
    private void sevendaystominecraft$updateVisibility() {
        List<PremadeWorldInfo> worlds = PremadeWorldManager.getAvailableWorlds();
        boolean showList = sevendaystominecraft$premadeMode && !worlds.isEmpty();
        boolean showEmpty = sevendaystominecraft$premadeMode && worlds.isEmpty();

        sevendaystominecraft$worldListWidget.visible = showList;
        sevendaystominecraft$worldListWidget.active = showList;
        sevendaystominecraft$emptyLabel.visible = showEmpty;
        sevendaystominecraft$worldNameBox.visible = sevendaystominecraft$premadeMode;

        for (AbstractWidget widget : sevendaystominecraft$vanillaWidgets) {
            if (!sevendaystominecraft$isBottomButton(widget)) {
                widget.visible = !sevendaystominecraft$premadeMode;
                widget.active = !sevendaystominecraft$premadeMode;
            }
        }

        CreateWorldScreenHandler.setPremadeMode(sevendaystominecraft$premadeMode);
    }

    @Unique
    private boolean sevendaystominecraft$isBottomButton(AbstractWidget widget) {
        if (widget instanceof Button) {
            return widget.getY() + widget.getHeight() >= this.height - 30;
        }
        return false;
    }

    @Inject(method = "onCreate", at = @At("HEAD"), cancellable = true)
    private void sevendaystominecraft$onCreateWorld(CallbackInfo ci) {
        if (!sevendaystominecraft$premadeMode) {
            return;
        }

        ci.cancel();

        PremadeWorldInfo selectedWorld = CreateWorldScreenHandler.getSelectedPremadeWorld();
        if (selectedWorld == null) {
            SevenDaysToMinecraft.LOGGER.warn("BZHS: No premade world selected");
            if (sevendaystominecraft$emptyLabel != null) {
                sevendaystominecraft$emptyLabel.setMessage(
                        Component.literal("Please select a premade world first!"));
                sevendaystominecraft$emptyLabel.visible = true;
            }
            return;
        }

        String worldName = CreateWorldScreenHandler.getWorldName();
        if (worldName == null || worldName.isBlank()) {
            worldName = selectedWorld.name();
        }

        String safeName = worldName.replaceAll("[^a-zA-Z0-9_ \\-]", "").trim();
        if (safeName.isEmpty()) {
            safeName = selectedWorld.name();
        }

        String finalName = safeName;
        java.nio.file.Path savesDir = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("saves").resolve(finalName);
        int suffix = 1;
        while (java.nio.file.Files.exists(savesDir)) {
            finalName = safeName + " (" + suffix + ")";
            savesDir = Minecraft.getInstance().gameDirectory.toPath()
                    .resolve("saves").resolve(finalName);
            suffix++;
        }

        SevenDaysToMinecraft.LOGGER.info("BZHS: Creating premade world '{}' from '{}'",
                finalName, selectedWorld.id());

        boolean success = PremadeWorldManager.copyPremadeWorld(selectedWorld, finalName);
        if (success) {
            this.onClose();
            String openName = finalName;
            Minecraft mc = Minecraft.getInstance();
            mc.createWorldOpenFlows().openWorld(openName, () -> mc.setScreen(null));
        } else {
            SevenDaysToMinecraft.LOGGER.error("BZHS: Failed to copy premade world '{}'", finalName);
            if (sevendaystominecraft$emptyLabel != null) {
                sevendaystominecraft$emptyLabel.setMessage(
                        Component.literal("Failed to create world — check logs"));
                sevendaystominecraft$emptyLabel.visible = true;
            }
        }
    }
}
