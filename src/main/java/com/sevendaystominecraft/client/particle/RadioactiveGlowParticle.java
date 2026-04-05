package com.sevendaystominecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class RadioactiveGlowParticle extends TextureSheetParticle {

    protected RadioactiveGlowParticle(ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 20 + this.random.nextInt(20);
        this.rCol = 0.22f;
        this.gCol = 1.0f;
        this.bCol = 0.08f;
        this.alpha = 0.6f;
        this.quadSize = 0.15f + this.random.nextFloat() * 0.05f;
        this.xd = xSpeed + (this.random.nextFloat() - 0.5f) * 0.02f;
        this.yd = 0.01f + this.random.nextFloat() * 0.02f;
        this.zd = zSpeed + (this.random.nextFloat() - 0.5f) * 0.02f;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > this.lifetime / 2) {
            this.alpha = Math.max(0, this.alpha - 0.05f);
        }
        this.quadSize *= 0.98f;
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
            RadioactiveGlowParticle particle = new RadioactiveGlowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
