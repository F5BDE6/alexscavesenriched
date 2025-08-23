package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.item.abs.AbstractNukaColaItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class NukaColaQuantumItem extends AbstractNukaColaItem {
    public NukaColaQuantumItem(Block block) {
        super(block, new Properties()
                .stacksTo(16)
                .rarity(Rarity.RARE)
                .food(new FoodProperties.Builder()
                        .nutrition(10).saturationMod(10)
                    .build()
                )
        );
    }

    @Override
    public void affectUser(ItemStack stack, Level world, LivingEntity user) {
        user.removeEffect(MobEffects.POISON);
        user.removeEffect(ACEffectRegistry.IRRADIATED.get());
        user.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                200, 0, false, false, true));
        user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
                200, 1, false, false, true));
    }
}
