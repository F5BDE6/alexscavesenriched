package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class RocketMiniNukeItem extends Item implements IRocketItem {
    public RocketMiniNukeItem() {
        super(new Properties()
                .stacksTo(16)
                .rarity(Rarity.EPIC));
    }

    public AbstractArrow createRocket(Level level, ItemStack ammoIn, Player player) {
        var e = new RocketEntity(level, player);
        e.setIsMiniNuke(true);
        return e;
    }
}
