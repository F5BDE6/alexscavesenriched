package net.hellomouse.alexscavesenriched;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ACESounds {
    public static final SoundEvent ROCKET_WHISTLE = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "rocket_whistle"), 64.0F);
    public static final SoundEvent BLACKHOLE = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "black_hole"), 64.0F);
    public static final SoundEvent FLAMETHROWER = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "flamethrower"), 32F);
    public static final SoundEvent FLAMETHROWER_EMPTY = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "flamethrower_empty"), 32F);
    public static final SoundEvent FLAMETHROWER_RELOAD = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "flamethrower_reload"), 32F);
    public static final SoundEvent FUMO_SQUEAK = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "fumo_squeak"), 16F);

    public static final SoundEvent RAILGUN_FIRE = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun_fire"), 16F);
    public static final SoundEvent RAILGUN_RELOAD = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun_reload"), 16F);
    public static final SoundEvent RAILGUN_EMPTY = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun_empty"), 16F);
    public static final SoundEvent RAILGUN_CHARGE = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "railgun_charge"), 16F);

    public static final SoundEvent CENTRIFUGE = SoundEvent.of(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "centrifuge"), 32F);
}
