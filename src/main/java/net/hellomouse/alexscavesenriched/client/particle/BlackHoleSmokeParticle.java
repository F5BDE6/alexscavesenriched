package net.hellomouse.alexscavesenriched.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlackHoleSmokeParticle extends TextureSheetParticle {
    public static final float DEFAULT_SCALE = 45.0F;
    public static final int DEFAULT_AGE = 5;

    protected BlackHoleSmokeParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.hasPhysics = false;
        this.scale(DEFAULT_SCALE);
        this.quadSize = DEFAULT_SCALE;

        this.setColor(1F, 1F, 1F);
        this.lifetime = 40;
        this.gravity = 0.0F;
        this.friction = 1.01F;
        this.setAlpha(1.0F);

        // Mojang randomizes these but we don't want to
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
    }

    @Override
    public void tick() {
        super.tick();
        float f = this.age / (float) this.lifetime;
        float f2 = (float)Math.pow(f, 0.5);
        this.setAlpha(f2 * 0.6F);
        float newScale = (1.0F - f) * DEFAULT_SCALE;
        this.scale(newScale);
        this.quadSize = newScale;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            BlackHoleSmokeParticle p = new BlackHoleSmokeParticle(level, x, y, z, xd, yd, zd);
            p.pickSprite(sprites);
            return p;
        }
    }
}
