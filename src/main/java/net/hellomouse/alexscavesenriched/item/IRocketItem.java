package net.hellomouse.alexscavesenriched.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IRocketItem {
    AbstractArrow createRocket(Level level, ItemStack ammoIn, Player player);
}
