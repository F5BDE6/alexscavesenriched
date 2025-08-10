package net.hellomouse.alexscavesenriched.block.centrifuge;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

// Base block only for building the multi-block
public class CentrifugeBaseBlock extends Block {
    public CentrifugeBaseBlock() {
        super(CentrifugeMultiBlockBaseBlock.SETTINGS);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
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
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, net.minecraft.item.ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        CentrifugeUtil.assembleMultiBlock(world, pos, false);
    }
}
