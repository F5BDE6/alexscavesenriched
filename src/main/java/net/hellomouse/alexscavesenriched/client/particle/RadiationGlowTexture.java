package net.hellomouse.alexscavesenriched.client.particle;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class RadiationGlowTexture {
    public static final Identifier ID = Identifier.of(AlexsCavesEnriched.MODID, "demon_core_glow_sprite");
    public static List<NativeImage> IMAGES;
    public static NativeImage CURRENT;
    private static float time;
    private static boolean reloading;
    private static boolean firstTime = true;

    private static float clamp(float x, float minVal, float maxVal) {
        return min(max(x, minVal), maxVal);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        var t = clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return (float) (t * t * (3.0 - 2.0 * t));
    }

    public static void init() {
        if (firstTime) {
            CURRENT = new NativeImage(AlexsCavesEnriched.CONFIG.demonCore.sprite.resolution, AlexsCavesEnriched.CONFIG.demonCore.sprite.resolution, false);
            firstTime = false;
        }
        var height = AlexsCavesEnriched.CONFIG.demonCore.sprite.resolution;
        var width = AlexsCavesEnriched.CONFIG.demonCore.sprite.resolution;
        var centerX = 0.5f;
        var centerY = 0.5f;

        var diameterFluctuation = AlexsCavesEnriched.CONFIG.demonCore.sprite.fluctuation;
        var maxDiameter = 1f;

        var animationCount = AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames;
        var animationStep = diameterFluctuation / (animationCount - 1);
        List<NativeImage> frames = new ArrayList<>();

        for (int i = animationCount - 1; i >= 0; i--) {
            float diameter = maxDiameter - animationStep * i;
            var radius = diameter / 2;
            float radiusSquared = radius * radius;
            try {
                NativeImage image = new NativeImage(width, height, false);
                for (int aX = 0; aX < width; aX++) {
                    for (int aY = 0; aY < height; aY++) {
                        var alpha = 255;
                        var x = (float) aX / (float) width;
                        var y = (float) aY / (float) height;
                        var distanceFromCenterSqr = pow(x - centerX, 2) + pow(y - centerY, 2);
                        alpha = (int) (alpha + smoothstep(radiusSquared - 0.1f, radiusSquared, (float) distanceFromCenterSqr) * -alpha);
                        image.setColor(aX, aY, ColorHelper.Argb.getArgb(alpha, 255, 255, 255));
                    }
                }
                frames.add(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        IMAGES = frames;
        CURRENT.copyFrom(IMAGES.get(0));
    }

    public static void tick() {
        if (!reloading) {
            var size = IMAGES.size() - 1;
            var a = floor(time) % (size * 2);
            var b = floor(time) % size;
            var index = abs(max(0, a - b) - b);
            CURRENT.copyFrom(IMAGES.get((int) index));
            time += 1 * MinecraftClient.getInstance().getTickDelta();
        }
    }

    public static void close() {
        for (int i = 0; i < IMAGES.size(); i++) {
            IMAGES.get(i).close();
        }
    }

    public static void reload() {
        if (!reloading) {
            if (IMAGES == null || IMAGES.isEmpty()) {
                AlexsCavesEnriched.LOGGER.warn("No sprite generated, skipping reload");
                return;
            }
            reloading = true;
            close();
            init();
            reloading = false;
        } else {
            AlexsCavesEnriched.LOGGER.warn("Already reloading radiation sprite, skipping");
        }
    }
}
