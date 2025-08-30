package net.hellomouse.alexscavesenriched.client;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.mojang.brigadier.CommandDispatcher;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.command.ReloadDemonCoreTextureCommand;
import net.hellomouse.alexscavesenriched.item.GammaFlashlightItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class ACEClientMod {
    // Reuse the submarine light for flashlight :)
    private static final ResourceLocation FLASHLIGHT_SHADER = ResourceLocation.tryBuild(AlexsCavesEnriched.MODID, "shaders/post/flashlight.json");

    // In increasing priority:
    public enum NukeSkyType {NONE, NEUTRON, NUKE, BLACK_HOLE}
    private static final float[] nukeSkyDecayRates = {1, 0.004F, 0.0003F, 0.001F};
    private static final float[] nukeSkyProgressPerType = new float[NukeSkyType.values().length];
    private static int prevLevelHash = -1;

    // Get gradient <start color + alpha, end color + alpha>
    // Alpha color of sky will be progress
    public static Tuple<Vec3, Vec3> NUKE_SKY_GRADIENT = new Tuple<>(new Vec3(0.9, 0.2, 0), new Vec3(0.9, 0.1, 0));

    private static long lastTickTime = 0;
    public static Tuple<Vec3, Vec3> NEUTRON_SKY_GRADIENT = new Tuple<>(new Vec3(0.1, 0.92, 1), new Vec3(0, 0.85, 0.85));
    public static Tuple<Vec3, Vec3> BLACK_HOLE_SKY_GRADIENT = new Tuple<>(new Vec3(0.2, 0.01, 0), new Vec3(1, 0.1, 0));

    // Nuke sky colors
    // ----------------------------------------------------
    private static void resetNukeSkyMaybe(ClientLevel level) {
        var levelHash = level == null ? 0 : level.hashCode();
        if (levelHash != prevLevelHash) {
            Arrays.fill(nukeSkyProgressPerType, 0.0F);
            prevLevelHash = levelHash;
        }
    }

    public static void setNukeSky(NukeSkyType type, float progress) {
        progress = Math.max(0F, Math.min(progress, 1F));
        nukeSkyProgressPerType[type.ordinal()] = Math.max(progress, nukeSkyProgressPerType[type.ordinal()]);
    }

    public static Tuple<Vec3, Vec3> getNukeSkyGradient(NukeSkyType type) {
        switch (type) {
            case NUKE, NONE -> {return NUKE_SKY_GRADIENT;}
            case NEUTRON -> { return NEUTRON_SKY_GRADIENT;}
            case BLACK_HOLE -> { return BLACK_HOLE_SKY_GRADIENT; }
        }
        return NUKE_SKY_GRADIENT; // Never reached
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        Entity player = Minecraft.getInstance().getCameraEntity();
        boolean firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            GameRenderer renderer = Minecraft.getInstance().gameRenderer;

            boolean shouldFlash = false;
            if (firstPerson && player instanceof Player playerE) {
                var stack1 = playerE.getInventory().getSelected();
                var stack2 = playerE.getInventory().offhand.get(0);
                shouldFlash = (stack1.getItem() instanceof GammaFlashlightItem || stack2.getItem() instanceof GammaFlashlightItem) &&
                        (GammaFlashlightItem.isOn(stack1) || GammaFlashlightItem.isOn(stack2));
            }
            if (firstPerson && shouldFlash) {
                if (renderer.currentEffect() == null || !FLASHLIGHT_SHADER.toString().equals(renderer.currentEffect().getName())) {
                    attemptLoadShader(FLASHLIGHT_SHADER);
                }
            } else if (renderer.currentEffect() != null && FLASHLIGHT_SHADER.toString().equals(renderer.currentEffect().getName())) {
                renderer.checkEntityPostEffect(null);
            }
        }
    }

    private static void attemptLoadShader(ResourceLocation resourceLocation) {
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        if (ClientProxy.shaderLoadAttemptCooldown <= 0) {
            renderer.loadEffect(resourceLocation);
            if (!renderer.effectActive) {
                ClientProxy.shaderLoadAttemptCooldown = 12000;
                AlexsCavesEnriched.LOGGER.warn("Alex's Caves Enriched could not load the shader {}, will attempt to load shader in 30 seconds", resourceLocation);
            }
        }
    }

    public static void computeFogColor(ViewportEvent.ComputeFogColor event, boolean first) {
        // Entity player = MinecraftClient.getInstance().player;
        Vec3 startColor = new Vec3(event.getRed(), event.getBlue(), event.getGreen());

        if (event.getCamera().getFluidInCamera() == FogType.NONE && AlexsCavesEnriched.CONFIG.client.overrideSkyColor) {
            var nukeSkyColor = getCurrentNukeSkyColor();
            if (nukeSkyColor.getB() > 0) {
                Vec3 nukeColor = nukeSkyColor.getA();
                float skyAlpha = nukeSkyColor.getB();
                skyAlpha = (float) Math.pow(skyAlpha, 1.2) * 0.9F;

                Vec3 curColor = startColor.add(nukeColor.subtract(startColor).scale(skyAlpha));
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
    public static Tuple<Vec3, Float> getCurrentNukeSkyColor() {
        float totalAmt = 0;
        for (int i = 1; i < NukeSkyType.values().length; i++)
            totalAmt += nukeSkyProgressPerType[i];
        if (totalAmt <= 0)
            return new Tuple<>(new Vec3(0, 0, 0), 0F);

        Vec3 outColor = new Vec3(0, 0, 0);
        float weightedProgress = 0;

        for (int i = 1; i < NukeSkyType.values().length; i++) {
            if (nukeSkyProgressPerType[i] > 0) {
                float progress = Math.min(1F, nukeSkyProgressPerType[i]);
                var grad = getNukeSkyGradient(NukeSkyType.values()[i]);
                Vec3 thisColor = grad.getB().add((grad.getA().subtract(grad.getB())).scale(progress));
                outColor = outColor.add(thisColor.scale(nukeSkyProgressPerType[i] / totalAmt));
                weightedProgress += nukeSkyProgressPerType[i] * (nukeSkyProgressPerType[i] / totalAmt);
            }
        }
        return new Tuple<>(outColor, weightedProgress);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (event.phase == TickEvent.Phase.START && level != null) {
            if (lastTickTime == level.getGameTime())
                return;
            lastTickTime = level.getGameTime();
            resetNukeSkyMaybe(level);

            // Decay nuke sky glow even when nuke is not loaded
            for (int i = NukeSkyType.values().length - 1; i > 0; i--)
                nukeSkyProgressPerType[i] = Math.max(0F, nukeSkyProgressPerType[i] - nukeSkyDecayRates[i]);

            // Spawn nuke ash particles

            LocalPlayer player = mc.player;
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
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
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
}