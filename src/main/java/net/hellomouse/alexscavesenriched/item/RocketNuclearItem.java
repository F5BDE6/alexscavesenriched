package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import java.util.List;

public class RocketNuclearItem extends Item implements IRocketItem {
    public RocketNuclearItem() {
        super(new Settings()
                .maxCount(1)
                .rarity(Rarity.EPIC));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        tooltip.add(Text.translatable("item.alexscavesenriched.rocket_nuclear.desc").formatted(Formatting.GRAY));
        super.appendTooltip(stack, worldIn, tooltip, flagIn);
    }

    public PersistentProjectileEntity createRocket(World level, ItemStack ammoIn, PlayerEntity player) {
        var e = new RocketEntity(level, player);
        e.setIsNuclear(true);
        return e;
    }
}
