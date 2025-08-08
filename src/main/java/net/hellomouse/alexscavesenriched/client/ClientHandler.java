package net.hellomouse.alexscavesenriched.client;

import com.github.alexmodguy.alexscaves.client.render.entity.EmptyRenderer;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import net.hellomouse.alexscavesenriched.*;
import net.hellomouse.alexscavesenriched.client.entity.BlackHoleDiskModel;
import net.hellomouse.alexscavesenriched.client.entity.BlackHoleModel;
import net.hellomouse.alexscavesenriched.client.entity.RocketModel;
import net.hellomouse.alexscavesenriched.client.entity.RocketNuclearModel;
import net.hellomouse.alexscavesenriched.client.particle.*;
import net.hellomouse.alexscavesenriched.client.render.*;
import net.hellomouse.alexscavesenriched.client.render.item.ACEItemRenderProperties;
import net.hellomouse.alexscavesenriched.item.DeadmanSwitchItem;
import net.hellomouse.alexscavesenriched.item.GammaFlashlightItem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AlexsCavesEnriched.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class ClientHandler {
    private static final ACEItemRenderProperties ITEM_RENDER_PROPERTIES = new ACEItemRenderProperties();

    public static ACEItemRenderProperties getItemRenderProperties() { return ITEM_RENDER_PROPERTIES; }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RocketModel.LAYER_LOCATION, RocketModel::createBodyLayer);
        event.registerLayerDefinition(RocketNuclearModel.LAYER_LOCATION, RocketNuclearModel::createBodyLayer);
        event.registerLayerDefinition(BlackHoleModel.LAYER_LOCATION, BlackHoleModel::createBodyLayer);
        event.registerLayerDefinition(BlackHoleDiskModel.LAYER_LOCATION, BlackHoleDiskModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticles(final RegisterParticleProvidersEvent event) {
        AlexsCavesEnriched.LOGGER.debug("Registered particle factories");
        event.registerSpriteSet(ACEParticleRegistry.NUKE_BLAST.get(), NukeBlastParticle.Factory::new);
        event.registerSpriteSet(ACEParticleRegistry.NEUTRON_BLAST.get(), NeutronBlastParticle.Factory::new);
        event.registerSpriteSet(ACEParticleRegistry.DEMONCORE_GLOW.get(), DemonCoreGlowParticle.Factory::new);
        event.registerSpriteSet(ACEParticleRegistry.FLAMETHROWER.get(), FlamethrowerParticle.Factory::new);
        event.registerSpriteSet(ACEParticleRegistry.BLACK_HOLE_SMOKE.get(), BlackHoleSmokeParticle.Factory::new);
        event.registerSpriteSet(ACEParticleRegistry.RAILGUN_SHOCKWAVE.get(), RailgunShockwaveParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ACEEntityRegistry.ROCKET.get(), RocketRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.URANIUM_ARROW.get(), UraniumArrowRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.NUCLEAR_EXPLOSION2.get(), EmptyRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.NEUTRON_EXPLOSION.get(), EmptyRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.NEUTRON_BOMB.get(), NeutronBombRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.MINI_NUKE.get(), MiniNukeRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.BLACK_HOLE_BOMB.get(), BlackHoleBombRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.FLAMETHROWER_PROJECTILE.get(), EmptyRenderer::new);
        event.registerEntityRenderer(ACEEntityRegistry.BLACK_HOLE.get(), BlackHoleRenderer::new);
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

        ModelPredicateProviderRegistry.register(ACEItemRegistry.GAMMA_FLASHLIGHT.get(), Identifier.withDefaultNamespace("active"), (stack, level, living, j) -> GammaFlashlightItem.isOn(stack) ? 1.0F : 0.0F);
        ModelPredicateProviderRegistry.register(ACEItemRegistry.DEADMAN_SWITCH.get(), Identifier.withDefaultNamespace("active"), (stack, level, living, j) -> DeadmanSwitchItem.isActive(stack) ? 1.0F : 0.0F);
        ModelPredicateProviderRegistry.register(ACEItemRegistry.RAYGUN.get(), Identifier.withDefaultNamespace("gamma"), (stack, level, living, j) ->
                stack.getEnchantmentLevel(ACEnchantmentRegistry.GAMMA_RAY.get()) > 0? 1.0F : 0.0F);
    }
}