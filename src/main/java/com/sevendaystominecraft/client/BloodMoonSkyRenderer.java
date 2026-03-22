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

    private static final float FOG_DENSITY_SCALE = 0.35f;

    private static final float SHAKE_MAX_DEGREES = 0.3f;
    private static final float SHAKE_PITCH_FREQ = 3.7f;
    private static final float SHAKE_YAW_FREQ = 2.9f;

    private static final float INTENSITY_RAMP_UP_PER_SEC = 0.033f;
    private static final float INTENSITY_RAMP_DOWN_PER_SEC = 0.066f;
    private static final float SHAKE_RAMP_UP_PER_SEC = 0.5f;
    private static final float SHAKE_RAMP_DOWN_PER_SEC = 1.0f;

    private static float currentIntensity = 0f;
    private static float shakeIntensity = 0f;
    private static long lastFogTimeNanos = 0;
    private static long lastShakeTimeNanos = 0;

    public static void resetIntensity() {
        currentIntensity = 0f;
        shakeIntensity = 0f;
        lastFogTimeNanos = 0;
        lastShakeTimeNanos = 0;
    }

    private static float getFogDeltaSeconds() {
        long now = System.nanoTime();
        if (lastFogTimeNanos == 0) {
            lastFogTimeNanos = now;
            return 0f;
        }
        float delta = (now - lastFogTimeNanos) / 1_000_000_000f;
        lastFogTimeNanos = now;
        return Math.min(delta, 0.1f);
    }

    private static float getShakeDeltaSeconds() {
        long now = System.nanoTime();
        if (lastShakeTimeNanos == 0) {
            lastShakeTimeNanos = now;
            return 0f;
        }
        float delta = (now - lastShakeTimeNanos) / 1_000_000_000f;
        lastShakeTimeNanos = now;
        return Math.min(delta, 0.1f);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        float dt = getFogDeltaSeconds();

        if (!BloodMoonClientState.isActive()) {
            if (currentIntensity > 0f) {
                currentIntensity = Math.max(0f, currentIntensity - INTENSITY_RAMP_DOWN_PER_SEC * dt);
                applyRedTint(event);
            }
            return;
        }

        if (currentIntensity < 1f) {
            currentIntensity = Math.min(1f, currentIntensity + INTENSITY_RAMP_UP_PER_SEC * dt);
        }

        applyRedTint(event);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (currentIntensity <= 0f) return;

        float fogScale = 1.0f - (1.0f - FOG_DENSITY_SCALE) * currentIntensity;
        float originalFar = event.getFarPlaneDistance();
        float originalNear = event.getNearPlaneDistance();

        event.setFarPlaneDistance(originalFar * fogScale);
        event.setNearPlaneDistance(originalNear * fogScale);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        boolean hordeActive = BloodMoonClientState.isActive() && BloodMoonClientState.getCurrentWave() > 0;

        float dt = getShakeDeltaSeconds();

        if (hordeActive) {
            shakeIntensity = Math.min(1f, shakeIntensity + SHAKE_RAMP_UP_PER_SEC * dt);
        } else {
            shakeIntensity = Math.max(0f, shakeIntensity - SHAKE_RAMP_DOWN_PER_SEC * dt);
        }

        if (shakeIntensity <= 0f) return;

        float time = (System.nanoTime() / 1_000_000_000f);

        float pitchOffset = (float) Math.sin(time * SHAKE_PITCH_FREQ) * SHAKE_MAX_DEGREES * shakeIntensity;
        float yawOffset = (float) Math.sin(time * SHAKE_YAW_FREQ) * SHAKE_MAX_DEGREES * shakeIntensity;

        event.setPitch(event.getPitch() + pitchOffset);
        event.setYaw(event.getYaw() + yawOffset);
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
