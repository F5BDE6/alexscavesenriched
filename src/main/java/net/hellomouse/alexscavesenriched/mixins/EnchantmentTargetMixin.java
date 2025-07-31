package net.hellomouse.alexscavesenriched.mixins;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(net.minecraft.enchantment.EnchantmentTarget.class)
public class EnchantmentTargetMixin {
    @Shadow private Predicate<Item> delegate;

    @Inject(at = @At(value = "HEAD"), method = {"isAcceptableItem"}, cancellable = true)
    public void isAcceptableItem(Item item, CallbackInfoReturnable<Boolean> cir) {
        // Allow raygun mk2 to get raygun enchants
        if (this.delegate.test(ACItemRegistry.RAYGUN.get()) && item.equals(ACEItemRegistry.RAYGUN.get())) {
            cir.setReturnValue(true);
        }
    }
}
