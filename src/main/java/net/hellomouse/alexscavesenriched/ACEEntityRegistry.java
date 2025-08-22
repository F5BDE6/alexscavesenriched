package net.hellomouse.alexscavesenriched;

import com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity;
import net.hellomouse.alexscavesenriched.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACEEntityRegistry {
    public static final DeferredRegister<EntityType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AlexsCavesEnriched.MODID);

    public static final RegistryObject<EntityType<UraniumArrowEntity>> URANIUM_ARROW = DEF_REG.register("uranium_arrow",
            () -> EntityType.Builder.of((EntityType.EntityFactory<UraniumArrowEntity>) UraniumArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("uranium_arrow"));    public static final RegistryObject<EntityType<RocketEntity>> ROCKET = DEF_REG.register("rocket",
            () -> EntityType.Builder.of((EntityType.EntityFactory<RocketEntity>) RocketEntity::new, MobCategory.MISC)
                    .sized(1.1F, 0.5F)
                    .setCustomClientFactory(RocketEntity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .fireImmune()
                    .build("rocket"));


    public static final RegistryObject<EntityType<NuclearExplosion2Entity>> NUCLEAR_EXPLOSION2 = DEF_REG.register("nuclear_explosion", () ->
            (EntityType.Builder.of((EntityType.EntityFactory<NuclearExplosion2Entity>) NuclearExplosion2Entity::new, MobCategory.MISC)
                    .sized(0.99F, 0.99F)
                    .setCustomClientFactory(NuclearExplosion2Entity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .updateInterval(10)
                    .clientTrackingRange(20)
                    .build("nuclear_explosion")));
    public static final RegistryObject<EntityType<NeutronExplosionEntity>> NEUTRON_EXPLOSION = DEF_REG.register("neutron_explosion", () ->
            (EntityType.Builder.of((EntityType.EntityFactory<NeutronExplosionEntity>) NeutronExplosionEntity::new, MobCategory.MISC)
                    .sized(0.99F, 0.99F)
                    .setCustomClientFactory(NeutronExplosionEntity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .updateInterval(10)
                    .clientTrackingRange(20)
                    .build("neutron_explosion")));
    public static final RegistryObject<EntityType<BlackHoleEntity>> BLACK_HOLE = DEF_REG.register("black_hole", () ->
            (EntityType.Builder.of((EntityType.EntityFactory<BlackHoleEntity>) BlackHoleEntity::new, MobCategory.MISC)
                    .sized(0.99F, 0.99F)
                    .setCustomClientFactory(BlackHoleEntity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .updateInterval(10)
                    .clientTrackingRange(20)
                    .build("black_hole")));
    public static final RegistryObject<EntityType<MiniNukeEntity>> MINI_NUKE = DEF_REG.register("mini_nuke", () ->
            (EntityType.Builder.of((EntityType.EntityFactory<MiniNukeEntity>) MiniNukeEntity::new, MobCategory.MISC)
                    .sized(0.7F, 0.7F)
                    .setCustomClientFactory(MiniNukeEntity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .updateInterval(10)
                    .clientTrackingRange(20)
                    .build("mini_nuke")));
    public static final RegistryObject<EntityType<NeutronBombEntity>> NEUTRON_BOMB = DEF_REG.register("neutron_bomb", () ->
            (EntityType.Builder.of((EntityType.EntityFactory<NeutronBombEntity>) NeutronBombEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .setCustomClientFactory(NeutronBombEntity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .updateInterval(10)
                    .clientTrackingRange(20)
                    .build("neutron_bomb")));
    public static final RegistryObject<EntityType<BlackHoleBombEntity>> BLACK_HOLE_BOMB = DEF_REG.register("black_hole_bomb", () ->
            (EntityType.Builder.of((EntityType.EntityFactory<BlackHoleBombEntity>) BlackHoleBombEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .setCustomClientFactory(BlackHoleBombEntity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .updateInterval(10)
                    .clientTrackingRange(20)
                    .build("black_hole_bomb")));
    public static final RegistryObject<EntityType<FlamethrowerProjectileEntity>> FLAMETHROWER_PROJECTILE = DEF_REG.register("flamethrower_projectile", () ->
            (EntityType.Builder.of((EntityType.EntityFactory<FlamethrowerProjectileEntity>) FlamethrowerProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setCustomClientFactory(FlamethrowerProjectileEntity::new)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .updateInterval(10)
                    .clientTrackingRange(64)
                    .build("flamethrower_projectile")));
}
