package net.hellomouse.alexscavesenriched.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRocketItem {
    PersistentProjectileEntity createRocket(World level, ItemStack ammoIn, PlayerEntity player);
}
