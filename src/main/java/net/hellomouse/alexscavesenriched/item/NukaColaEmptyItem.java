package net.hellomouse.alexscavesenriched.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class NukaColaEmptyItem extends BlockItem {
    public NukaColaEmptyItem(Block block) {
        super(block, new Properties().stacksTo(16));
    }
}
