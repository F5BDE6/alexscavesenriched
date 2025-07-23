package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class RocketNormalItem extends Item implements IRocketItem {
    public RocketNormalItem() {
        super(new Settings().rarity(Rarity.UNCOMMON));
    }

    public PersistentProjectileEntity createRocket(World level, ItemStack ammoIn, PlayerEntity player) {
        var e = new RocketEntity(level, player);
        e.setExplosionStrength((float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.normal.explosionPower);
        return e;
    }
}
