package com.sevendaystominecraft.client;

import java.util.HashMap;
import java.util.Map;

import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.capability.ModAttachments;
import com.sevendaystominecraft.capability.SevenDaysPlayerStats;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class DebuffOverlayRenderer {

    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(SevenDaysToMinecraft.MOD_ID, "debuff_overlay");

    private static final float FADE_SPEED_PER_TICK = 1.0f;
    private static final float MAX_SINGLE_ALPHA = 0.75f;
    private static final float MAX_GLOBAL_ALPHA = 0.65f;

    private static final int BLEED_TICK_INTERVAL = 20;
    private static final int INFECTION1_GROWTH_TICKS = 600;

    private static final Map<String, Float> fadeAlphas = new HashMap<>();
    private static final Map<String, Long> debuffStartTicks = new HashMap<>();

    private static boolean wasSprinting = false;
    private static boolean wasSprintKeyDown = false;
    private static float sprintFlashAlpha = 0f;
    private static long lastTickCount = 0;
    private static float alphaBudgetUsed = 0f;
    private static float bleedPulseAlpha = 0f;

    public static void reset() {
        fadeAlphas.clear();
        debuffStartTicks.clear();
        wasSprinting = false;
        wasSprintKeyDown = false;
        sprintFlashAlpha = 0f;
        lastTickCount = 0;
        alphaBudgetUsed = 0f;
        bleedPulseAlpha = 0f;
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, DebuffOverlayRenderer::render);
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.hideGui) return;
        if (!player.hasData(ModAttachments.PLAYER_STATS.get())) return;

        SevenDaysPlayerStats stats = player.getData(ModAttachments.PLAYER_STATS.get());
        Map<String, Integer> debuffs = stats.getDebuffs();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        long currentTick = player.tickCount;

        long tickDelta = currentTick - lastTickCount;

        updateFade(SevenDaysPlayerStats.DEBUFF_BLEEDING, debuffs, currentTick, tickDelta);
        updateFade(SevenDaysPlayerStats.DEBUFF_INFECTION_1, debuffs, currentTick, tickDelta);
        updateFade(SevenDaysPlayerStats.DEBUFF_INFECTION_2, debuffs, currentTick, tickDelta);
        updateFade(SevenDaysPlayerStats.DEBUFF_DYSENTERY, debuffs, currentTick, tickDelta);
        updateFade(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA, debuffs, currentTick, tickDelta);
        updateFade(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA, debuffs, currentTick, tickDelta);
        updateFade(SevenDaysPlayerStats.DEBUFF_CONCUSSION, debuffs, currentTick, tickDelta);
        updateFade(SevenDaysPlayerStats.DEBUFF_SPRAIN, debuffs, currentTick, tickDelta);
        updateFade(SevenDaysPlayerStats.DEBUFF_FRACTURE, debuffs, currentTick, tickDelta);

        boolean hasFractureOrSprain = debuffs.containsKey(SevenDaysPlayerStats.DEBUFF_FRACTURE)
                || debuffs.containsKey(SevenDaysPlayerStats.DEBUFF_SPRAIN);
        boolean isSprinting = player.isSprinting();
        boolean isJumping = !player.onGround() && player.getDeltaMovement().y > 0.1;
        boolean sprintKeyDown = mc.options.keySprint.isDown();

        if (hasFractureOrSprain) {
            boolean sprintAttempt = (isSprinting && !wasSprinting)
                    || (sprintKeyDown && !wasSprintKeyDown)
                    || (isJumping && !wasSprinting);
            if (sprintAttempt) {
                sprintFlashAlpha = 0.4f;
            }
        }
        wasSprinting = isSprinting || isJumping;
        wasSprintKeyDown = sprintKeyDown;

        if (currentTick != lastTickCount) {
            sprintFlashAlpha = Math.max(0f, sprintFlashAlpha - 0.04f);

            if (debuffs.containsKey(SevenDaysPlayerStats.DEBUFF_BLEEDING)) {
                if (currentTick % BLEED_TICK_INTERVAL == 0) {
                    bleedPulseAlpha = 1.0f;
                }
                bleedPulseAlpha = Math.max(0f, bleedPulseAlpha - 0.05f);
            } else {
                bleedPulseAlpha = 0f;
            }

            lastTickCount = currentTick;
        }

        alphaBudgetUsed = 0f;

        float bleedAlpha = getFadeAlpha(SevenDaysPlayerStats.DEBUFF_BLEEDING);
        if (bleedAlpha > 0) {
            float baseIntensity = 0.3f;
            float tickPulse = bleedPulseAlpha * 0.5f;
            float intensity = budgetAlpha(bleedAlpha * (baseIntensity + tickPulse));
            renderVignette(graphics, screenWidth, screenHeight, 0xCC0000, intensity);
        }

        float inf1Alpha = getFadeAlpha(SevenDaysPlayerStats.DEBUFF_INFECTION_1);
        if (inf1Alpha > 0) {
            long inf1Start = debuffStartTicks.getOrDefault(SevenDaysPlayerStats.DEBUFF_INFECTION_1, currentTick);
            float elapsed = Math.min(1.0f, (float)(currentTick - inf1Start) / INFECTION1_GROWTH_TICKS);
            float progression = 0.15f + elapsed * 0.25f;
            float intensity = budgetAlpha(inf1Alpha * progression);
            renderVignette(graphics, screenWidth, screenHeight, 0x33AA33, intensity);
        }

        float inf2Alpha = getFadeAlpha(SevenDaysPlayerStats.DEBUFF_INFECTION_2);
        if (inf2Alpha > 0) {
            float flicker = 1.0f;
            boolean distortionWindow = currentTick % 80 < 4;
            if (distortionWindow) {
                flicker = 0.3f;
                float distortX = (float) Math.sin(currentTick * 0.5) * 2.0f * inf2Alpha;
                float distortY = (float) Math.cos(currentTick * 0.3) * 1.5f * inf2Alpha;
                graphics.pose().pushPose();
                graphics.pose().translate(distortX, distortY, 0);
            }
            float intensity = budgetAlpha(inf2Alpha * 0.5f * flicker);
            renderVignette(graphics, screenWidth, screenHeight, 0x22CC22, intensity);
            if (distortionWindow) {
                graphics.pose().popPose();
            }
        }

        float dysenteryAlpha = getFadeAlpha(SevenDaysPlayerStats.DEBUFF_DYSENTERY);
        if (dysenteryAlpha > 0) {
            float wobblePhase = (currentTick % 100) / 100.0f;
            boolean inWobbleWindow = wobblePhase < 0.3f;
            if (inWobbleWindow) {
                float wobbleT = wobblePhase / 0.3f;
                float wobbleX = (float) Math.sin(wobbleT * Math.PI * 4) * 3.0f * dysenteryAlpha;
                float wobbleY = (float) Math.cos(wobbleT * Math.PI * 3) * 2.0f * dysenteryAlpha;
                graphics.pose().pushPose();
                graphics.pose().translate(wobbleX, wobbleY, 0);
            }
            float intensity = budgetAlpha(dysenteryAlpha * 0.2f);
            renderVignette(graphics, screenWidth, screenHeight, 0x998833, intensity);
            if (inWobbleWindow) {
                graphics.pose().popPose();
            }
        }

        float hypoAlpha = getFadeAlpha(SevenDaysPlayerStats.DEBUFF_HYPOTHERMIA);
        if (hypoAlpha > 0) {
            float shakeX = (float) (Math.sin(currentTick * 0.7) * 0.8 * hypoAlpha);
            float shakeY = (float) (Math.cos(currentTick * 0.9) * 0.5 * hypoAlpha);
            graphics.pose().pushPose();
            graphics.pose().translate(shakeX, shakeY, 0);
            float intensity = budgetAlpha(hypoAlpha * 0.45f);
            renderVignette(graphics, screenWidth, screenHeight, 0x88CCFF, intensity);
            graphics.pose().popPose();
        }

        float hyperAlpha = getFadeAlpha(SevenDaysPlayerStats.DEBUFF_HYPERTHERMIA);
        if (hyperAlpha > 0) {
            float shimmer = 0.7f + 0.3f * (float) Math.sin(currentTick * 0.08);
            float intensity = budgetAlpha(hyperAlpha * 0.4f * shimmer);
            renderVignette(graphics, screenWidth, screenHeight, 0xFF6633, intensity);
        }

        float concAlpha = getFadeAlpha(SevenDaysPlayerStats.DEBUFF_CONCUSSION);
        if (concAlpha > 0) {
            float concBudgeted = budgetAlpha(concAlpha * 0.15f);
            if (concBudgeted > 0) {
                renderDoubleVision(graphics, screenWidth, screenHeight, currentTick, concBudgeted / 0.15f);
            }
        }

        if (sprintFlashAlpha > 0) {
            float flashBudgeted = budgetAlpha(sprintFlashAlpha * 0.4f);
            if (flashBudgeted > 0) {
                int alpha = (int) ((flashBudgeted / 0.4f) * 255);
                int color = (alpha << 24) | 0xFF2222;
                graphics.fill(0, 0, screenWidth, screenHeight, color);
            }
        }
    }

    private static float budgetAlpha(float requested) {
        float remaining = MAX_GLOBAL_ALPHA - alphaBudgetUsed;
        if (remaining <= 0) return 0f;
        float used = Math.min(requested, remaining);
        alphaBudgetUsed += used;
        return used;
    }

    private static void updateFade(String debuffId, Map<String, Integer> activeDebuffs, long currentTick, long tickDelta) {
        if (tickDelta <= 0) return;
        float current = fadeAlphas.getOrDefault(debuffId, 0f);
        float step = FADE_SPEED_PER_TICK * tickDelta / 20.0f;
        if (activeDebuffs.containsKey(debuffId)) {
            if (!debuffStartTicks.containsKey(debuffId)) {
                debuffStartTicks.put(debuffId, currentTick);
            }
            current = Math.min(1.0f, current + step);
        } else {
            debuffStartTicks.remove(debuffId);
            current = Math.max(0f, current - step);
        }
        if (current > 0f) {
            fadeAlphas.put(debuffId, current);
        } else {
            fadeAlphas.remove(debuffId);
        }
    }

    private static float getFadeAlpha(String debuffId) {
        return Math.min(MAX_SINGLE_ALPHA, fadeAlphas.getOrDefault(debuffId, 0f));
    }

    private static void renderDoubleVision(GuiGraphics graphics, int width, int height, long tick, float alpha) {
        int offsetX = 2 + (int) (Math.sin(tick * 0.07) * 1.5);
        int offsetY = 1 + (int) (Math.cos(tick * 0.05) * 1.0);

        int a = Math.min(255, (int) (alpha * 40));
        if (a <= 0) return;

        int color1 = (a << 24) | 0xFFDDDD;
        graphics.fill(offsetX, offsetY, width + offsetX, height + offsetY, color1);

        int color2 = (a << 24) | 0xDDDDFF;
        graphics.fill(-offsetX, -offsetY, width - offsetX, height - offsetY, color2);
    }

    private static void renderVignette(GuiGraphics graphics, int width, int height, int rgb, float intensity) {
        if (intensity <= 0) return;
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int edgeSize = Math.min(width, height) / 4;

        for (int i = 0; i < edgeSize; i++) {
            float t = 1.0f - ((float) i / edgeSize);
            float alphaVal = intensity * t * t;
            int a = Math.min(255, (int) (alphaVal * 255));
            if (a <= 0) continue;
            int color = (a << 24) | (r << 16) | (g << 8) | b;

            graphics.fill(0, i, edgeSize - i, i + 1, color);
            graphics.fill(width - edgeSize + i, i, width, i + 1, color);

            graphics.fill(0, height - i - 1, edgeSize - i, height - i, color);
            graphics.fill(width - edgeSize + i, height - i - 1, width, height - i, color);

            graphics.fill(0, i, i + 1, height - i, color);
            graphics.fill(width - i - 1, i, width - i, height - i, color);
        }
    }
}
