package net.hellomouse.alexscavesenriched.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NukeBlastParticle extends TextureSheetParticle {
    public static final float DEFAULT_SCALE = 15.0F;

    protected NukeBlastParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.hasPhysics = false;
        this.scale(DEFAULT_SCALE);
        this.quadSize = DEFAULT_SCALE;

        this.setColor(1F, 1F, 1F);
        this.lifetime = 400;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.setAlpha(1.0F);

        // Mojang randomizes these but we don't want to
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
    }

    @Override
    public void tick() {
        super.tick();
        float f = (this.age - (float) (this.lifetime / 2)) / (float) this.lifetime;
        if (this.age > this.lifetime / 2)
            this.setAlpha(1.0F - f * 2F);
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
            NukeBlastParticle p = new NukeBlastParticle(level, x, y, z, xd, yd, zd);
            p.pickSprite(sprites);
            return p;
        }
    }
}
