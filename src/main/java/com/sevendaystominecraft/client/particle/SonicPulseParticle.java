package com.sevendaystominecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class SonicPulseParticle extends TextureSheetParticle {

    protected SonicPulseParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 15;
        this.rCol = 0.88f;
        this.gCol = 0.25f;
        this.bCol = 0.98f;
        this.alpha = 0.7f;
        this.quadSize = 0.3f;
        this.xd = xSpeed;
        this.yd = 0;
        this.zd = zSpeed;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
        float progress = (float) this.age / (float) this.lifetime;
        this.quadSize = 0.3f + progress * 2.0f;
        this.alpha = Math.max(0, 0.7f * (1.0f - progress));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            SonicPulseParticle particle = new SonicPulseParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
