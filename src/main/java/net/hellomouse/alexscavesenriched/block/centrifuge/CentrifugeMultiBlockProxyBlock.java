package net.hellomouse.alexscavesenriched.block.centrifuge;

import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeInventoryProxyBlockEntity;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class CentrifugeMultiBlockProxyBlock extends BaseEntityBlock {
    public static final BooleanProperty IS_TOP = BooleanProperty.create("top");

    public CentrifugeMultiBlockProxyBlock() {
        super(CentrifugeUtil.getBlockSettings().destroyTime(20));
        this.registerDefaultState(this.stateDefinition.any().setValue(IS_TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_TOP);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
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
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClientSide && !(newState.getBlock() instanceof CentrifugeMultiBlockProxyBlock)) {
            var be = world.getBlockEntity(pos);
            if (be instanceof CentrifugeInventoryProxyBlockEntity pbe) {
                BlockPos basePos = pbe.getTargetPos();
                if (basePos != null) {
                    world.updateNeighbourForOutputSignal(pos, this);
                    world.removeBlock(basePos, false);
                    return;
                }
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult result) {
        BlockPos.MutableBlockPos carve = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy < CentrifugeUtil.CENTRIFUGE_HEIGHT; dy++) {
            carve.set(blockPos.getX(), blockPos.getY() - dy, blockPos.getZ());
            BlockState otherState = level.getBlockState(carve);
            if (otherState.getBlock() instanceof CentrifugeMultiBlockBaseBlock)
                return CentrifugeMultiBlockBaseBlock.centrifugeUse(otherState, level, carve, player, hand, result);
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CentrifugeInventoryProxyBlockEntity(pos, state);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.BLOCK;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, ACEBlockEntityRegistry.CENTRIFUGE_PROXY.get(), CentrifugeInventoryProxyBlockEntity::tick);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(ACEBlockRegistry.CENTRIFUGE_TOP.get());
    }
}
