package net.hellomouse.alexscavesenriched.block;

import net.hellomouse.alexscavesenriched.block.abs.AbstractColaBlock;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;

public class NukaColaQuantumBlock extends AbstractColaBlock {
    public NukaColaQuantumBlock() {
        super(Settings.create()
                .mapColor(MapColor.CLEAR)
                .luminance(_blockState -> 8)
                .strength(0, 0)
                .sounds(BlockSoundGroup.GLASS)
                .nonOpaque());
    }
}
