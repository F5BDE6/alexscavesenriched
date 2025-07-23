package net.hellomouse.alexscavesenriched;

import net.hellomouse.alexscavesenriched.block.block_entity.EnrichedUraniumBlockEntity;
import net.hellomouse.alexscavesenriched.block.block_entity.EnrichedUraniumRodBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACEBlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AlexsCavesEnriched.MODID);

    public static final RegistryObject<BlockEntityType<EnrichedUraniumRodBlockEntity>> ENRICHED_URANIUM_ROD = DEF_REG.register("enriched_uranium_rod",
            () -> BlockEntityType.Builder.create(
                    EnrichedUraniumRodBlockEntity::new,
                    ACEBlockRegistry.ENRICHED_URANIUM_ROD.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<EnrichedUraniumBlockEntity>> ENRICHED_URANIUM = DEF_REG.register("enriched_uranium", () -> BlockEntityType.Builder.create(EnrichedUraniumBlockEntity::new, ACEBlockRegistry.ENRICHED_URANIUM.get()).build(null));
}
