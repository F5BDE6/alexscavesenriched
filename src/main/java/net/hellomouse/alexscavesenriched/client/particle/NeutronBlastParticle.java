package net.hellomouse.alexscavesenriched.client.particle;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.particle.abs.AbstractBlueGlowParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NeutronBlastParticle extends AbstractBlueGlowParticle {
    public static final int LIFETIME = 200;
    public static final float TARGET_SIZE = AlexsCavesEnriched.CONFIG.neutron.radius * 8F;

    protected NeutronBlastParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.maxAge = LIFETIME;
    }

    @Override
    public ParticleTextureSheet getType() {
        return AbstractBlueGlowParticle.PARTICLE_SHEET_NEUTRON_BOMB_CPU;
    }

    @Override
    public void tick() {
        super.tick();
        float f = (this.age - (float)(this.maxAge / 2)) / (float)this.maxAge;
        if (this.age > this.maxAge / 2)
            this.setAlpha(1.0F - f * 2F);
        this.setSize((float)(TARGET_SIZE * Math.pow(this.age / (float)this.maxAge, 0.2F)));
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        public Factory() {}
        public Factory(SpriteProvider _unused) {}

        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new NeutronBlastParticle(level, x, y, z, xd, yd, zd);
        }
    }
}
