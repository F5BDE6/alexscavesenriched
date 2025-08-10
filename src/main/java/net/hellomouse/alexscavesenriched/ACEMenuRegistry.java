package net.hellomouse.alexscavesenriched;

import net.hellomouse.alexscavesenriched.inventory.CentrifugeBlockMenu;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACEMenuRegistry {
    public static final DeferredRegister<ScreenHandlerType<?>> DEF_REG =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AlexsCavesEnriched.MODID);

    public static final RegistryObject<ScreenHandlerType<CentrifugeBlockMenu>> CENTRIFUGE =
            DEF_REG.register("centrifuge_menu", () -> new ScreenHandlerType(CentrifugeBlockMenu::new, FeatureFlags.DEFAULT_ENABLED_FEATURES));
}
