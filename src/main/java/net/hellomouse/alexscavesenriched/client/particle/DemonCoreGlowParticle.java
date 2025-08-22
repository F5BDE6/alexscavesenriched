package net.hellomouse.alexscavesenriched.client.particle;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.particle.abs.AbstractBlueGlowParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DemonCoreGlowParticle extends AbstractBlueGlowParticle {
    public static final int LIFETIME = Integer.MAX_VALUE;

    protected DemonCoreGlowParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.expandSize(0.0F);
        this.lifetime = LIFETIME;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return AbstractBlueGlowParticle.PARTICLE_SHEET_DEMONCORE_CPU;
    }

    public void expandSize(float bonus) {
        this.setSize(AlexsCavesEnriched.CONFIG.demonCore.diameter + bonus);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new DemonCoreGlowParticle(level, x, y, z, xd, yd, zd);
        }
    }
}
