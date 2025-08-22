package net.hellomouse.alexscavesenriched.block.centrifuge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

// Base block only for building the multi-block
public class CentrifugeBaseBlock extends Block {
    public CentrifugeBaseBlock() {
        super(CentrifugeUtil.getBlockSettings());
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.BLOCK;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        CentrifugeUtil.assembleMultiBlock(world, pos, false);
    }
}
