package net.hellomouse.alexscavesenriched.client;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.mojang.brigadier.CommandDispatcher;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.command.ReloadDemonCoreTextureCommand;
import net.hellomouse.alexscavesenriched.item.GammaFlashlightItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class ACEClientMod {
    // Reuse the submarine light for flashlight :)
    private static final Identifier FLASHLIGHT_SHADER = Identifier.of(AlexsCavesEnriched.MODID, "shaders/post/flashlight.json");

    private static final float[] nukeSkyDecayRates = {1, 0.002F, 0.0003F, 0.001F};

    private static final float[] nukeSkyProgressPerType = new float[NukeSkyType.values().length];

    public static Pair<Vec3d, Vec3d> getNukeSkyGradient(NukeSkyType type) {
        switch (type) {
            case NUKE, NONE -> {
                return NUKE_SKY_GRADIENT;
            }
            case NEUTRON -> {
                return NEUTRON_SKY_GRADIENT;
            }
            case BLACK_HOLE -> {
                return BLACK_HOLE_SKY_GRADIENT;
            }
        }
        return NUKE_SKY_GRADIENT; // Never reached
    }

    private static long lastTickTime = 0;

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        Entity player = MinecraftClient.getInstance().getCameraEntity();
        boolean firstPerson = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            GameRenderer renderer = MinecraftClient.getInstance().gameRenderer;

            boolean shouldFlash = false;
            if (firstPerson && player instanceof PlayerEntity playerE) {
                var stack1 = playerE.getInventory().getMainHandStack();
                var stack2 = playerE.getInventory().offHand.get(0);
                shouldFlash = (stack1.getItem() instanceof GammaFlashlightItem || stack2.getItem() instanceof GammaFlashlightItem) &&
                        (GammaFlashlightItem.isOn(stack1) || GammaFlashlightItem.isOn(stack2));
            }
            if (firstPerson && shouldFlash) {
                if (renderer.getPostProcessor() == null || !FLASHLIGHT_SHADER.toString().equals(renderer.getPostProcessor().getName())) {
                    attemptLoadShader(FLASHLIGHT_SHADER);
                }
            } else if (renderer.getPostProcessor() != null && FLASHLIGHT_SHADER.toString().equals(renderer.getPostProcessor().getName())) {
                renderer.onCameraEntitySet(null);
            }
        }
    }

    private static void attemptLoadShader(Identifier resourceLocation) {
        GameRenderer renderer = MinecraftClient.getInstance().gameRenderer;
        if (ClientProxy.shaderLoadAttemptCooldown <= 0) {
            renderer.loadPostProcessor(resourceLocation);
            if (!renderer.postProcessorEnabled) {
                ClientProxy.shaderLoadAttemptCooldown = 12000;
                AlexsCavesEnriched.LOGGER.warn("Alex's Caves Enriched could not load the shader {}, will attempt to load shader in 30 seconds", resourceLocation);
            }
        }
    }

    // Nuke sky colors
    // ----------------------------------------------------
    public static void setNukeSky(NukeSkyType type, float progress) {
        progress = Math.max(0F, Math.min(progress, 1F));
        nukeSkyProgressPerType[type.ordinal()] = Math.max(progress, nukeSkyProgressPerType[type.ordinal()]);
    }

    // Get gradient <start color + alpha, end color + alpha>
    // Alpha color of sky will be progress
    public static Pair<Vec3d, Vec3d> NUKE_SKY_GRADIENT = new Pair<>(new Vec3d(0.9, 0.2, 0), new Vec3d(0.9, 0.1, 0));
    public static Pair<Vec3d, Vec3d> NEUTRON_SKY_GRADIENT = new Pair<>(new Vec3d(0.1, 0.92, 1), new Vec3d(0, 0.85, 0.85));
    public static Pair<Vec3d, Vec3d> BLACK_HOLE_SKY_GRADIENT = new Pair<>(new Vec3d(0.2, 0.01, 0), new Vec3d(1, 0.1, 0));

    public static void computeFogColor(ViewportEvent.ComputeFogColor event, boolean first) {
        // Entity player = MinecraftClient.getInstance().player;
        Vec3d startColor = new Vec3d(event.getRed(), event.getBlue(), event.getGreen());

        if (event.getCamera().getSubmersionType() == CameraSubmersionType.NONE && AlexsCavesEnriched.CONFIG.client.overrideSkyColor) {
            var nukeSkyColor = getCurrentNukeSkyColor();
            if (nukeSkyColor.getRight() > 0) {
                Vec3d nukeColor = nukeSkyColor.getLeft();
                float skyAlpha = nukeSkyColor.getRight();
                skyAlpha = (float) Math.pow(skyAlpha, 0.8) * 0.9F;

                Vec3d curColor = startColor.add(nukeColor.subtract(startColor).multiply(skyAlpha));
                if (first) {
                    ClientProxy.acSkyOverrideAmount = skyAlpha;
                    ClientProxy.acSkyOverrideColor = curColor;
                } else {
                    event.setRed((float) curColor.x);
                    event.setBlue((float) curColor.z);
                    event.setGreen((float) curColor.y);
                }
            }
        }
    }

    // Nuke sky color, progress
    public static Pair<Vec3d, Float> getCurrentNukeSkyColor() {
        float totalAmt = 0;
        for (int i = 1; i < NukeSkyType.values().length; i++)
            totalAmt += nukeSkyProgressPerType[i];
        if (totalAmt <= 0)
            return new Pair<>(new Vec3d(0, 0, 0), 0F);

        Vec3d outColor = new Vec3d(0, 0, 0);
        float weightedProgress = 0;

        for (int i = 1; i < NukeSkyType.values().length; i++) {
            if (nukeSkyProgressPerType[i] > 0) {
                float progress = Math.min(1F, nukeSkyProgressPerType[i]);
                var grad = getNukeSkyGradient(NukeSkyType.values()[i]);
                Vec3d thisColor = grad.getRight().add((grad.getLeft().subtract(grad.getRight())).multiply(progress));
                outColor = outColor.add(thisColor.multiply(nukeSkyProgressPerType[i] / totalAmt));
                weightedProgress += nukeSkyProgressPerType[i] * (nukeSkyProgressPerType[i] / totalAmt);
            }
        }
        return new Pair<>(outColor, weightedProgress);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld level = mc.world;

        if (event.phase == TickEvent.Phase.START && level != null) {
            if (lastTickTime == level.getTime())
                return;
            lastTickTime = level.getTime();

            // Decay nuke sky glow even when nuke is not loaded
            for (int i = NukeSkyType.values().length - 1; i > 0; i--)
                nukeSkyProgressPerType[i] = Math.max(0F, nukeSkyProgressPerType[i] - nukeSkyDecayRates[i]);

            // Spawn nuke ash particles

            ClientPlayerEntity player = mc.player;
            if (player != null && AlexsCavesEnriched.CONFIG.client.nukeParticleEffects) {
                if (nukeSkyProgressPerType[NukeSkyType.BLACK_HOLE.ordinal()] > 0.1) {
                    for (int i = 0; i < 8; i++) {
                        double x = player.getX() + (level.random.nextDouble() - 0.5) * 16;
                        double y = player.getY() + level.random.nextDouble() * 6;
                        double z = player.getZ() + (level.random.nextDouble() - 0.5) * 16;
                        level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z,
                                (level.random.nextDouble() - 0.5) * 4,
                                (level.random.nextDouble() - 0.5) * 4,
                                (level.random.nextDouble() - 0.5) * 4);
                    }
                } else if (nukeSkyProgressPerType[NukeSkyType.NUKE.ordinal()] > 0) {
                    for (int i = 0; i < (nukeSkyProgressPerType[NukeSkyType.NUKE.ordinal()] < 0.3 ? 4 : 12); i++) {
                        double x = player.getX() + (level.random.nextDouble() - 0.5) * 16;
                        double y = player.getY() + level.random.nextDouble() * 6;
                        double z = player.getZ() + (level.random.nextDouble() - 0.5) * 16;
                        level.addParticle(ParticleTypes.ASH, x, y, z, 0, 0, 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<ServerCommandSource> dispatcher = event.getDispatcher();
        ReloadDemonCoreTextureCommand.register(dispatcher);
    }

    // Set fog color last to avoid Alex's Caves overriding our fog color
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void fogColorPost(ViewportEvent.ComputeFogColor event) {
        computeFogColor(event, false);
    }

    // Only set alex's caves skycolor
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void fogColor(ViewportEvent.ComputeFogColor event) {
        computeFogColor(event, true);
    }

    // In increasing priority:
    public enum NukeSkyType {NONE, NEUTRON, NUKE, BLACK_HOLE}
}