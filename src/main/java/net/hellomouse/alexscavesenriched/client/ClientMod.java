package net.hellomouse.alexscavesenriched.client;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.item.GammaFlashlightItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class ClientMod {
    // Reuse the submarine light for flashlight :)
    private static final Identifier FLASHLIGHT_SHADER = Identifier.of(AlexsCavesEnriched.MODID, "shaders/post/flashlight.json");

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
}