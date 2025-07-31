package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class RocketNeutronItem extends Item implements IRocketItem {
    public RocketNeutronItem() {
        super(new Settings()
                .maxCount(1)
                .rarity(Rarity.EPIC));
    }

    public PersistentProjectileEntity createRocket(World level, ItemStack ammoIn, PlayerEntity player) {
        var e = new RocketEntity(level, player);
        e.setIsNeutron(true);
        return e;
    }
}
