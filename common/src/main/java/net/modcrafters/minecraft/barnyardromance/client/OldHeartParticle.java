package net.modcrafters.minecraft.barnyardromance.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

// A small, faint dark heart that drifts up slowly and fades in and out. Meant to be easy to miss.
public class OldHeartParticle extends SingleQuadParticle {
    private static final float MAX_ALPHA = 0.5F;

    private OldHeartParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.friction = 0.9F;
        this.xd *= 0.01F;
        this.yd *= 0.01F;
        this.zd *= 0.01F;
        this.yd += 0.04;
        this.lifetime = 40;
        this.hasPhysics = false;
        this.setAlpha(0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.setAlpha(MAX_ALPHA * Mth.sin((float) Math.PI * this.age / this.lifetime));
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xAux,
                double yAux,
                double zAux,
                RandomSource random
        ) {
            return new OldHeartParticle(level, x, y, z, this.sprite.get(random));
        }
    }
}
