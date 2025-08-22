package net.hellomouse.alexscavesenriched.client.particle.texture;

import net.hellomouse.alexscavesenriched.ACEConfig;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import java.util.Random;

import static java.lang.Math.*;
import static org.joml.Math.lerp;

import D;
import F;
import I;
import com.mojang.blaze3d.platform.NativeImage;

public class DemonCoreGlowTexture {
    static final java.util.Random random = new Random();
    public static ResourceLocation ID = ResourceLocation.tryBuild(AlexsCavesEnriched.MODID, "dynamic/demon_core_glow_sprite");
    public static DynamicTexture CURRENT;
    public static ACEConfig.DemonCoreConfig.Sprite CONFIG_CACHE = new ACEConfig.DemonCoreConfig.Sprite();

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
        // Fixed with dithering
        var resolution = AlexsCavesEnriched.CONFIG.client.demonCoreSprite.resolution;
        var image = new NativeImage(
                AlexsCavesEnriched.CONFIG.client.demonCoreSprite.getSpriteWidth(),
                AlexsCavesEnriched.CONFIG.client.demonCoreSprite.getSpriteHeight(), false);
        image.fillRect(0, 0, image.getWidth(), image.getHeight(), 0x00000000);

        var centerX = 0.5f;
        var centerY = 0.5f;

        var diameter = 1f;
        var radius = diameter / 2;
        float radiusSquared = radius * radius;
        float ditherRadiusSquared = (radius * 0.7F) * (radius * 0.7F);
        var animationCount = AlexsCavesEnriched.CONFIG.client.demonCoreSprite.animationFrames;

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
                            alpha += 0.5f * decay * (random.nextFloat() - 0.5f);
                            alpha *= decay;

                            if (distanceFromCenterSqr > ditherRadiusSquared + random.nextFloat() * 0.1F) {
                                float ditherStrength = ((float)distanceFromCenterSqr - ditherRadiusSquared) / (radiusSquared - ditherRadiusSquared);
                                ditherStrength = (float)pow(ditherStrength, 0.2F);
                                alpha += random.nextFloat() * 0.15 * ditherStrength;
                            }

                            var pixelColor = FastColor.ARGB32.color((int) clamp((alpha) * 255, 0, 255), 255, 255, 255);
                            image.setPixelRGBA(aX, aY + (nthAnimation * resolution), pixelColor);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (CURRENT == null) {
            CURRENT = new DynamicTexture(image);
        } else {
            CURRENT.setPixels(image);
        }
        Minecraft.getInstance().textureManager.register(ID, CURRENT);
    }

    public static void resetIfChanged() {
        if (!CONFIG_CACHE.equals(AlexsCavesEnriched.CONFIG.client.demonCoreSprite)) {
            CONFIG_CACHE = AlexsCavesEnriched.CONFIG.client.demonCoreSprite.copy();
            reset();
        }
    }

    public static void reset() {
        if (CURRENT != null) {
            CURRENT.close();
            CURRENT = null;
        }
        init();
    }
}
