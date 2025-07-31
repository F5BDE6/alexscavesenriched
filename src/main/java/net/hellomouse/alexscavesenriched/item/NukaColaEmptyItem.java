package net.hellomouse.alexscavesenriched.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class NukaColaEmptyItem extends BlockItem {
    public NukaColaEmptyItem(Block block) {
        super(block, new Settings().maxCount(16));
    }
}
