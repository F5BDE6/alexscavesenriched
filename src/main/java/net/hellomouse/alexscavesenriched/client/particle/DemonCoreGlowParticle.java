package net.hellomouse.alexscavesenriched.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
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
import org.joml.Vector4f;

import static java.lang.Math.floor;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;

public class DemonCoreGlowParticle extends SpriteBillboardParticle {
    public static final int LIFETIME = Integer.MAX_VALUE;
    private float frame = 0;

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

    @OnlyIn(Dist.CLIENT)
    public static ParticleTextureSheet PARTICLE_SHEET_DEMONCORE_CPU = new ParticleTextureSheet() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.blendEquation(GL_FUNC_ADD);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getParticleProgram);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
            var colour = cpuShader();
            float r = Math.min(1f, Math.max(0f, colour.x));
            float g = Math.min(1f, Math.max(0f, colour.y));
            float b = Math.min(1f, Math.max(0f, colour.z));
            float a = Math.min(1f, Math.max(0f, colour.w));
            RenderSystem.setShaderColor(r, g, b, a);
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        @Override
        public void draw(Tessellator tessellator) {
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.enableCull();
        }

        public String toString() {
            return "PARTICLE_SHEET_DEMONCORE";
        }
    };

    public static ParticleTextureSheet getParticleSheet() {
        return PARTICLE_SHEET_DEMONCORE_CPU;

    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    private static Vector4f cpuShader() {
        float time = RenderSystem.getShaderGameTime();
        var animation = time * 2000;
        var animation1 = (float) (Math.sin(animation) + 1);
        return new Vector4f(0, animation1 * 0.15F + 0.85F, animation1 * 0.15F + 0.85F, 0.8F - (0.2F * animation1));
    }

    @Override
    public ParticleTextureSheet getType() {
        return getParticleSheet();
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d vec3d = camera.getPos();
        float tx = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
        float ty = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
        float tz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());

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
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            var particle = new DemonCoreGlowParticle(level, x, y, z, xd, yd, zd);
            particle.setSprite(this.spriteProvider);
            return particle;
        }
    }

    @Override
    protected float getMinU() {
        //return 0;
        return super.getMinU();
        //return super.getMinU() + (super.getMaxU() - super.getMinU()) / AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames * frame;
    }

    @Override
    protected float getMinV() {
        //return 0;
        //return super.getMinV();
        return super.getMinV() + (super.getMaxV() - super.getMinV()) / AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames * (float) ((int) ((float) floor(frame) % AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames));

    }

    @Override
    protected float getMaxU() {
        //return 1;
        return super.getMaxU();
        //return super.getMinU() + (super.getMaxU() - super.getMinU()) / AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames * (frame + 1);
    }

    @Override
    protected float getMaxV() {
        //return 1;
        return super.getMinV() + (super.getMaxV() - super.getMinV()) / AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames * (float) ((int) ((float) floor(frame) % AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames) + 1);
    }

    @Override
    public void tick() {
        super.tick();
        frame++;
    }
}
