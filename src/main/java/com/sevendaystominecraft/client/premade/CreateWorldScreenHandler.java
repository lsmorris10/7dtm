package com.sevendaystominecraft.client.premade;

import net.minecraft.client.gui.components.EditBox;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateWorldScreenHandler {

    private static boolean premadeMode = false;
    private static PremadeWorldListWidget worldListWidget = null;
    private static EditBox worldNameBox = null;

    public static void linkScreen(PremadeWorldListWidget list, EditBox nameBox) {
        worldListWidget = list;
        worldNameBox = nameBox;
    }

    public static void setPremadeMode(boolean mode) {
        premadeMode = mode;
    }

    public static boolean isPremadeMode() {
        return premadeMode;
    }

    public static PremadeWorldInfo getSelectedPremadeWorld() {
        if (worldListWidget != null) {
            return worldListWidget.getSelectedWorld();
        }
        return null;
    }

    public static String getWorldName() {
        if (worldNameBox != null) {
            return worldNameBox.getValue();
        }
        return null;
    }

    public static void reset() {
        premadeMode = false;
        worldListWidget = null;
        worldNameBox = null;
    }
}
