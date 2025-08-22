package net.hellomouse.alexscavesenriched.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RailgunShockwaveParticle extends HugeExplosionParticle {
    protected RailgunShockwaveParticle(ClientLevel arg, double d, double e, double f, double g, SpriteSet arg2) {
        super(arg, d, e, f, g, arg2);
        this.lifetime = 8;
        this.quadSize = 0.6F; // 1.5
        this.setSpriteFromAge(arg2);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType arg, ClientLevel arg2, double d, double e, double f, double g, double h, double i) {
            return new RailgunShockwaveParticle(arg2, d, e, f, g, this.spriteProvider);
        }
    }
}
