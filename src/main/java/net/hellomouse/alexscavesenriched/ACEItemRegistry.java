package net.hellomouse.alexscavesenriched;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.RadioactiveItem;
import net.hellomouse.alexscavesenriched.item.*;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
    public static final RegistryObject<Item> URANIUM_ARROW = DEF_REG.register("uranium_arrow", UraniumArrowItem::new);
    public static final RegistryObject<Item> URANIUM_CANDY = DEF_REG.register("uranium_candy", UraniumCandyItem::new);
    public static final RegistryObject<Item> RAYGUN = DEF_REG.register("raygun_mk2", RayGunMk2Item::new);
}