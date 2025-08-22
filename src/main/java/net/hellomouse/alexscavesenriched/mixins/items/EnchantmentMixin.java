package net.hellomouse.alexscavesenriched.mixins.items;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.item.RocketLauncherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ArrowDamageEnchantment;
import net.minecraft.world.item.enchantment.ArrowFireEnchantment;
import net.minecraft.world.item.enchantment.ArrowInfiniteEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.MultiShotEnchantment;
import net.minecraft.world.item.enchantment.QuickChargeEnchantment;
import net.minecraftforge.common.extensions.IForgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements IForgeEnchantment {
    @ModifyReturnValue(at = @At("RETURN"), method = "isAcceptableItem")
    public boolean isAcceptableItem(boolean original, ItemStack itemStack) {
        Enchantment enchant = (Enchantment) ((Object) this);
        if (itemStack.getItem() instanceof RocketLauncherItem) {
            if (enchant instanceof ArrowDamageEnchantment || enchant instanceof ArrowFireEnchantment)
                return true;
        }
        else if (itemStack.getItem() instanceof RocketLauncherItem) {
            if (enchant instanceof ArrowInfiniteEnchantment && AlexsCavesEnriched.CONFIG.railgun.infinity)
                return true;
            else if (enchant instanceof MultiShotEnchantment && AlexsCavesEnriched.CONFIG.railgun.multishot)
                return true;
            else if (enchant instanceof QuickChargeEnchantment && AlexsCavesEnriched.CONFIG.railgun.quickCharge)
                return true;
        }
        return original;
    }
}
