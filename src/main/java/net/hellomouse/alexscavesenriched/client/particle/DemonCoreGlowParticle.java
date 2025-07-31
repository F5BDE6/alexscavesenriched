package net.hellomouse.alexscavesenriched.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.render.ACEInternalShaders;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;

public class DemonCoreGlowParticle extends SpriteBillboardParticle {
    public static final int LIFETIME = Integer.MAX_VALUE;

    protected DemonCoreGlowParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.collidesWithWorld = false;
        this.expandSize(0.0F);

        this.setColor(1F, 1F, 1F);
        this.maxAge = LIFETIME;
        this.gravityStrength = 0.0F;
        this.velocityMultiplier = 1.0F;
        this.setAlpha(1.0F);

        // Mojang randomizes these but we don't want to
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
    }

    public void expandSize(float bonus) {
        this.scale(AlexsCavesEnriched.CONFIG.demonCore.diameter + bonus);
        this.scale = AlexsCavesEnriched.CONFIG.demonCore.diameter + bonus;
    }

    @Override
    public ParticleTextureSheet getType() {
        return PARTICLE_SHEET_DEMONCORE;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d vec3d = camera.getPos();
        float tx = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
        float ty = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
        float tz = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());

        double len = (new Vec3d(tx, ty, tz)).length();
        final boolean inside = len < this.scale;
        if (inside) {
            Vector3f forward = new Vector3f(0, 0, -1);
            Vector3f lookDir = forward.rotate(camera.getRotation()).mul(-0.1F);
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
        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).texture(l, n).color(this.red, this.green, this.blue, this.alpha).light(o).next();
        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).texture(l, m).color(this.red, this.green, this.blue, this.alpha).light(o).next();
        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).texture(k, m).color(this.red, this.green, this.blue, this.alpha).light(o).next();
        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).texture(k, n).color(this.red, this.green, this.blue, this.alpha).light(o).next();
    }

    @OnlyIn(Dist.CLIENT)
    public static ParticleTextureSheet PARTICLE_SHEET_DEMONCORE = new ParticleTextureSheet() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.blendEquation(GL_FUNC_ADD);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ACEInternalShaders::getRadiationParticleShader);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        @Override
        public void draw(Tessellator tessellator) {
            tessellator.draw();
        }

        public String toString() {
            return "PARTICLE_SHEET_DEMONCORE";
        }
    };

    @Override
    protected float getMinU() {
        return 0.0F;
    }
    @Override
    protected float getMaxU() {
        return 1.0F;
    }
    @Override
    protected float getMinV() {
        return 0.0F;
    }
    @Override
    protected float getMaxV() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        public Factory() {}
        public Factory(SpriteProvider _unused) {}

        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new DemonCoreGlowParticle(level, x, y, z, xd, yd, zd);
        }
    }
}
