package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.entity.UraniumArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class UraniumArrowItem extends ArrowItem {
    public UraniumArrowItem() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).rarity(ACItemRegistry.RARITY_NUCLEAR));
    }

    @Override
    public @NotNull AbstractArrow createArrow(Level world, ItemStack ammoStack, LivingEntity shooter) {
        return new UraniumArrowEntity(ACEEntityRegistry.URANIUM_ARROW.get(), shooter, world);
    }
}
