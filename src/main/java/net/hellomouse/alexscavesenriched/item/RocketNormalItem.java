package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class RocketNormalItem extends Item implements IRocketItem {
    public RocketNormalItem() {
        super(new Properties().rarity(Rarity.UNCOMMON));
    }

    public boolean allowInfinity() {
        return true;
    }

    public AbstractArrow createRocket(Level level, ItemStack ammoIn, Player player) {
        var e = new RocketEntity(level, player);
        e.setExplosionStrength((float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.normal.explosionPower);
        return e;
    }
}
