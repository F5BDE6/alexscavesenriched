package net.hellomouse.alexscavesenriched.client.particle;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.Random;

import static java.lang.Math.*;
import static org.joml.Math.lerp;

public class DemonCoreGlowTexture {
    public static final Identifier ID = Identifier.of(AlexsCavesEnriched.MODID, "demon_core_glow_sprite");
    public static List<NativeImage> IMAGES;
    public static NativeImage CURRENT;
    private static float time;
    private static boolean reloading;
    private static boolean firstTime = true;
    private static final Random random = new Random();

    private static float clamp(float x, float minVal, float maxVal) {
        return min(max(x, minVal), maxVal);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        var t = clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return (float) (t * t * (3.0 - 2.0 * t));
    }

    public static void init() {
        // Side note:
        // Minecraft discard anything with alpha <0.1
        // The shader tint can apply *0.6 alpha
        // thus, some pixel can be invisible, creating a glowing effect
        // I don't want to fix it, so it's a feature now
        var resolution = AlexsCavesEnriched.CONFIG.demonCore.sprite.resolution;

        if (firstTime) {
            CURRENT = new NativeImage(AlexsCavesEnriched.CONFIG.demonCore.sprite.getSpriteWidth(), AlexsCavesEnriched.CONFIG.demonCore.sprite.getSpriteHeight(), false);
            firstTime = false;
        }

        CURRENT.fillRect(0, 0, CURRENT.getWidth(), CURRENT.getHeight(), 0x00000000);

        var centerX = 0.5f;
        var centerY = 0.5f;

        var diameter = 1f;
        var radius = diameter / 2;
        float radiusSquared = radius * radius;

        var animationCount = AlexsCavesEnriched.CONFIG.demonCore.sprite.animationFrames;

        for (int nthAnimation = animationCount - 1; nthAnimation >= 0; nthAnimation--) {
            try {
                for (int aX = 0; aX < resolution; aX++) {
                    for (int aY = 0; aY < resolution; aY++) {
                        float alpha = 1f;
                        var x = (float) aX / (float) resolution;
                        var y = (float) aY / (float) resolution;
                        var distanceFromCenterSqr = pow(x - centerX, 2) + pow(y - centerY, 2);
                        alpha = alpha + smoothstep(radiusSquared - 0.1f, radiusSquared, (float) distanceFromCenterSqr) * -alpha;
                        if (alpha > 0) {
                            float decay = (float) lerp(1, 0, distanceFromCenterSqr / radiusSquared);
                            alpha += 0.5f * decay * (DemonCoreGlowTexture.random.nextFloat() - 0.5f);
                            alpha *= decay;
                            alpha = max(alpha, 0.1f);
                            CURRENT.setColor(aX, aY + (nthAnimation * resolution), ColorHelper.Argb.getArgb((int) clamp((alpha + ((aX & 1) + (aY & 1) > 0 ? 0 : 1) * AlexsCavesEnriched.CONFIG.demonCore.sprite.noiseStrength) * 255, 0, 255), 255, 255, 255));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void reload() {
        if (!reloading) {
            reloading = true;
            init();
            reloading = false;
        } else {
            AlexsCavesEnriched.LOGGER.warn("Already reloading radiation sprite, skipping");
        }
    }
}
