package com.sevendaystominecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class BloodDripParticle extends TextureSheetParticle {

    protected BloodDripParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 10 + this.random.nextInt(10);
        this.rCol = 0.83f;
        this.gCol = 0.0f;
        this.bCol = 0.0f;
        this.alpha = 0.8f;
        this.quadSize = 0.08f + this.random.nextFloat() * 0.04f;
        this.xd = xSpeed;
        this.yd = -0.04f;
        this.zd = zSpeed;
        this.gravity = 0.8f;
        this.hasPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > this.lifetime / 2) {
            this.alpha = Math.max(0, this.alpha - 0.1f);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
            BloodDripParticle particle = new BloodDripParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
