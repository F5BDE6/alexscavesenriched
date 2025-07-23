package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class RocketItem extends Item implements IRocketItem {
    public RocketItem() {
        super(new Item.Settings().rarity(Rarity.UNCOMMON).rarity(ACItemRegistry.RARITY_NUCLEAR));
    }

    public PersistentProjectileEntity createRocket(World level, ItemStack ammoIn, PlayerEntity player) {
        var e = new RocketEntity(level, player);
        e.setIsRadioactive(true);
        e.setExplosionStrength((float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.explosionPower);
        return e;
    }
}
