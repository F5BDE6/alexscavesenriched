package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

public class RocketNuclearItem extends Item implements IRocketItem {
    public RocketNuclearItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.alexscavesenriched.rocket_nuclear.desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public AbstractArrow createRocket(Level level, ItemStack ammoIn, Player player) {
        var e = new RocketEntity(level, player);
        e.setIsNuclear(true);
        return e;
    }
}
