package net.hellomouse.alexscavesenriched;

import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.hellomouse.alexscavesenriched.advancements.ACECriterionTriggers;
import net.hellomouse.alexscavesenriched.item.ACEDispenserItemBehavior;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AlexsCavesEnriched.MODID)
public class AlexsCavesEnriched {
    public static final String MODID = "alexscavesenriched";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Block> NEUTRONREFLECTOR_TAG = BlockTags.create(Identifier.fromNamespaceAndPath(MODID, "neutron_reflector"));
    public static final TagKey<Block> WEAK_PLANTS_TAG = BlockTags.create(Identifier.fromNamespaceAndPath(MODID, "weak_plants"));
    public static final TagKey<Item> FLAMETHROWER_FUEL_TAG = ItemTags.create(Identifier.fromNamespaceAndPath(MODID, "flamethrower_fuel"));

    public static ACEConfig CONFIG;
    public static final DeferredRegister<ItemGroup> CREATIVE_TAB_REG = DeferredRegister.create(RegistryKeys.ITEM_GROUP, MODID);
    public static final RegistryObject<ItemGroup> CREATIVE_TAB_ACE = CREATIVE_TAB_REG.register(MODID, () -> ItemGroup.builder()
            .displayName(Text.translatable("itemGroup." + MODID + ".creative_tab"))
            .icon(() -> new ItemStack(ACEBlockRegistry.ENRICHED_URANIUM_ROD.get()))
            .withTabsBefore(ItemGroups.SPAWN_EGGS)
            .entries((enabledFeatures, output) -> {
                output.add(ACEBlockRegistry.ENRICHED_URANIUM.get());
                output.add(ACEBlockRegistry.ENRICHED_URANIUM_ROD.get());
                output.add(ACEItemRegistry.ENRICHED_URANIUM.get());
                output.add(ACEItemRegistry.ROCKET_LAUNCHER.get());
                output.add(ACEItemRegistry.ROCKET_NORMAL.get());
                output.add(ACEItemRegistry.ROCKET.get());
                output.add(ACEItemRegistry.ROCKET_NUCLEAR.get());
                output.add(ACEItemRegistry.ROCKET_NEUTRON.get());
                output.add(ACEItemRegistry.ROCKET_MINI_NUKE.get());
                output.add(ACEItemRegistry.URANIUM_ARROW.get());
                output.add(ACEItemRegistry.URANIUM_CANDY.get());
                output.add(ACEItemRegistry.RAYGUN.get());
                output.add(ACEItemRegistry.RAYGUN_UPGRADE_TEMPLATE.get());
                output.add(ACEItemRegistry.FLAMETHROWER.get());
                output.add(ACEBlockRegistry.MINI_NUKE.get());
                output.add(ACEBlockRegistry.NEUTRON_BOMB.get());
                output.add(ACEBlockRegistry.BLACK_HOLE_BOMB.get());
                output.add(ACEItemRegistry.DEADMAN_SWITCH.get());
                output.add(ACEItemRegistry.GAMMA_FLASHLIGHT.get());
                output.add(ACEItemRegistry.NUKA_COLA.get());
                output.add(ACEItemRegistry.NUKA_COLA_QUANTUM.get());
                output.add(ACEItemRegistry.NUKA_COLA_EMPTY.get());
                output.add(ACEItemRegistry.RAILGUN.get());
                output.add(ACEItemRegistry.RAILGUN_AMMO.get());
            })
            .build());

    public AlexsCavesEnriched(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        AutoConfig.register(ACEConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ACEConfig.class).getConfig();
        modEventBus.addListener(this::commonSetup);
        ACERecipeRegistry.DEF_REG.register(modEventBus);
        ACERecipeRegistry.TYPE_DEF_REG.register(modEventBus);
        ACEEntityRegistry.DEF_REG.register(modEventBus);
        ACEBlockRegistry.DEF_REG.register(modEventBus);
        ACEItemRegistry.DEF_REG.register(modEventBus);
        ACEBlockEntityRegistry.DEF_REG.register(modEventBus);
        ACEEffectRegistry.DEF_REG.register(modEventBus);
        ACEParticleRegistry.DEF_REG.register(modEventBus);

        CREATIVE_TAB_REG.register(modEventBus);

        modEventBus.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ACEDispenserItemBehavior.bootStrap();
        ACECriterionTriggers.init();
        LOGGER.info("Alex's Caves Enriched has loaded");
    }
}
