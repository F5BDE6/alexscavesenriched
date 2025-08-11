package net.hellomouse.alexscavesenriched.block.centrifuge;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// A base block only for building the multi-block
public class CentrifugeTopBlock extends Block {
    public CentrifugeTopBlock() {
        super(CentrifugeMultiBlockBaseBlock.SETTINGS);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public PistonBehavior getPistonPushReaction(BlockState blockState) {
        return PistonBehavior.BLOCK;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, net.minecraft.item.ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        CentrifugeUtil.assembleMultiBlock(world, pos, false);
    }
}
