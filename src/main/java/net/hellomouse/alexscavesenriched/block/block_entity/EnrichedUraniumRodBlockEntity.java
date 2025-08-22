package net.hellomouse.alexscavesenriched.block.block_entity;

import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EnrichedUraniumRodBlockEntity extends RadiationEmitterBlockEntity{
    public EnrichedUraniumRodBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ACEBlockEntityRegistry.ENRICHED_URANIUM_ROD.get(), p_155229_, p_155230_);
    }
}
