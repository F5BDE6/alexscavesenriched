package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public class UraniumCandyItem extends Item {
    public UraniumCandyItem() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(200).saturationMod(200)
                        .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 900, 1), 1.0F)
                        .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 900, 0), 1.0F)
                        .effect(() -> new MobEffectInstance(MobEffects.POISON, 900, 3), 1.0F)
                        .effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 900, 1), 1.0F)
                        .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 900, 1), 1.0F)
                        .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 900, 1), 1.0F)
                        .effect(() -> new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(), 80, 5), 1.0F)
                        .build()
                )
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.alexscavesenriched.uranium_candy.desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
