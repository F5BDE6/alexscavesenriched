package net.hellomouse.alexscavesenriched.block.fumo;

import net.hellomouse.alexscavesenriched.block.abs.AbstractFumoBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class XiaoyuFumoBlock extends AbstractFumoBlock {
    public XiaoyuFumoBlock() {
        super(Settings.create()
                .mapColor(MapColor.CLEAR)
                .strength(0, 0)
                .sounds(BlockSoundGroup.WOOL)
                .nonOpaque());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        tooltip.add(Text.translatable("block.alexscavesenriched.xiaoyu_fumo.tooltip").formatted(Formatting.GOLD));
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        world.playSoundAtBlockCenter(hit.getBlockPos(), SoundEvents.ENTITY_FOX_HURT, SoundCategory.BLOCKS, 1.0F, 1.0F, true);
    }
}
