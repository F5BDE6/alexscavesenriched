package net.hellomouse.alexscavesenriched.block.centrifuge;

import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class CentrifugeMultiBlockBaseBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CentrifugeMultiBlockBaseBlock() {
        super(CentrifugeUtil.getBlockSettings()
                .destroyTime(20)
                .lightLevel(state -> {
                    if (!(state.getBlock() instanceof CentrifugeMultiBlockBaseBlock))
                        return 0;
                    return state.getValue(POWERED) ? 7 : 0;
                }));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    public static InteractionResult centrifugeUse(BlockState state, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!player.isShiftKeyDown()) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;

            BlockEntity blockentity = level.getBlockEntity(blockPos);
            if (blockentity instanceof CentrifugeBlockEntity centrifugeBlockEntity) {
                if (!AlexsCavesEnriched.CONFIG.centrifuge.cantInteractWithActive || centrifugeBlockEntity.getSpinSpeed() <= 0)
                    player.openMenu(centrifugeBlockEntity);
                else
                    player.displayClientMessage(Component.translatable("block.alexscavesenriched.centrifuge.cannot_open").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CentrifugeBlockEntity inv)
            return inv.getComparatorPowerOutput();
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClientSide) {
            boolean powered = world.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED))
                world.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.BLOCK;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, ACEBlockEntityRegistry.CENTRIFUGE.get(), CentrifugeBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClientSide && !(newState.getBlock() instanceof CentrifugeMultiBlockBaseBlock)) {
            if (world instanceof ServerLevel serverWorld)
                if (world.getBlockEntity(pos) instanceof Container inv)
                    Containers.dropContents(serverWorld, pos, inv);

            world.updateNeighbourForOutputSignal(pos, this);
            CentrifugeUtil.breakMultiBlockFromBase(world, pos, true);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult result) {
        return centrifugeUse(state, level, blockPos, player, hand, result);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(ACEBlockRegistry.CENTRIFUGE_BASE.get());
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }
}
