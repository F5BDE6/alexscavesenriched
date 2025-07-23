package net.hellomouse.alexscavesenriched;

import com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity;
import net.hellomouse.alexscavesenriched.entity.NuclearExplosion2Entity;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.hellomouse.alexscavesenriched.entity.UraniumArrowEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACEEntityRegistry {
    public static final DeferredRegister<EntityType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AlexsCavesEnriched.MODID);

    public static final RegistryObject<EntityType<RocketEntity>> ROCKET = DEF_REG.register("rocket",
            () -> EntityType.Builder.create((EntityType.EntityFactory<RocketEntity>) RocketEntity::new, SpawnGroup.MISC)
                    .setDimensions(1.1F, 0.5F)
                    .setCustomClientFactory(RocketEntity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .makeFireImmune()
                    .build("rocket"));

    public static final RegistryObject<EntityType<UraniumArrowEntity>> URANIUM_ARROW = DEF_REG.register("uranium_arrow",
            () -> EntityType.Builder.create((EntityType.EntityFactory<UraniumArrowEntity>) UraniumArrowEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.5F, 0.5F)
                    .build("uranium_arrow"));
    public static final RegistryObject<EntityType<NuclearExplosion2Entity>> NUCLEAR_EXPLOSION2 = DEF_REG.register("nuclear_explosion", () ->
            (EntityType.Builder.create((EntityType.EntityFactory<NuclearExplosion2Entity>) NuclearExplosion2Entity::new, SpawnGroup.MISC)
                    .setDimensions(0.99F, 0.99F)
                    .setCustomClientFactory(NuclearExplosion2Entity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .trackingTickInterval(10)
                    .maxTrackingRange(20)
                    .build("nuclear_explosion")));
}
