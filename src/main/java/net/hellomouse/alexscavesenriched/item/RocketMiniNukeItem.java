package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class RocketMiniNukeItem extends Item implements IRocketItem {
    public RocketMiniNukeItem() {
        super(new Settings()
                .maxCount(16)
                .rarity(Rarity.EPIC));
    }

    public PersistentProjectileEntity createRocket(World level, ItemStack ammoIn, PlayerEntity player) {
        var e = new RocketEntity(level, player);
        e.setIsMiniNuke(true);
        return e;
    }
}
