package net.hellomouse.alexscavesenriched.client.particle;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static java.lang.Math.floor;

public class NeutronBlastParticle extends SpriteBillboardParticle {
    public static final int LIFETIME = 200;
    public static final float TARGET_SIZE = AlexsCavesEnriched.CONFIG.neutron.radius * 8F;
    public static float frame = 0;
    protected NeutronBlastParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.collidesWithWorld = false;

        this.setColor(1F, 1F, 1F);
        this.maxAge = LIFETIME;
        this.gravityStrength = 0.0F;
        this.velocityMultiplier = 1.0F;
        this.setAlpha(1.0F);
        this.setSize(0);

        // Mojang randomizes these but we don't want to
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
    }

    private void setSize(float s) {
        this.scale(s);
        this.scale = s;
    }

    @Override
    public ParticleTextureSheet getType() {
        return DemonCoreGlowParticle.PARTICLE_SHEET_DEMONCORE_CPU;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d vec3d = camera.getPos();
        float tx = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
        float ty = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
        float tz = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());

        Vector3f forward = new Vector3f(0, 0, -1);
        Vector3f lookDir = forward.rotate(camera.getRotation());
        float fade = 1F;

        Vec3d dir = new Vec3d(tx, ty, tz);
        double len = dir.length();
        final boolean inside = len < this.scale;
        if (inside) {
            // Fade out if looking out
            final float FADE_DISTANCE = 5F;
            final var dotProduct = (new Vec3d(lookDir)).dotProduct(dir);
            if (len > this.scale - FADE_DISTANCE && dotProduct > 0)
                fade = 1F - (float)((len - (this.scale - FADE_DISTANCE)) / FADE_DISTANCE);

            lookDir.mul(-0.1F);
            tx = lookDir.x;
            ty = lookDir.y;
            tz = lookDir.z;
        } else {
            double moveScale = (len - this.scale) / len;
            tx *= moveScale;
            ty *= moveScale;
            tz *= moveScale;
        }

        Quaternionf quaternionf;
        if (this.angle == 0.0F) {
            quaternionf = camera.getRotation();
        } else {
            quaternionf = new Quaternionf(camera.getRotation());
            quaternionf.rotateZ(MathHelper.lerp(tickDelta, this.prevAngle, this.angle));
        }

        Vector3f[] vector3fs = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float i = this.getSize(tickDelta);

        for (int j = 0; j < 4; j++) {
            Vector3f vector3f = vector3fs[j];
            vector3f.rotate(quaternionf);
            vector3f.mul(i);
            vector3f.add(tx, ty, tz);
        }

        float k = this.getMinU();
        float l = this.getMaxU();
        float m = this.getMinV();
        float n = this.getMaxV();
        int o = this.getBrightness(tickDelta);
        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).texture(l, n).color(this.red, this.green, this.blue, this.alpha * fade).light(o).next();
        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).texture(l, m).color(this.red, this.green, this.blue, this.alpha * fade).light(o).next();
        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).texture(k, m).color(this.red, this.green, this.blue, this.alpha * fade).light(o).next();
        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).texture(k, n).color(this.red, this.green, this.blue, this.alpha * fade).light(o).next();
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    protected float getMinU() {
        return 0;
    }

    @Override
    protected float getMinV() {
        return 1.0f / AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames * (float) ((int) ((float) floor(frame) % AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames));

    }

    @Override
    protected float getMaxU() {
        return 1;
    }

    @Override
    protected float getMaxV() {
        return 1.0f / AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames * (float) ((int) ((float) floor(frame) % AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames) + 1);
    }

    @Override
    public void tick() {
        super.tick();
        float f = (this.age - (float)(this.maxAge / 2)) / (float)this.maxAge;
        if (this.age > this.maxAge / 2)
            this.setAlpha(1.0F - f * 2F);
        this.setSize((float)(TARGET_SIZE * Math.pow(this.age / (float)this.maxAge, 0.2F)));
        frame++;
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
