package net.hellomouse.alexscavesenriched.block.fumo;

import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.block.abs.AbstractFumoBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import java.util.List;

public class XenoFumoBlock extends AbstractFumoBlock {
    public XenoFumoBlock() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0, 0)
                .sound(ACSoundTypes.SCRAP_METAL)
                .noOcclusion());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options) {
        super.appendHoverText(stack, world, tooltip, options);
        tooltip.add(Component.translatable("block.alexscavesenriched.xeno_fumo.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        world.playLocalSound(hit.getBlockPos(), SoundEvents.PANDA_HURT, SoundSource.BLOCKS, 1.0F, 1.0F, true);
    }
}
