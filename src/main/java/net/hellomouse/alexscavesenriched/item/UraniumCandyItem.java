package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import javax.annotation.Nullable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import java.util.List;

public class UraniumCandyItem extends Item {
    public UraniumCandyItem() {
        super(new Item.Settings()
                .food(new FoodComponent.Builder()
                        .hunger(200).saturationModifier(200)
                        .effect(() -> new StatusEffectInstance(StatusEffects.NAUSEA, 900, 1), 1.0F)
                        .effect(() -> new StatusEffectInstance(StatusEffects.BLINDNESS, 900, 0), 1.0F)
                        .effect(() -> new StatusEffectInstance(StatusEffects.POISON, 900, 3), 1.0F)
                        .effect(() -> new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 900, 1), 1.0F)
                        .effect(() -> new StatusEffectInstance(StatusEffects.WEAKNESS, 900, 1), 1.0F)
                        .effect(() -> new StatusEffectInstance(StatusEffects.SLOWNESS, 900, 1), 1.0F)
                        .effect(() -> new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(), 80, 5), 1.0F)
                        .build()
                )
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        tooltip.add(Text.translatable("item.alexscavesenriched.uranium_candy.desc").formatted(Formatting.GRAY));
        super.appendTooltip(stack, worldIn, tooltip, flagIn);
    }
}
