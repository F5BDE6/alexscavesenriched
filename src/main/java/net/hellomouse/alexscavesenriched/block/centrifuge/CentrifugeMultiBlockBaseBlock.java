package net.hellomouse.alexscavesenriched.block.centrifuge;

import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CentrifugeMultiBlockBaseBlock extends BlockWithEntity {
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final AbstractBlock.Settings SETTINGS = AbstractBlock.Settings.create()
            .mapColor(MapColor.IRON_GRAY)
            .requiresTool()
            .nonOpaque()
            .solid()
            .strength(4, 3)
            .sounds(ACSoundTypes.METAL_SCAFFOLDING);

    public CentrifugeMultiBlockBaseBlock() {
        super(SETTINGS
                .hardness(20)
                .luminance(state -> {
                    if (!(state.getBlock() instanceof CentrifugeMultiBlockBaseBlock))
                        return 0;
                    return state.get(POWERED) ? 7 : 0;
                }));
        this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, false));
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CentrifugeBlockEntity inv)
            return inv.getComparatorPowerOutput();
        return 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            boolean powered = world.isReceivingRedstonePower(pos);
            if (powered != state.get(POWERED))
                world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_ALL);
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Override
    public PistonBehavior getPistonPushReaction(BlockState blockState) {
        return PistonBehavior.BLOCK;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> entityType) {
        return checkType(entityType, ACEBlockEntityRegistry.CENTRIFUGE.get(), CentrifugeBlockEntity::tick);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && !(newState.getBlock() instanceof CentrifugeMultiBlockBaseBlock)) {
            if (world instanceof ServerWorld serverWorld)
                if (world.getBlockEntity(pos) instanceof Inventory inv)
                    ItemScatterer.spawn(serverWorld, pos, inv);

            world.updateComparators(pos, this);
            CentrifugeUtil.breakMultiBlockFromBase(world, pos, true);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult result) {
        return centrifugeUse(state, level, blockPos, player, hand, result);
    }

    public static ActionResult centrifugeUse(BlockState state, World level, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult result) {
        if (!player.isSneaking()) {
            if (level.isClient)
                return ActionResult.SUCCESS;

            BlockEntity blockentity = level.getBlockEntity(blockPos);
            if (blockentity instanceof CentrifugeBlockEntity centrifugeBlockEntity) {
                if (!AlexsCavesEnriched.CONFIG.centrifuge.cantInteractWithActive || centrifugeBlockEntity.getSpinSpeed() <= 0)
                    player.openHandledScreen(centrifugeBlockEntity);
                else
                    player.sendMessage(Text.translatable("block.alexscavesenriched.centrifuge.cannot_open").formatted(Formatting.RED), true);
            }
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(ACEBlockRegistry.CENTRIFUGE_BASE.get());
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }
}
