package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class RocketItem extends Item implements IRocketItem {
    public RocketItem() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).rarity(ACItemRegistry.RARITY_NUCLEAR));
    }

    public AbstractArrow createRocket(Level level, ItemStack ammoIn, Player player) {
        var e = new RocketEntity(level, player);
        e.setIsRadioactive(true);
        e.setExplosionStrength((float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.explosionPower);
        return e;
    }
}
