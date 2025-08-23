package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class RocketNeutronItem extends Item implements IRocketItem {
    public RocketNeutronItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC));
    }

    public AbstractArrow createRocket(Level level, ItemStack ammoIn, Player player) {
        var e = new RocketEntity(level, player);
        e.setIsNeutron(true);
        return e;
    }
}
