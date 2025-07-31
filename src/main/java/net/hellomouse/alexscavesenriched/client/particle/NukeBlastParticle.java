package net.hellomouse.alexscavesenriched.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NukeBlastParticle extends SpriteBillboardParticle {
    public static final float DEFAULT_SCALE = 15.0F;

    protected NukeBlastParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.collidesWithWorld = false;
        this.scale(DEFAULT_SCALE);
        this.scale = DEFAULT_SCALE;

        this.setColor(1F, 1F, 1F);
        this.maxAge = 400;
        this.gravityStrength = 0.0F;
        this.velocityMultiplier = 1.0F;
        this.setAlpha(1.0F);

        // Mojang randomizes these but we don't want to
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
    }

    @Override
    public void tick() {
        super.tick();
        float f = (this.age - (float)(this.maxAge / 2)) / (float)this.maxAge;
        if (this.age > this.maxAge / 2)
            this.setAlpha(1.0F - f * 2F);
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
            NukeBlastParticle p = new NukeBlastParticle(level, x, y, z, xd, yd, zd);
            p.setSprite(sprites);
            return p;
        }
    }
}
