package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.item.abs.AbstractNukaColaItem;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class NukaColaQuantumItem extends AbstractNukaColaItem {
    public NukaColaQuantumItem(Block block) {
        super(block, new Settings()
                .maxCount(16)
                .rarity(Rarity.RARE)
                .food(new FoodComponent.Builder()
                    .hunger(10).saturationModifier(10)
                    .build()
                )
        );
    }

    @Override
    public void affectUser(ItemStack stack, World world, LivingEntity user) {
        user.removeStatusEffect(StatusEffects.POISON);
        user.removeStatusEffect(ACEffectRegistry.IRRADIATED.get());
        user.addStatusEffect(new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                200, 0, false, false, true));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,
                200, 1, false, false, true));
    }
}
