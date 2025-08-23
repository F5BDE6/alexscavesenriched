package net.hellomouse.alexscavesenriched.block;

import net.hellomouse.alexscavesenriched.block.abs.AbstractColaBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class NukaColaQuantumBlock extends AbstractColaBlock {
    public NukaColaQuantumBlock() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .lightLevel(_blockState -> 8)
                .strength(0, 0)
                .sound(SoundType.GLASS)
                .noOcclusion());
    }
}
