package net.hellomouse.alexscavesenriched.client.particle.abs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.particle.texture.DemonCoreGlowTexture;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;


public abstract class AbstractBlueGlowParticle extends TextureSheetParticle {
    public int frame = 0;
    private float realSize = 0;

    @OnlyIn(Dist.CLIENT)
    public static ParticleRenderType PARTICLE_SHEET_DEMONCORE_CPU = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) { beginImpl(builder, textureManager, 0); }
        @Override
        public void end(Tesselator tessellator) {
            drawImpl(tessellator);
        }
        public String toString() {
            return "PARTICLE_SHEET_DEMONCORE";
        }
    };
    @OnlyIn(Dist.CLIENT)
    public static ParticleRenderType PARTICLE_SHEET_NEUTRON_BOMB_CPU = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) { beginImpl(builder, textureManager, 1); }
        @Override
        public void end(Tesselator tessellator) {
            drawImpl(tessellator);
        }
        public String toString() {
            return "PARTICLE_SHEET_NEUTRON_BOMB";
        }
    };

    protected AbstractBlueGlowParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.hasPhysics = false;
        this.setColor(1F, 1F, 1F);
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.setAlpha(1.0F);
        this.setSize(0);

        // Mojang randomizes these but we don't want to
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
    }

    @OnlyIn(Dist.CLIENT)
    private static void beginImpl(BufferBuilder builder, TextureManager textureManager, int variant) {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderTexture(0, DemonCoreGlowTexture.ID);

        var color = variant == 0 ? getDemonCoreColorForTick() : getNeutronBombColorForTick();
        float r = Math.min(1f, Math.max(0f, color.x));
        float g = Math.min(1f, Math.max(0f, color.y));
        float b = Math.min(1f, Math.max(0f, color.z));
        float a = Math.min(1f, Math.max(0f, color.w));
        RenderSystem.setShaderColor(r, g, b, a);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawImpl(Tesselator tessellator) {
        tessellator.end();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
    }

    protected void setSize(float s) {
        this.quadSize = 1F;
        this.scale(s);
        this.realSize = s;
    }

    // Set size without setting real size
    protected void setSizeTemporary(float s) {
        this.quadSize = 1F;
        this.scale(s);
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3 vec3d = camera.getPosition();
        float tx = (float) (Mth.lerp(tickDelta, this.xo, this.x) - vec3d.x());
        float ty = (float) (Mth.lerp(tickDelta, this.yo, this.y) - vec3d.y());
        float tz = (float) (Mth.lerp(tickDelta, this.zo, this.z) - vec3d.z());

        Vector3f forward = new Vector3f(0, 0, -1);
        Vector3f lookDir = forward.rotate(camera.rotation());
        float fade = 1F;

        Vec3 dir = new Vec3(tx, ty, tz);
        double len = dir.length();
        final boolean inside = len < this.realSize;
        if (inside) {
            // Fade out if looking out
            final float FADE_DISTANCE = this.realSize * 0.1F;
            final var dotProduct = (new Vec3(lookDir)).dot(dir);
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
        if (this.roll == 0.0F) {
            quaternionf = camera.rotation();
        } else {
            quaternionf = new Quaternionf(camera.rotation());
            quaternionf.rotateZ(Mth.lerp(tickDelta, this.oRoll, this.roll));
        }

        Vector3f[] vector3fs = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float i = this.getQuadSize(tickDelta);

        for (int j = 0; j < 4; j++) {
            Vector3f vector3f = vector3fs[j];
            vector3f.rotate(quaternionf);
            vector3f.mul(i);
            vector3f.add(tx, ty, tz);
        }

        float k = this.getU0();
        float l = this.getU1();
        float m = this.getV0();
        float n = this.getV1();
        int o = this.getLightColor(tickDelta);
        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(l, n).color(this.rCol, this.gCol, this.bCol, this.alpha * fade).uv2(o).endVertex();
        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(l, m).color(this.rCol, this.gCol, this.bCol, this.alpha * fade).uv2(o).endVertex();
        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(k, m).color(this.rCol, this.gCol, this.bCol, this.alpha * fade).uv2(o).endVertex();
        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(k, n).color(this.rCol, this.gCol, this.bCol, this.alpha * fade).uv2(o).endVertex();
    }

    @Override
    protected int getLightColor(float tickDelta) {
        return LightTexture.FULL_BLOCK;
    }

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
    protected float getU0() {
        return 0;
    }

    public int getNumFrames() {
        return AlexsCavesEnriched.CONFIG.client.demonCoreSprite.animationFrames;
    }

    @Override
    protected float getV0() {
        return 1.0f / getNumFrames() * (frame % getNumFrames());
    }

    @Override
    protected float getV1() {
        return getV0() + 1.0f / getNumFrames();
    }

    @Override
    protected float getU1() {
        return 1;
    }

    @Override
    public void tick() {
        super.tick();
        frame++;
    }
}
