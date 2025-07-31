package net.hellomouse.alexscavesenriched.block;

import net.hellomouse.alexscavesenriched.block.abs.AbstractColaBlock;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;

public class NukaColaBlock extends AbstractColaBlock {
    public NukaColaBlock() {
        super(Settings.create()
                .mapColor(MapColor.CLEAR)
                .luminance(_blockState -> 1)
                .strength(0, 0)
                .sounds(BlockSoundGroup.GLASS)
                .nonOpaque());
    }
}
