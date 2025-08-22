package net.hellomouse.alexscavesenriched;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ACESounds {
    public static final SoundEvent ROCKET_WHISTLE = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "rocket_whistle"), 64.0F);
    public static final SoundEvent BLACKHOLE = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "black_hole"), 64.0F);
    public static final SoundEvent FLAMETHROWER = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "flamethrower"), 32F);
    public static final SoundEvent FLAMETHROWER_EMPTY = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "flamethrower_empty"), 32F);
    public static final SoundEvent FLAMETHROWER_RELOAD = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "flamethrower_reload"), 32F);
    public static final SoundEvent FUMO_SQUEAK = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "fumo_squeak"), 16F);

    public static final SoundEvent RAILGUN_FIRE = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun_fire"), 16F);
    public static final SoundEvent RAILGUN_RELOAD = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun_reload"), 16F);
    public static final SoundEvent RAILGUN_EMPTY = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun_empty"), 16F);
    public static final SoundEvent RAILGUN_CHARGE = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun_charge"), 16F);

    public static final SoundEvent CENTRIFUGE = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "centrifuge"), 32F);
}
