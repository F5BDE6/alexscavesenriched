package net.hellomouse.alexscavesenriched.mixins.items;

import net.hellomouse.alexscavesenriched.item.RocketLauncherItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.PowerEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

// Allow power to be applied to rocket launcher
// makes rockets fly faster
@Mixin(PowerEnchantment.class)
public abstract class ArrowDamageEnchantmentMixin extends Enchantment {
    public ArrowDamageEnchantmentMixin(Rarity p_44576_, EquipmentSlot... p_44577_) {
        super(p_44576_, EnchantmentTarget.BOW, p_44577_);
    }

    public boolean isAcceptableItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof RocketLauncherItem)
            return true;
        return super.isAcceptableItem(itemStack);
    }
}
