package com.sevendaystominecraft.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class FadeableMusicSound extends AbstractTickableSoundInstance {

    private static final int FADE_TICKS = 40;

    private boolean fadingOut = false;
    private int fadeOutTick = 0;
    private float startVolume = 1.0f;

    public FadeableMusicSound(SoundEvent soundEvent) {
        super(soundEvent, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (isStopped()) {
            return;
        }

        if (fadingOut) {
            fadeOutTick++;
            float progress = (float) fadeOutTick / FADE_TICKS;
            this.volume = startVolume * (1.0f - Math.min(progress, 1.0f));
            if (fadeOutTick >= FADE_TICKS) {
                stop();
            }
        }
    }

    public void startFadeOut() {
        this.fadingOut = true;
        this.fadeOutTick = 0;
        this.startVolume = this.volume;
    }

    public boolean isDoneFading() {
        return fadingOut && fadeOutTick >= FADE_TICKS;
    }

    public boolean isFadingOut() {
        return fadingOut;
    }
}
