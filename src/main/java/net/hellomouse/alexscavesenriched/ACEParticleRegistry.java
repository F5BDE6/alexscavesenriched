package net.hellomouse.alexscavesenriched;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ACEParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> DEF_REG = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.PARTICLE_TYPES, AlexsCavesEnriched.MODID);

    public static final RegistryObject<SimpleParticleType> NUKE_BLAST = DEF_REG.register("nuke_blast", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> NEUTRON_BLAST = DEF_REG.register("neutron_blast", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> DEMONCORE_GLOW = DEF_REG.register("demon_core_glow", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FLAMETHROWER = DEF_REG.register("flamethrower_flame", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BLACK_HOLE_SMOKE = DEF_REG.register("black_hole_smoke", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RAILGUN_SHOCKWAVE = DEF_REG.register("railgun_shockwave", () -> new SimpleParticleType(true));
}
