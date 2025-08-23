package net.hellomouse.alexscavesenriched.client.particle;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.particle.abs.AbstractBlueGlowParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NeutronBlastParticle extends AbstractBlueGlowParticle {
    public static final int LIFETIME = 200;
    public static final float TARGET_SIZE = AlexsCavesEnriched.CONFIG.neutron.radius * 8F;

    protected NeutronBlastParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.lifetime = LIFETIME;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return AbstractBlueGlowParticle.PARTICLE_SHEET_NEUTRON_BOMB_CPU;
    }

    @Override
    public void tick() {
        super.tick();
        float f = (this.age - (float) (this.lifetime / 2)) / (float) this.lifetime;
        if (this.age > this.lifetime / 2)
            this.setAlpha(1.0F - f * 2F);
        this.setSize((float) (TARGET_SIZE * Math.pow(this.age / (float) this.lifetime, 0.2F)));
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        public Factory() {}

        public Factory(SpriteSet _unused) {
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new NeutronBlastParticle(level, x, y, z, xd, yd, zd);
        }
    }
}
