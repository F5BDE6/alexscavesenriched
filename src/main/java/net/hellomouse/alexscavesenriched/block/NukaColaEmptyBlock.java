package net.hellomouse.alexscavesenriched.block;

import net.hellomouse.alexscavesenriched.block.abs.AbstractColaBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class NukaColaEmptyBlock extends AbstractColaBlock {
    public NukaColaEmptyBlock() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0, 0)
                .sound(SoundType.GLASS)
                .noOcclusion());
    }
}
