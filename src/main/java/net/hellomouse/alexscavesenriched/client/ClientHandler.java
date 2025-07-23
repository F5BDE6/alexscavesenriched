package net.hellomouse.alexscavesenriched.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.hellomouse.alexscavesenriched.*;
import net.hellomouse.alexscavesenriched.client.entity.RocketModel;
import net.hellomouse.alexscavesenriched.client.entity.RocketNuclearModel;
import net.hellomouse.alexscavesenriched.client.particle.DemonCoreGlowParticle;
import net.hellomouse.alexscavesenriched.client.particle.NukeBlastParticle;
import net.hellomouse.alexscavesenriched.client.render.ACEInternalShaders;
import net.hellomouse.alexscavesenriched.client.render.RocketRenderer;
import net.hellomouse.alexscavesenriched.client.render.UraniumArrowRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import com.github.alexmodguy.alexscaves.client.render.entity.EmptyRenderer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class ClientHandler {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RocketModel.LAYER_LOCATION, RocketModel::createBodyLayer);
        event.registerLayerDefinition(RocketNuclearModel.LAYER_LOCATION, RocketNuclearModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticles(final RegisterParticleProvidersEvent event) {
        AlexsCavesEnriched.LOGGER.debug("Registered particle factories");
        event.registerSpriteSet(ACEParticleRegistry.NUKE_BLAST.get(), NukeBlastParticle.Factory::new);
        event.registerSpriteSet(ACEParticleRegistry.DEMONCORE_GLOW.get(), DemonCoreGlowParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ACEEntityRegistry.ROCKET.get(), RocketRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.URANIUM_ARROW.get(), UraniumArrowRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.NUCLEAR_EXPLOSION2.get(), EmptyRenderer::new);
    }

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent e) {
        try {
            e.registerShader(new ShaderProgram(e.getResourceProvider(),
                    Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "radiation_particle"),
                    VertexFormats.POSITION_TEXTURE_COLOR_LIGHT), ACEInternalShaders::setRadiationParticleShader);
            AlexsCavesEnriched.LOGGER.info("Registered AlexsCavesEnriched internal shaders");
        } catch (IOException exception) {
            AlexsCavesEnriched.LOGGER.error("could not register internal shaders");
            exception.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onFMLClientSetupEvent(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, prevScreen) -> AutoConfig.getConfigScreen(ACEConfig.class, prevScreen).get())
        );
    }
}