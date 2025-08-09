package net.hellomouse.alexscavesenriched.mixins.items;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.item.RocketLauncherItem;
import net.minecraft.enchantment.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements IForgeEnchantment {
    @Inject(at = @At(value = "HEAD"), method = {"isAcceptableItem"}, cancellable = true)
    public void isAcceptableItem(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        Enchantment enchant = (Enchantment)((Object)this);
        if (itemStack.getItem() instanceof RocketLauncherItem) {
            if (enchant instanceof PowerEnchantment || enchant instanceof FlameEnchantment)
                cir.setReturnValue(true);
        }
        else if (itemStack.getItem() instanceof RocketLauncherItem) {
            if (enchant instanceof InfinityEnchantment && AlexsCavesEnriched.CONFIG.railgun.infinity)
                cir.setReturnValue(true);
            else if (enchant instanceof MultishotEnchantment && AlexsCavesEnriched.CONFIG.railgun.multishot)
                cir.setReturnValue(true);
            else if (enchant instanceof QuickChargeEnchantment && AlexsCavesEnriched.CONFIG.railgun.quickCharge)
                cir.setReturnValue(true);
        }
    }
}
