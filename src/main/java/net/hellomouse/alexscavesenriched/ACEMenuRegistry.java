package net.hellomouse.alexscavesenriched;

import net.hellomouse.alexscavesenriched.inventory.CentrifugeBlockMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACEMenuRegistry {
    public static final DeferredRegister<MenuType<?>> DEF_REG =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AlexsCavesEnriched.MODID);

    public static final RegistryObject<MenuType<CentrifugeBlockMenu>> CENTRIFUGE =
            DEF_REG.register("centrifuge_menu", () -> new MenuType(CentrifugeBlockMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
