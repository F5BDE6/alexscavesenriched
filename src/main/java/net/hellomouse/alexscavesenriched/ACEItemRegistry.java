package net.hellomouse.alexscavesenriched;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.RadioactiveItem;
import net.hellomouse.alexscavesenriched.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ACEItemRegistry {
    public static final DeferredRegister<Item> DEF_REG = DeferredRegister.create(ForgeRegistries.ITEMS, AlexsCavesEnriched.MODID);

    public static final RegistryObject<Item> ENRICHED_URANIUM = DEF_REG.register("enriched_uranium",
            () -> new RadioactiveItem(new Item.Settings()
                    .rarity(Rarity.UNCOMMON)
                    .fireproof()
                    .rarity(ACItemRegistry.RARITY_NUCLEAR), 0.001F)
    );

    public static final RegistryObject<Item> ROCKET_LAUNCHER = DEF_REG.register("rocket_launcher", RocketLauncherItem::new);
    public static final RegistryObject<Item> ROCKET = DEF_REG.register("rocket", RocketItem::new);
    public static final RegistryObject<Item> ROCKET_NORMAL = DEF_REG.register("rocket_normal", RocketNormalItem::new);
    public static final RegistryObject<Item> ROCKET_NUCLEAR = DEF_REG.register("rocket_nuclear", RocketNuclearItem::new);
    public static final RegistryObject<Item> ROCKET_NEUTRON = DEF_REG.register("rocket_neutron", RocketNeutronItem::new);
    public static final RegistryObject<Item> URANIUM_ARROW = DEF_REG.register("uranium_arrow", UraniumArrowItem::new);
    public static final RegistryObject<Item> URANIUM_CANDY = DEF_REG.register("uranium_candy", UraniumCandyItem::new);
    public static final RegistryObject<Item> NUKA_COLA = DEF_REG.register("nuka_cola", () -> new NukaColaItem(ACEBlockRegistry.NUKA_COLA.get()));
    public static final RegistryObject<Item> NUKA_COLA_QUANTUM = DEF_REG.register("nuka_cola_quantum", () -> new NukaColaQuantumItem(ACEBlockRegistry.NUKA_COLA_QUANTUM.get()));
    public static final RegistryObject<Item> NUKA_COLA_EMPTY = DEF_REG.register("nuka_cola_empty", () -> new NukaColaEmptyItem(ACEBlockRegistry.NUKA_COLA_EMPTY.get()));
    public static final RegistryObject<Item> RAYGUN = DEF_REG.register("raygun_mk2", RayGunMk2Item::new);
    public static final RegistryObject<Item> RAYGUN_UPGRADE_TEMPLATE = DEF_REG.register("raygun_upgrade_template",
            () -> new SmithingTemplateItem(
                    Text.translatable("item.alexscavesenriched.raygun_upgrade_template.applies_to"),
                    Text.translatable("item.alexscavesenriched.raygun_upgrade_template.ingredients"),
                    Text.translatable("item.alexscavesenriched.raygun_upgrade_template.title"),
                    Text.translatable("item.alexscavesenriched.raygun_upgrade_template.base_slot_description"),
                    Text.translatable("item.alexscavesenriched.raygun_upgrade_template.additions_slot_description"),
                    List.of(),
                    List.of()
            )
    );
    public static final RegistryObject<Item> FLAMETHROWER = DEF_REG.register("flamethrower", FlamethrowerItem::new);
    public static final RegistryObject<Item> DEADMAN_SWITCH = DEF_REG.register("deadmans_switch", DeadmanSwitchItem::new);
    public static final RegistryObject<Item> GAMMA_FLASHLIGHT = DEF_REG.register("gamma_flashlight", GammaFlashlightItem::new);
}