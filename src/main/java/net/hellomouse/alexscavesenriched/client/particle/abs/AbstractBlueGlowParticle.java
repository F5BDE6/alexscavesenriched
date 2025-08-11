package net.hellomouse.alexscavesenriched.client.particle.abs;

import com.mojang.blaze3d.systems.RenderSystem;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.particle.texture.DemonCoreGlowTexture;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public abstract class AbstractBlueGlowParticle extends SpriteBillboardParticle {
    public int frame = 0;
    private float realSize = 0;

    protected AbstractBlueGlowParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.collidesWithWorld = false;
        this.setColor(1F, 1F, 1F);
        this.gravityStrength = 0.0F;
        this.velocityMultiplier = 1.0F;
        this.setAlpha(1.0F);
        this.setSize(0);

        // Mojang randomizes these but we don't want to
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
    }

    protected void setSize(float s) {
        this.scale = 1F;
        this.scale(s);
        this.realSize = s;
    }

    // Set size without setting real size
    protected void setSizeTemporary(float s) {
        this.scale = 1F;
        this.scale(s);
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
        final boolean inside = len < this.realSize;
        if (inside) {
            // Fade out if looking out
            final float FADE_DISTANCE = this.realSize * 0.1F;
            final var dotProduct = (new Vec3d(lookDir)).dotProduct(dir);
            final float fadeScale = 1F - (float)((len - (this.realSize - FADE_DISTANCE)) / FADE_DISTANCE);
            final float clampedFadeScale = Math.min(fadeScale, 1F);

            if (len > this.realSize - FADE_DISTANCE && dotProduct > 0)
                fade = fadeScale;

            lookDir.mul(-0.1F);
            tx = lookDir.x;
            ty = lookDir.y;
            tz = lookDir.z;

            // Lerp between real size and fullscreen size
            // Scale 2 works for speed II while flying with max FOV
            final float INSIDE_SCALE = 2F;
            this.setSizeTemporary((1F - clampedFadeScale) * this.realSize + clampedFadeScale * INSIDE_SCALE);
        } else {
            double moveScale = (len - this.realSize) / len;
            tx *= moveScale;
            ty *= moveScale;
            tz *= moveScale;
            this.setSizeTemporary(realSize);
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
    protected int getBrightness(float tickDelta) {
        return LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE;
    }

    @OnlyIn(Dist.CLIENT)
    private static void beginImpl(BufferBuilder builder, TextureManager textureManager, int variant) {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getParticleProgram);
        RenderSystem.setShaderTexture(0, DemonCoreGlowTexture.ID);

        var color = variant == 0 ? getDemonCoreColorForTick() : getNeutronBombColorForTick();
        float r = Math.min(1f, Math.max(0f, color.x));
        float g = Math.min(1f, Math.max(0f, color.y));
        float b = Math.min(1f, Math.max(0f, color.z));
        float a = Math.min(1f, Math.max(0f, color.w));
        RenderSystem.setShaderColor(r, g, b, a);
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawImpl(Tessellator tessellator) {
        tessellator.draw();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
    }

    @OnlyIn(Dist.CLIENT)
    public static ParticleTextureSheet PARTICLE_SHEET_DEMONCORE_CPU = new ParticleTextureSheet() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) { beginImpl(builder, textureManager, 0); }
        @Override
        public void draw(Tessellator tessellator) { drawImpl(tessellator); }
        public String toString() {
            return "PARTICLE_SHEET_DEMONCORE";
        }
    };

    @OnlyIn(Dist.CLIENT)
    public static ParticleTextureSheet PARTICLE_SHEET_NEUTRON_BOMB_CPU = new ParticleTextureSheet() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) { beginImpl(builder, textureManager, 1); }
        @Override
        public void draw(Tessellator tessellator) { drawImpl(tessellator); }
        public String toString() {
            return "PARTICLE_SHEET_NEUTRON_BOMB";
        }
    };

    @Override
    public boolean shouldCull() {
        return false;
    }

    protected static Vector4f getDemonCoreColorForTick() {
        float time = RenderSystem.getShaderGameTime();
        var animation = time * 1000;
        var animation1 = (float) (Math.sin(animation) + 1);
        return new Vector4f(0,animation1 * 0.1F + 0.85F, animation1 * 0.1F + 0.85F,
                0.36F + animation1 * 0.05F);
    }

    protected static Vector4f getNeutronBombColorForTick() {
        return new Vector4f(0,0.93F, 0.93F, 2F);
    }

    @Override
    protected float getMinU() {
        return 0;
    }

    public int getNumFrames() {
        return AlexsCavesEnriched.CONFIG.client.demonCoreSprite.animationFrames;
    }

    @Override
    protected float getMinV() {
        return 1.0f / getNumFrames() * (frame % getNumFrames());
    }

    @Override
    protected float getMaxV() {
        return getMinV() + 1.0f / getNumFrames();
    }

    @Override
    protected float getMaxU() {
        return 1;
    }

    @Override
    public void tick() {
        super.tick();
        frame++;
    }
}
