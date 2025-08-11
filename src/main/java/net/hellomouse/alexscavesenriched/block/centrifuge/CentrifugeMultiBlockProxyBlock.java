package net.hellomouse.alexscavesenriched.block.centrifuge;

import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeInventoryProxyBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CentrifugeMultiBlockProxyBlock extends BlockWithEntity {
    public static final BooleanProperty IS_TOP = BooleanProperty.of("top");

    public CentrifugeMultiBlockProxyBlock() {
        super(CentrifugeMultiBlockBaseBlock.SETTINGS.hardness(20));
        this.setDefaultState(this.stateManager.getDefaultState().with(IS_TOP, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(IS_TOP);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CentrifugeInventoryProxyBlockEntity proxy) {
            var target = proxy.getTargetPos();
            if (target != null) {
                BlockEntity be2 = level.getBlockEntity(target);
                if (be2 instanceof CentrifugeBlockEntity inv)
                    return inv.getComparatorPowerInput();
            }
        }
        return 0;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && !(newState.getBlock() instanceof CentrifugeMultiBlockProxyBlock)) {
            var be = world.getBlockEntity(pos);
            if (be instanceof CentrifugeInventoryProxyBlockEntity pbe) {
                BlockPos basePos = pbe.getTargetPos();
                if (basePos != null) {
                    world.updateComparators(pos, this);
                    world.removeBlock(basePos, false);
                    return;
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult result) {
        BlockPos.Mutable carve = new BlockPos.Mutable();
        for (int dy = 1; dy < CentrifugeUtil.CENTRIFUGE_HEIGHT; dy++) {
            carve.set(blockPos.getX(), blockPos.getY() - dy, blockPos.getZ());
            BlockState otherState = level.getBlockState(carve);
            if (otherState.getBlock() instanceof CentrifugeMultiBlockBaseBlock)
                return CentrifugeMultiBlockBaseBlock.centrifugeUse(otherState, level, carve, player, hand, result);
        }
        return ActionResult.PASS;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CentrifugeInventoryProxyBlockEntity(pos, state);
    }

    @Override
    public PistonBehavior getPistonPushReaction(BlockState blockState) {
        return PistonBehavior.BLOCK;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> entityType) {
        return checkType(entityType, ACEBlockEntityRegistry.CENTRIFUGE_PROXY.get(), CentrifugeInventoryProxyBlockEntity::tick);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(ACEBlockRegistry.CENTRIFUGE_TOP.get());
    }
}
