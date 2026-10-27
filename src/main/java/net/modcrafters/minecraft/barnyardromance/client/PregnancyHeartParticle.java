package net.modcrafters.minecraft.barnyardromance.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
//? if >=1.21.9 {
import net.minecraft.client.particle.SingleQuadParticle;
//?} else {
/*import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
*///?}
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

//? if >=1.21.9 {
public class PregnancyHeartParticle extends SingleQuadParticle {
//?} else
//public class PregnancyHeartParticle extends TextureSheetParticle {
    private PregnancyHeartParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        //? if >=1.21.9 {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
        //?} else {
        /*super(level, x, y, z, 0.0, 0.0, 0.0);
        this.setSprite(sprite);
        *///?}
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 0.86F;
        this.xd *= 0.01F;
        this.yd *= 0.01F;
        this.zd *= 0.01F;
        this.yd += 0.08;
        this.quadSize *= 1.5F;
        this.lifetime = 22;
        this.hasPhysics = false;
    }

    //? if >=1.21.9 {
    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }
    //?} else {
    /*@Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
    *///?}

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * Mth.clamp((this.age + partialTick) / this.lifetime * 32.0F, 0.0F, 1.0F);
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
                //? if >=1.21.9 {
                double zAux,
                RandomSource random
        ) {
            return new PregnancyHeartParticle(level, x, y, z, this.sprite.get(random));
                //?} else {
                /*double zAux
        ) {
            return new PregnancyHeartParticle(level, x, y, z, this.sprite.get(level.random));
                *///?}
        }
    }
}
