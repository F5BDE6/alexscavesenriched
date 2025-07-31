package net.hellomouse.alexscavesenriched.block;

import net.hellomouse.alexscavesenriched.block.abs.AbstractColaBlock;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;

public class NukaColaEmptyBlock extends AbstractColaBlock {
    public NukaColaEmptyBlock() {
        super(Settings.create()
                .mapColor(MapColor.CLEAR)
                .strength(0, 0)
                .sounds(BlockSoundGroup.GLASS)
                .nonOpaque());
    }
}
