package net.hellomouse.alexscavesenriched.client.particle;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.particle.abs.AbstractBlueGlowParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DemonCoreGlowParticle extends AbstractBlueGlowParticle {
    public static final int LIFETIME = Integer.MAX_VALUE;

    protected DemonCoreGlowParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.expandSize(0.0F);
        this.maxAge = LIFETIME;
    }

    @Override
    public ParticleTextureSheet getType() {
        return AbstractBlueGlowParticle.PARTICLE_SHEET_DEMONCORE_CPU;
    }

    public void expandSize(float bonus) {
        this.setSize(AlexsCavesEnriched.CONFIG.demonCore.diameter + bonus);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new DemonCoreGlowParticle(level, x, y, z, xd, yd, zd);
        }
    }
}
