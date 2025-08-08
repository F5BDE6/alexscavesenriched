package net.hellomouse.alexscavesenriched.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RailgunShockwaveParticle extends ExplosionLargeParticle {
    protected RailgunShockwaveParticle(ClientWorld arg, double d, double e, double f, double g, SpriteProvider arg2) {
        super(arg, d, e, f, g, arg2);
        this.maxAge = 8;
        this.scale = 0.6F; // 1.5
        this.setSpriteForAge(arg2);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
            return new RailgunShockwaveParticle(arg2, d, e, f, g, this.spriteProvider);
        }
    }
}
