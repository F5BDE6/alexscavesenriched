package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.entity.UraniumArrowEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class UraniumArrowItem extends ArrowItem {
    public UraniumArrowItem() {
        super(new Item.Settings().rarity(Rarity.UNCOMMON).rarity(ACItemRegistry.RARITY_NUCLEAR));
    }

    @Override
    public @NotNull PersistentProjectileEntity createArrow(World world, ItemStack ammoStack, LivingEntity shooter) {
        return new UraniumArrowEntity(ACEEntityRegistry.URANIUM_ARROW.get(), shooter, world);
    }
}
