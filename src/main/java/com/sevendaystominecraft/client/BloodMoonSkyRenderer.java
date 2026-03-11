package com.sevendaystominecraft.client;

import com.sevendaystominecraft.SevenDaysToMinecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = SevenDaysToMinecraft.MOD_ID, value = Dist.CLIENT)
public class BloodMoonSkyRenderer {

    private static final float TARGET_RED = 0.6f;
    private static final float TARGET_GREEN = 0.05f;
    private static final float TARGET_BLUE = 0.05f;

    private static float currentIntensity = 0f;
    private static final float RAMP_SPEED = 0.002f;

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (!BloodMoonClientState.isActive()) {
            if (currentIntensity > 0f) {
                currentIntensity = Math.max(0f, currentIntensity - RAMP_SPEED * 2f);
                applyRedTint(event);
            }
            return;
        }

        if (currentIntensity < 1f) {
            currentIntensity = Math.min(1f, currentIntensity + RAMP_SPEED);
        }

        applyRedTint(event);
    }

    private static void applyRedTint(ViewportEvent.ComputeFogColor event) {
        if (currentIntensity <= 0f) return;

        float originalRed = event.getRed();
        float originalGreen = event.getGreen();
        float originalBlue = event.getBlue();

        float newRed = lerp(originalRed, TARGET_RED, currentIntensity);
        float newGreen = lerp(originalGreen, TARGET_GREEN, currentIntensity);
        float newBlue = lerp(originalBlue, TARGET_BLUE, currentIntensity);

        event.setRed(newRed);
        event.setGreen(newGreen);
        event.setBlue(newBlue);
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}
