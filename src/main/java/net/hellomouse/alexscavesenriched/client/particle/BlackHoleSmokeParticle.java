package net.hellomouse.alexscavesenriched.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlackHoleSmokeParticle extends SpriteBillboardParticle {
    public static final float DEFAULT_SCALE = 45.0F;
    public static final int DEFAULT_AGE = 5;

    protected BlackHoleSmokeParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.collidesWithWorld = false;
        this.scale(DEFAULT_SCALE);
        this.scale = DEFAULT_SCALE;

        this.setColor(1F, 1F, 1F);
        this.maxAge = 40;
        this.gravityStrength = 0.0F;
        this.velocityMultiplier = 1.01F;
        this.setAlpha(1.0F);

        // Mojang randomizes these but we don't want to
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
    }

    @Override
    public void tick() {
        super.tick();
        float f = this.age / (float)this.maxAge;
        float f2 = (float)Math.pow(f, 0.5);
        this.setAlpha(f2 * 0.6F);
        float newScale = (1.0F - f) * DEFAULT_SCALE;
        this.scale(newScale);
        this.scale = newScale;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            BlackHoleSmokeParticle p = new BlackHoleSmokeParticle(level, x, y, z, xd, yd, zd);
            p.setSprite(sprites);
            return p;
        }
    }
}
