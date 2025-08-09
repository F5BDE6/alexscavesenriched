package net.hellomouse.alexscavesenriched.client.particle;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class RadiationGlowTexture {
    public static final Identifier ID = Identifier.of(AlexsCavesEnriched.MODID, "demon_core_glow_sprite");
    public static int WIDTH = 1280;
    public static int HEIGHT = 1280;
    public static List<NativeImage> IMAGES;
    public static NativeImage CURRENT;
    private static int time;

    private static float clamp(float x, float minVal, float maxVal) {
        return min(max(x, minVal), maxVal);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        var t = clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return (float) (t * t * (3.0 - 2.0 * t));
    }

    public static void init() {
        CURRENT = new NativeImage(WIDTH, HEIGHT, false);
        var centerX = WIDTH / 2;
        var centerY = HEIGHT / 2;

        var diameterFluctuation = 300;
        //var minDiameter = WIDTH-diameterFluctuation;
        var maxDiameter = WIDTH;

        var animationCount = 10;
        var animationStep = diameterFluctuation / 10;

        List<NativeImage> frames = new ArrayList<>();

        for (int i = animationCount - 1; i >= 0; i--) {
            var diameter = maxDiameter - animationStep * i;
            try {
                NativeImage image = new NativeImage(WIDTH, HEIGHT, false);
                for (int x = 0; x < WIDTH; x++) {
                    for (int y = 0; y < HEIGHT; y++) {
                        var c = 255;
                        var distanceFromCenterSqr = pow(x - centerX, 2) + pow(y - centerY, 2);
                        c = (int) (c + smoothstep(diameter - 100f, diameter, (float) distanceFromCenterSqr) * -c);
                        image.setColor(x, y, ColorHelper.Argb.getArgb(c, c, c, c));
                    }
                }
                frames.add(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        IMAGES = frames;
    }

    public static void tick() {
        var index = time % IMAGES.size();
        CURRENT.copyFrom(IMAGES.get(index));
        time++;
    }

    public static void close() {
        CURRENT.close();
        for (int i = 0; i < IMAGES.size(); i++) {
            IMAGES.get(i).close();
        }
    }
}
