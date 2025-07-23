package net.hellomouse.alexscavesenriched;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ACEParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> DEF_REG = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.PARTICLE_TYPES, AlexsCavesEnriched.MODID);

    public static final RegistryObject<DefaultParticleType> NUKE_BLAST = DEF_REG.register("nuke_blast", () -> new DefaultParticleType(true));
    public static final RegistryObject<DefaultParticleType> DEMONCORE_GLOW = DEF_REG.register("demon_core", () -> new DefaultParticleType(true));
}
