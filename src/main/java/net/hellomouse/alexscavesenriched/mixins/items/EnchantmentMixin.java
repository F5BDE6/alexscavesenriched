package net.hellomouse.alexscavesenriched.mixins.items;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.item.RocketLauncherItem;
import net.minecraft.enchantment.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements IForgeEnchantment {
    @ModifyReturnValue(at = @At("RETURN"), method = "isAcceptableItem")
    public boolean isAcceptableItem(boolean original, ItemStack itemStack) {
        Enchantment enchant = (Enchantment) ((Object) this);
        if (itemStack.getItem() instanceof RocketLauncherItem) {
            if (enchant instanceof PowerEnchantment || enchant instanceof FlameEnchantment)
                return true;
        } else if (itemStack.getItem() instanceof RocketLauncherItem) {
            if (enchant instanceof InfinityEnchantment && AlexsCavesEnriched.CONFIG.railgun.infinity)
                return true;
            else if (enchant instanceof MultishotEnchantment && AlexsCavesEnriched.CONFIG.railgun.multishot)
                return true;
            else if (enchant instanceof QuickChargeEnchantment && AlexsCavesEnriched.CONFIG.railgun.quickCharge)
                return true;
        }
        return original;
    }
}
