package net.hellomouse.alexscavesenriched.mixins.items;

import net.hellomouse.alexscavesenriched.item.RocketLauncherItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.FlameEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

// Allow flame to be applied to rocket launcher
@Mixin(FlameEnchantment.class)
public abstract class ArrowFireEnchantmentMixin extends Enchantment {
    public ArrowFireEnchantmentMixin(Enchantment.Rarity p_44576_, EquipmentSlot... p_44577_) {
        super(p_44576_, EnchantmentTarget.BOW, p_44577_);
    }

    public boolean isAcceptableItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof RocketLauncherItem)
            return true;
        return super.isAcceptableItem(itemStack);
    }
}
